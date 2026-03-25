package com.projetdevis.service;

import com.projetdevis.dto.ValiderRequest;
import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.Client;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service Spring orchestrant le pipeline de génération de devis :
 *   Nettoyage → Extraction → Analyse → Brouillon
 */
@Service
public class DevisPipelineService {

    private final EmailCleanerService  cleanerService  = new EmailCleanerService();
    private final AnalysisService      analysisService = new AnalysisService();
    private final DraftService         draftService;
    private final DraftQuoteRepository quoteRepository;
    private final ClientRepository     clientRepository;

    // ExtractionService est créé à la demande car il nécessite OPENAI_API_KEY.
    // La création est différée pour donner un message d'erreur clair si la clé manque.
    private ExtractionService extractionService;

    public DevisPipelineService(DraftService draftService,
                                DraftQuoteRepository quoteRepository,
                                ClientRepository clientRepository) {
        this.draftService      = draftService;
        this.quoteRepository   = quoteRepository;
        this.clientRepository  = clientRepository;
    }

    private ExtractionService getExtractionService() {
        if (extractionService == null) {
            extractionService = new ExtractionService();
        }
        return extractionService;
    }

    /**
     * Traite un e-mail brut et retourne un devis brouillon.
     *
     * @param rawEmail texte brut de l'e-mail
     * @return devis brouillon généré
     * @throws IllegalStateException si la variable d'environnement OPENAI_API_KEY est absente
     */
    public DraftQuote process(String rawEmail) {
        // Étape 1 — Nettoyage (pas d'IA, toujours disponible)
        String cleaned = cleanerService.clean(rawEmail);

        // Étape 2 — Extraction (peut faire appel à l'IA pour les quantités humaines)
        ExtractedInfo extracted = getExtractionService().extract(cleaned);

        // Étape 3 — Analyse et classification
        AnalyzedInfo analyzed = analysisService.analyze(extracted);

        // Étape 4 — Génération du devis brouillon
        DraftQuote draft = draftService.generateDraft(analyzed);

        // Étape 5 — Création d'une fiche client prospect
        Client client = new Client("Prospect");
        client.setSourceOrigine("EMAIL");
        client.getHistoriqueDevis().add(draft.getQuoteNumber());
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
