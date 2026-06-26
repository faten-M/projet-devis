package com.projetdevis.service;

import com.projetdevis.dto.ValiderRequest;
import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.CorrectionIA;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.CorrectionIARepository;
import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service Spring orchestrant le pipeline de génération de devis :
 *   Nettoyage → Extraction LLM → Analyse → Brouillon
 *
 * L'extraction des produits est intégralement déléguée au LLM via ExtractInfoIA.
 * ExtractionService (regex) n'est plus utilisé dans ce pipeline.
 */
@Service
public class DevisPipelineService {

    private final EmailCleanerIA           cleanerService;
    private final AnalysisService          analysisService;
    private final DraftService             draftService;
    private final DraftQuoteRepository     quoteRepository;
    private final ClientRepository         clientRepository;
    private final CorrectionIARepository   correctionRepository;
    private final DevisPersistenceService  persistenceService;

    // ExtractInfoIA créé à la demande : nécessite OPENAI_API_KEY au runtime.
    private ExtractInfoIA extractInfoIA;

    public DevisPipelineService(EmailCleanerIA cleanerService,
                                AnalysisService analysisService,
                                DraftService draftService,
                                DraftQuoteRepository quoteRepository,
                                ClientRepository clientRepository,
                                CorrectionIARepository correctionRepository,
                                DevisPersistenceService persistenceService) {
        this.cleanerService       = cleanerService;
        this.analysisService      = analysisService;
        this.draftService         = draftService;
        this.quoteRepository      = quoteRepository;
        this.clientRepository     = clientRepository;
        this.correctionRepository = correctionRepository;
        this.persistenceService   = persistenceService;
    }

    private ExtractInfoIA getExtractInfoIA() {
        if (extractInfoIA == null) {
            extractInfoIA = new ExtractInfoIA();
        }
        return extractInfoIA;
    }

    /**
     * Convertit la sortie LLM ({@link ExtractInfoIA.ProductInfo}) en {@link ExtractedInfo}
     * compatible avec la suite du pipeline.
     *
     * <p>Chaque produit renvoyé par le LLM est transformé en {@link ItemRequest} :
     * <ul>
     *   <li>le nom est conservé exactement tel que le LLM l'a extrait de l'e-mail ;</li>
     *   <li>la quantité floue est convertie en entier via le parseur interne ;</li>
     *   <li>les détails (couleur, matière…) sont conservés comme caractéristique libre.</li>
     * </ul>
     */
    private ExtractedInfo buildExtractedInfo(String cleaned, ExtractInfoIA ia,
                                             ExtractInfoIA.MetadataInfo meta) {
        ExtractedInfo info = new ExtractedInfo(cleaned);

        // Extraction des produits
        List<ExtractInfoIA.ProductInfo> produits = ia.extractProductList(cleaned);

        // — Produits —
        List<String> quantitesFloues = new ArrayList<>();
        for (ExtractInfoIA.ProductInfo p : produits) {
            if (p.nom() == null || p.nom().isBlank()) continue;

            ItemRequest item = new ItemRequest();
            item.setProduct(p.nom());

            String qteRaw = p.quantite() != null && !p.quantite().isBlank() ? p.quantite() : "1";
            int qty = 1;
            try {
                qty = ia.parseQuantity(qteRaw);
            } catch (Exception ignored) {}
            item.setQuantity(qty);

            // Détection quantité floue : si le texte original n'est pas un nombre exact
            if (!qteRaw.trim().matches("\\d+")) {
                quantitesFloues.add("\"" + p.nom() + "\" : quantité \"" + qteRaw
                        + "\" interprétée comme " + qty + " — à confirmer avec le client");
            }

            item.setUnite(p.unite());

            if (p.details() != null && !p.details().isBlank()) {
                item.addCharacteristic(p.details());
            }

            item.setRawLine(p.nom() + " × " + qteRaw + " " + p.unite());
            info.addItem(item);
        }
        info.setQuantitesFloues(quantitesFloues);

        // — Budget —
        if (meta.budgetMontant() != null) {
            info.setBudget(meta.budgetMontant());
            info.setBudgetUnit(meta.budgetUnite());
            info.setBudgetRaw(meta.budgetBrut());
        }

        // — Date de livraison —
        if (meta.dateLivraisonBrut() != null && !meta.dateLivraisonBrut().isBlank()) {
            info.setDeliveryDateRaw(meta.dateLivraisonBrut());
        }
        if (meta.dateLivraison() != null && !meta.dateLivraison().isBlank()) {
            try {
                info.setDeliveryDate(LocalDate.parse(meta.dateLivraison()));
            } catch (DateTimeParseException e) {
                System.err.println("[Pipeline] Date non parsable : " + meta.dateLivraison());
            }
        }

        // — Urgence —
        if (meta.urgence() != null && !meta.urgence().isBlank()) {
            info.setUrgency(meta.urgence());
        }

        info.setConfidence(produits.isEmpty() ? 0.0 : 0.7);
        return info;
    }

