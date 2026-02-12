package com.projetdevis.demo;

import com.projetdevis.model.*;
import com.projetdevis.service.*;

import java.time.LocalDate;

/**
 * Démonstration de l'Étape 6 : Validation humaine du devis.
 *
 * Cette démo montre le processus complet de validation par un commercial :
 * 1. Pipeline complet : Email → Nettoyage → Extraction → Analyse → Brouillon
 * 2. Validation humaine : modifications, corrections, remises
 * 3. Approbation ou rejet du devis
 *
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * @author BMAD Pipeline - Étape 6
 * @version 1.0
 */
public class ValidationDemo {

    // Services du pipeline
    private EmailCleanerService cleanerService;
    private ExtractionService extractionService;
    private AnalysisService analysisService;
    private DraftService draftService;
    private ValidationService validationService;

    /**
     * Point d'entrée de la démonstration.
     */
    public static void main(String[] args) {
        ValidationDemo demo = new ValidationDemo();
        demo.run();
    }

    /**
     * Constructeur - initialisation des services.
     */
    public ValidationDemo() {
        this.cleanerService = new EmailCleanerService();
        this.extractionService = new ExtractionService();
        this.analysisService = new AnalysisService();
        this.draftService = new DraftService();
        this.validationService = new ValidationService("Jean Dupont"); // Commercial
    }

    /**
     * Exécute la démonstration complète.
     */
    public void run() {
        printHeader();

        // Cas 1 : Validation avec modifications
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 1 : VALIDATION AVEC MODIFICATIONS ET APPROBATION");
        System.out.println("=".repeat(70));
        demoCasModificationsEtApprobation();

        // Cas 2 : Validation avec ajout d'articles
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 2 : AJOUT D'ARTICLES ET REMISES");
        System.out.println("=".repeat(70));
        demoCasAjoutArticlesEtRemises();

        // Cas 3 : Rejet d'un devis
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 3 : REJET D'UN DEVIS INCOHÉRENT");
        System.out.println("=".repeat(70));
        demoCasRejet();

        // Cas 4 : Mise en attente
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 4 : MISE EN ATTENTE POUR INFORMATION MANQUANTE");
        System.out.println("=".repeat(70));
        demoCasMiseEnAttente();

        printFooter();
    }

    /**
     * Affiche l'en-tête de la démo.
     */
    private void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                  ║");
        System.out.println("║         PIPELINE BMAD - ÉTAPE 6 : VALIDATION HUMAINE            ║");
        System.out.println("║                                                                  ║");
        System.out.println("║  Cette étape permet au commercial de :                          ║");
        System.out.println("║  • Modifier les articles (prix, quantités, désignation)         ║");
        System.out.println("║  • Ajouter ou supprimer des articles                            ║");
        System.out.println("║  • Appliquer des remises                                        ║");
        System.out.println("║  • Modifier les conditions (livraison, paiement)                ║");
        System.out.println("║  • Approuver ou rejeter le devis                                ║");
        System.out.println("║                                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Affiche le pied de page de la démo.
     */
    private void printFooter() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("FIN DE LA DÉMONSTRATION - ÉTAPE 6 TERMINÉE");
        System.out.println("=".repeat(70));
        System.out.println("\nL'Étape 6 (Validation humaine) est maintenant opérationnelle.");
        System.out.println("Le pipeline BMAD complet est fonctionnel :");
        System.out.println("  1. Nettoyage de l'email");
        System.out.println("  2. (Normalisation)");
        System.out.println("  3. Extraction des informations");
        System.out.println("  4. Analyse et enrichissement");
        System.out.println("  5. Génération du brouillon");
        System.out.println("  6. Validation humaine → DEVIS PRÊT");
    }

