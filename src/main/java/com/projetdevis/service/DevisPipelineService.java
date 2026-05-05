package com.projetdevis.service;

import com.projetdevis.dto.ValiderRequest;
import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.Client;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service Spring orchestrant le pipeline de génération de devis :
 *   Nettoyage → Extraction LLM → Analyse → Brouillon
 *
 * L'extraction des produits est intégralement déléguée au LLM via ExtractInfoIA.
 * ExtractionService (regex) n'est plus utilisé dans ce pipeline.
 */
@Service
public class DevisPipelineService {

    private final EmailCleanerService  cleanerService  = new EmailCleanerService();
    private final AnalysisService      analysisService = new AnalysisService();
    private final DraftService         draftService;
    private final DraftQuoteRepository quoteRepository;
    private final ClientRepository     clientRepository;

    // ExtractInfoIA est créé à la demande car il nécessite OPENAI_API_KEY.
    private ExtractInfoIA extractInfoIA;

    public DevisPipelineService(DraftService draftService,
                                DraftQuoteRepository quoteRepository,
                                ClientRepository clientRepository) {
        this.draftService      = draftService;
        this.quoteRepository   = quoteRepository;
        this.clientRepository  = clientRepository;
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
        for (ExtractInfoIA.ProductInfo p : produits) {
            if (p.nom() == null || p.nom().isBlank()) continue;

            ItemRequest item = new ItemRequest();
            item.setProduct(p.nom());

            int qty = 1;
            try {
                qty = ia.parseQuantity(p.quantite() != null && !p.quantite().isBlank()
                        ? p.quantite() : "1");
            } catch (Exception ignored) {}
            item.setQuantity(qty);

            item.setUnite(p.unite());

            if (p.details() != null && !p.details().isBlank()) {
                item.addCharacteristic(p.details());
            }

            item.setRawLine(p.nom() + " × " + p.quantite() + " " + p.unite());
            info.addItem(item);
        }

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
        // Étape 1 — Nettoyage
        String cleaned = cleanerService.clean(rawEmail);

        // Étape 2 — Extraction LLM : métadonnées + produits
        ExtractInfoIA ia = getExtractInfoIA();
        ExtractInfoIA.MetadataInfo meta = ia.extractMetadata(cleaned);
        ExtractedInfo extracted = buildExtractedInfo(cleaned, ia, meta);

        // Étape 3 — Analyse et classification
        AnalyzedInfo analyzed = analysisService.analyze(extracted);

        // Étape 4 — Génération du devis brouillon
        DraftQuote draft = draftService.generateDraft(analyzed);

        // Étape 5 — Création ou récupération de la fiche client (déduplication par email)
        String nomClient   = meta.nomClient() != null && !meta.nomClient().isBlank()
                             ? meta.nomClient() : "Prospect";
        String emailClient = meta.emailClient() != null ? meta.emailClient().trim() : "";

        Client client = emailClient.isBlank()
                ? new Client(nomClient)
                : clientRepository.findByEmailOrigine(emailClient)
                                  .orElseGet(() -> new Client(nomClient));

        client.setSourceOrigine("EMAIL");
        client.setEmailOrigine(emailClient.isBlank() ? null : emailClient);
        if (!client.getHistoriqueDevis().contains(draft.getQuoteNumber())) {
            client.getHistoriqueDevis().add(draft.getQuoteNumber());
        }
        clientRepository.save(client);
        draft.setClientReference(client.getClientId());

        // Étape 6 — Sauvegarde du devis en base de données
        quoteRepository.save(draft);

        return draft;
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

        // Remise globale sur toutes les lignes
        if (req.getRemiseGlobale() != null && req.getRemiseGlobale() > 0) {
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

        quoteRepository.save(draft);
        return draft;
    }

    /** Recherche un devis par son numéro. */
    public Optional<DraftQuote> findByQuoteNumber(String quoteNumber) {
        return quoteRepository.findById(quoteNumber);
    }

    /** Retourne tous les devis sauvegardés. */
    public List<DraftQuote> findAll() {
        return quoteRepository.findAll();
    }
}