    /**
     * Traite un e-mail brut et retourne un devis brouillon.
     *
     * @param rawEmail texte brut de l'e-mail
     * @return devis brouillon généré
     * @throws IllegalStateException si la variable d'environnement OPENAI_API_KEY est absente
     */
    public DraftQuote process(String rawEmail) {
        ExtractInfoIA ia = getExtractInfoIA();

        // Étape 1+2 — Nettoyage ET extraction des métadonnées en PARALLÈLE
        // extractMetadata utilise rawEmail directement → pas besoin d'attendre le cleaner
        CompletableFuture<String> cleanedFuture =
            CompletableFuture.supplyAsync(() -> cleanerService.clean(rawEmail));
        CompletableFuture<ExtractInfoIA.MetadataInfo> metaFuture =
            CompletableFuture.supplyAsync(() -> ia.extractMetadata(rawEmail));

        String cleaned = cleanedFuture.join();
        ExtractInfoIA.MetadataInfo meta = metaFuture.join();

        // Étape 3 — Extraction des produits (nécessite cleaned, donc après)
        ExtractedInfo extracted = buildExtractedInfo(cleaned, ia, meta);

        // Étape 3 — Analyse et classification (logique Java, pas de transaction DB)
        AnalyzedInfo analyzed = analysisService.analyze(extracted);

        // Étape 4 — Génération du devis brouillon (logique Java, pas de transaction DB)
        DraftQuote draft = draftService.generateDraft(analyzed);

        // Alertes quantités floues → recommandations pour le commercial
        for (String alerte : extracted.getQuantitesFloues()) {
            draft.addRecommendation("Quantité imprécise — " + alerte);
        }

        // Corps de l'email conservé 30 jours pour vérification commerciale (purgé automatiquement ensuite)
        draft.setEmailOriginal(rawEmail);

        // Étape 5 — Sauvegarde en DB dans une vraie transaction (service séparé = proxy Spring actif)
        String nomClient   = meta.nomClient() != null && !meta.nomClient().isBlank() ? meta.nomClient() : null;
        String emailClient = meta.emailClient() != null ? meta.emailClient().trim() : "";
        return persistenceService.saveDevisWithClient(draft, nomClient, emailClient, meta.segmentClient());
    }