    /**
     * Cas 1 : Validation avec modifications puis approbation.
     */
    private void demoCasModificationsEtApprobation() {
        String email = """
            Bonjour,

            Suite à notre réunion, je vous confirme notre besoin pour l'aménagement
            de nos nouveaux bureaux :

            - 10 bureaux opérationnels 160x80cm
            - 10 fauteuils ergonomiques
            - 5 armoires de rangement hautes
            - 1 table de réunion 8 places

            Budget : 25000 euros HT
            Livraison souhaitée : avant le 15 mars 2025

            Cordialement,
            Marie Martin
            """;

        // Pipeline complet jusqu'au brouillon
        System.out.println("\n>>> Exécution du pipeline (Étapes 1-5)...\n");
        DraftQuote draft = executePipeline(email);

        System.out.println("Brouillon généré : " + draft.getQuoteNumber());
        System.out.println("Statut initial : " + draft.getStatus().getLabel());
        System.out.println("Total HT initial : " + draft.getFormattedTotalHT());

        // Démarrage de la validation
        System.out.println("\n>>> Démarrage de la validation humaine...\n");
        ValidatedQuote validation = validationService.startValidation(draft);

        System.out.println("Validateur : " + validation.getValidatorName());
        System.out.println("Actions en attente : " + validation.getPendingActions().size());

        // Modifications par le commercial
        System.out.println("\n>>> Modifications par le commercial :\n");

        // 1. Ajuster le prix des bureaux (ligne 1)
        System.out.println("1. Ajustement du prix des bureaux (ligne 1) : 450 → 420 €");
        validationService.modifyPrice(1, 420.0);

        // 2. Réduire la quantité de fauteuils
        System.out.println("2. Réduction quantité fauteuils (ligne 2) : 10 → 8");
        validationService.modifyQuantity(2, 8);

        // 3. Appliquer une remise sur les armoires
        System.out.println("3. Application d'une remise de 10% sur les armoires (ligne 3)");
        validationService.applyItemDiscount(3, 10.0);

        // 4. Modifier la désignation de la table
        System.out.println("4. Modification désignation table (ligne 4)");
        validationService.modifyDesignation(4, "Table de réunion ovale 8-10 places");

        // 5. Ajouter une condition spéciale
        System.out.println("5. Ajout d'une condition : montage inclus");
        validationService.addSpecialCondition("Montage et installation inclus dans le prix");

        // 6. Modifier les frais de livraison (offerts car gros volume)
        System.out.println("6. Livraison offerte (frais = 0)");
        validationService.modifyDeliveryFees(0.0);

        // Résoudre les actions en attente
        System.out.println("\n>>> Résolution des actions en attente :\n");
        while (validation.hasPendingActions()) {
            String action = validation.getPendingActions().get(0);
            System.out.println("  Résolution : " + action);
            validationService.resolveAction(0, "Traité par le commercial");
        }

        // Effacer les incohérences après vérification
        if (draft.hasInconsistencies()) {
            System.out.println("\n>>> Traitement des incohérences :\n");
            System.out.println("  Le commercial vérifie et valide manuellement les incohérences détectées.");
            validationService.clearInconsistencies("Incohérences vérifiées : date ajustée, budget négocié avec le client");
        }

        // Valider les articles
        System.out.println("\n>>> Validation des articles :\n");
        for (int i = 1; i <= draft.getItemCount(); i++) {
            validationService.validateItem(i);
        }

        // Afficher le rapport avant approbation
        System.out.println("\n>>> Rapport de validation :\n");
        System.out.println(validationService.getValidationReport());

        // Approbation finale
        System.out.println("\n>>> Approbation du devis...\n");
        boolean approved = validationService.approve("Devis validé après négociation. Prix ajustés selon budget client.");

        if (approved) {
            System.out.println("DEVIS APPROUVÉ !");
            System.out.println("Signature : " + validation.getValidatorSignature());
            System.out.println("Statut final : " + draft.getStatus().getLabel());
            System.out.println("Total HT final : " + draft.getFormattedTotalHT());
            System.out.println("Total TTC final : " + draft.getFormattedTotalTTC());
        }

        // Historique des modifications
        System.out.println("\n>>> Historique des modifications :\n");
        System.out.println(validation.getModificationHistory());
    }

    /**
     * Cas 2 : Ajout d'articles et application de remises.
     */
    private void demoCasAjoutArticlesEtRemises() {
        String email = """
            Demande de devis :
            - 20 chaises de bureau
            - 20 bureaux simples

            Merci de nous faire une offre.
            """;

        DraftQuote draft = executePipeline(email);
        System.out.println("\nBrouillon : " + draft.getQuoteNumber());
        System.out.println("Articles initiaux : " + draft.getItemCount());
        System.out.println("Total HT : " + draft.getFormattedTotalHT());

        // Validation
        validationService = new ValidationService("Sophie Bernard");
        ValidatedQuote validation = validationService.startValidation(draft);

        System.out.println("\n>>> Ajout d'articles complémentaires :\n");

        // Ajout d'accessoires
        int line1 = validationService.addItem(
            "Repose-pieds ergonomique",
            20, 45.0,
            AnalyzedItem.Category.ACCESSOIRE
        );
        System.out.println("Ajouté ligne " + line1 + " : Repose-pieds ergonomique x20");

        int line2 = validationService.addItem(
            "Tapis de souris ergonomique avec repose-poignet",
            20, 25.0,
            AnalyzedItem.Category.ACCESSOIRE
        );
        System.out.println("Ajouté ligne " + line2 + " : Tapis de souris x20");

        int line3 = validationService.addItem(
            "Support écran réglable",
            20, 75.0,
            AnalyzedItem.Category.ACCESSOIRE
        );
        System.out.println("Ajouté ligne " + line3 + " : Support écran x20");

        // Application d'une remise globale
        System.out.println("\n>>> Application d'une remise globale de 8% :\n");
        validationService.applyGlobalDiscount(8.0);

        // Ajout commentaire
        validationService.addComment("Pack complet ergonomie proposé au client - remise volume appliquée");

        // Résoudre toutes les actions et valider
        while (validation.hasPendingActions()) {
            validationService.resolveAction(0, "OK");
        }
        for (int i = 1; i <= draft.getItemCount(); i++) {
            validationService.validateItem(i);
        }

        System.out.println("Articles finaux : " + draft.getItemCount());
        System.out.println("Total HT après ajouts et remise : " + draft.getFormattedTotalHT());

        // Approbation
        validationService.approve("Pack ergonomie complet avec remise volume.");

        System.out.println("\n>>> Résumé de validation :\n");
        System.out.println(validation.getSummary());
    }

    /**
     * Cas 3 : Rejet d'un devis incohérent.
     */
    private void demoCasRejet() {
        String email = """
            Urgent ! Besoin de mobilier pour demain :
            - 500 bureaux direction cuir
            - 1000 fauteuils Herman Miller

            Budget : 5000 euros maximum.
            """;

        DraftQuote draft = executePipeline(email);
        System.out.println("\nBrouillon : " + draft.getQuoteNumber());
        System.out.println("Total HT estimé : " + draft.getFormattedTotalHT());
        System.out.println("Budget client : " + (draft.getClientBudget() != null ?
            String.format("%.2f €", draft.getClientBudget()) : "N/A"));

        if (draft.hasInconsistencies()) {
            System.out.println("\nIncohérences détectées :");
            for (String inc : draft.getInconsistencies()) {
                System.out.println("  - " + inc);
            }
        }

        // Validation
        validationService = new ValidationService("Pierre Leroy");
        ValidatedQuote validation = validationService.startValidation(draft);

        System.out.println("\n>>> Analyse par le commercial :\n");
        System.out.println("Le commercial constate que :");
        System.out.println("  - Les quantités sont irréalistes (500 bureaux, 1000 fauteuils)");
        System.out.println("  - Le budget est dérisoire pour ce volume");
        System.out.println("  - Le délai (demain) est impossible");

        // Ajout de commentaires avant rejet
        validationService.addComment("Demande incohérente : volumes impossibles, budget irréaliste");
        validationService.addComment("Nécessite clarification complète avec le client");

        // Rejet
        System.out.println("\n>>> Rejet du devis :\n");
        boolean rejected = validationService.reject(
            "Demande rejetée - Budget largement insuffisant pour les quantités demandées. " +
            "Délai de livraison irréaliste. Contacter le client pour clarifier le besoin réel."
        );

        if (rejected) {
            System.out.println("DEVIS REJETÉ");
            System.out.println("Statut : " + validation.getValidationStatus().getLabel());
            System.out.println("Raison : " + validation.getValidationComment());
        }
    }

    /**
     * Cas 4 : Mise en attente pour information manquante.
     */
    private void demoCasMiseEnAttente() {
        String email = """
            Bonjour,

            Nous souhaitons équiper notre nouvelle salle de réunion.
            Pouvez-vous nous faire un devis ?

            Cordialement
            """;

        DraftQuote draft = executePipeline(email);
        System.out.println("\nBrouillon : " + draft.getQuoteNumber());
        System.out.println("Articles : " + draft.getItemCount());
        System.out.println("Confiance : " + String.format("%.0f%%", draft.getConfidence() * 100));

        // Validation
        validationService = new ValidationService("Claire Moreau");
        ValidatedQuote validation = validationService.startValidation(draft);

        System.out.println("\n>>> Analyse par le commercial :\n");
        System.out.println("Le commercial constate que :");
        System.out.println("  - Aucun article spécifique n'est mentionné");
        System.out.println("  - Pas de budget indiqué");
        System.out.println("  - Pas de quantités précises");
        System.out.println("  - Information insuffisante pour établir un devis");

        // Ajout d'actions en attente
        validationService.addComment("Demande trop vague - informations insuffisantes");

        // Mise en attente
        System.out.println("\n>>> Mise en attente du devis :\n");
        validationService.putOnHold(
            "Attente retour client : taille de la salle, nombre de participants, " +
            "type de mobilier souhaité (table, chaises, écran...), budget"
        );

        System.out.println("Statut : " + validation.getValidationStatus().getLabel());
        System.out.println("\nActions en attente :");
        for (String action : validation.getPendingActions()) {
            System.out.println("  - " + action);
        }

        System.out.println("\nLe commercial doit maintenant contacter le client pour obtenir plus de détails.");
    }

    /**
     * Exécute le pipeline complet (étapes 1-5).
     *
     * @param email Email brut
     * @return Brouillon de devis généré
     */
    private DraftQuote executePipeline(String email) {
        // Étape 1 : Nettoyage
        String cleaned = cleanerService.clean(email);

        // Étape 3 : Extraction
        ExtractedInfo extracted = extractionService.extract(cleaned);

        // Étape 4 : Analyse
        AnalyzedInfo analyzed = analysisService.analyze(extracted);

        // Étape 5 : Brouillon
        return draftService.generateDraft(analyzed);
    }
}