    /**
     * Valide ou modifie un devis existant.
     * Permet au commercial de changer le statut, appliquer une remise, etc.
     *
     * @param quoteNumber numéro du devis
     * @param req         modifications demandées
     * @return devis mis à jour
     * @throws NoSuchElementException si le devis n'existe pas
     * @throws IllegalArgumentException si le statut fourni est invalide
     */
    public DraftQuote valider(String quoteNumber, ValiderRequest req) {
        DraftQuote draft = quoteRepository.findById(quoteNumber)
                .orElseThrow(() -> new NoSuchElementException("Devis introuvable : " + quoteNumber));

        // Changement de statut
        if (req.getStatut() != null && !req.getStatut().isBlank()) {
            try {
                draft.setStatus(DraftQuote.DraftStatus.valueOf(req.getStatut().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Statut invalide : '" + req.getStatut() + "'. "
                    + "Valeurs acceptées : BROUILLON, A_COMPLETER, A_VALIDER, PRET, REJETE.");
            }
        }

        // Mise à jour des articles (désignation, quantité, prix, remise individuelle)
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            java.util.Map<Integer, QuoteItem> byLine = new java.util.HashMap<>();
            for (QuoteItem qi : draft.getItems()) byLine.put(qi.getLineNumber(), qi);

            // Corrections IA enregistrées uniquement lors d'une validation finale (PRET ou REJETE)
            // Sauvegarder = brouillon en cours, pas une correction définitive
            boolean isFinalValidation = "PRET".equalsIgnoreCase(req.getStatut())
                                     || "REJETE".equalsIgnoreCase(req.getStatut());
            if (isFinalValidation) {
                for (ValiderRequest.ItemUpdate u : req.getItems()) {
                    QuoteItem existing = byLine.get(u.getLineNumber());
                    if (existing == null) continue;
                    if (u.getQuantity() != null && u.getQuantity() != existing.getQuantity()) {
                        correctionRepository.save(correction(quoteNumber, u.getLineNumber(),
                            CorrectionIA.ChampCorrige.QUANTITE,
                            String.valueOf(existing.getQuantity()),
                            String.valueOf(u.getQuantity())));
                    }
                    if (u.getUnitPriceHT() != null && existing.getUnitPriceHT() != null
                            && !u.getUnitPriceHT().equals(existing.getUnitPriceHT())) {
                        correctionRepository.save(correction(quoteNumber, u.getLineNumber(),
                            CorrectionIA.ChampCorrige.PRIX_UNITAIRE,
                            String.format("%.2f", existing.getUnitPriceHT()),
                            String.format("%.2f", u.getUnitPriceHT())));
                    }
                    if (u.getDesignation() != null && !u.getDesignation().equals(existing.getDesignation())) {
                        correctionRepository.save(correction(quoteNumber, u.getLineNumber(),
                            CorrectionIA.ChampCorrige.DESIGNATION,
                            existing.getDesignation(),
                            u.getDesignation()));
                    }
                }
            }

            for (ValiderRequest.ItemUpdate u : req.getItems()) {
                QuoteItem qi = byLine.get(u.getLineNumber());
                if (qi == null) {
                    // Nouvel article ajouté par le commercial
                    qi = new QuoteItem();
                    if (u.getLineNumber() != null) qi.setLineNumber(u.getLineNumber());
                    draft.getItems().add(qi);
                }
                if (u.getDesignation()   != null) qi.setDesignation(u.getDesignation());
                if (u.getQuantity()      != null) qi.setQuantity(u.getQuantity());
                if (u.getUnitPriceHT()   != null) qi.setUnitPriceHT(u.getUnitPriceHT());
                if (u.getDiscountPercent() != null) qi.setDiscountPercent(u.getDiscountPercent());
            }
            // Supprimer les articles qui ne sont plus dans la liste du commercial
            java.util.Set<Integer> keptLines = new java.util.HashSet<>();
            for (ValiderRequest.ItemUpdate u : req.getItems()) keptLines.add(u.getLineNumber());
            draft.getItems().removeIf(qi -> !keptLines.contains(qi.getLineNumber()));

            draft.recalculateTotals();
        }

        // Remise globale sur toutes les lignes (si pas d'articles individuels fournis)
        if (req.getRemiseGlobale() != null && req.getRemiseGlobale() > 0
                && (req.getItems() == null || req.getItems().isEmpty())) {
            for (QuoteItem item : draft.getItems()) {
                item.setDiscountPercent(req.getRemiseGlobale());
            }
            draft.recalculateTotals();
        }

        // Conditions de paiement
        if (req.getConditionsPaiement() != null && !req.getConditionsPaiement().isBlank()) {
            draft.setPaymentTerms(req.getConditionsPaiement());
        }

        // Commentaire du validateur → ajouté aux recommandations
        if (req.getCommentaire() != null && !req.getCommentaire().isBlank()) {
            draft.addRecommendation("Validateur : " + req.getCommentaire());
        }

        // Date de livraison corrigée manuellement
        if (req.getDateLivraison() != null && !req.getDateLivraison().isBlank()) {
            try {
                draft.setRequestedDeliveryDate(LocalDate.parse(req.getDateLivraison()));
                // Supprimer l'incohérence "date dans le passé" si elle existait
                draft.getInconsistencies().removeIf(i -> i.contains("Date de livraison dans le passé"));
            } catch (DateTimeParseException ignored) {}
        }

        // Nom client saisi manuellement par le commercial
        if (req.getNomClient() != null && !req.getNomClient().isBlank()) {
            draft.setClientNom(req.getNomClient().trim());
            if (draft.getClientReference() != null) {
                clientRepository.findById(draft.getClientReference()).ifPresent(c -> {
                    c.setRaisonSociale(req.getNomClient().trim());
                    clientRepository.save(c);
                });
            }
        }

        // Priorité modifiée par le commercial
        if (req.getPriorite() != null && !req.getPriorite().isBlank()) {
            try {
                draft.setPriority(DraftQuote.Priority.valueOf(req.getPriorite().trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        // Budget client révisé
        if (req.getBudgetClient() != null && req.getBudgetClient() > 0) {
            draft.setClientBudget(req.getBudgetClient());
        }

        // Aperçu du besoin révisé
        if (req.getSujetBesoin() != null && !req.getSujetBesoin().isBlank()) {
            draft.setSubject(req.getSujetBesoin().trim());
        }

        // Mode de livraison et frais
        if (req.getDeliveryIncluded() != null) {
            draft.setDeliveryIncluded(req.getDeliveryIncluded());
        }
        if (req.getDeliveryFees() != null) {
            draft.setDeliveryFees(req.getDeliveryFees() > 0 ? req.getDeliveryFees() : null);
            draft.recalculateTotals();
        }

        // Garantie
        if (req.getWarranty() != null) {
            draft.setWarranty(req.getWarranty().isBlank() ? null : req.getWarranty().trim());
        }

        quoteRepository.save(draft);
        return draft;
    }

    private CorrectionIA correction(String quoteNumber, Integer lineNumber,
                                     CorrectionIA.ChampCorrige champ,
                                     String valeurIa, String valeurCommerciale) {
        CorrectionIA c = new CorrectionIA();
        c.setQuoteNumber(quoteNumber);
        c.setLineNumber(lineNumber);
        c.setChamp(champ);
        c.setValeurIa(valeurIa);
        c.setValeurCommerciale(valeurCommerciale);
        return c;
    }

    /** Recherche un devis par son numéro. */
    @Transactional(readOnly = true)
    public Optional<DraftQuote> findByQuoteNumber(String quoteNumber) {
        return quoteRepository.findById(quoteNumber);
    }

    /** Retourne tous les devis sauvegardés. */
    @Transactional(readOnly = true)
    public List<DraftQuote> findAll() {
        return quoteRepository.findAll();
    }
}
