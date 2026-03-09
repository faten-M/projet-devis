package com.projetdevis.demo;

import com.projetdevis.model.*;
import com.projetdevis.service.*;

import java.util.Map;

/**
 * Démonstration de l'Étape 7 : Intégration CRM.
 *
 * Cette démo montre le processus complet d'intégration CRM :
 * 1. Pipeline complet : Email → Validation → Devis prêt
 * 2. Extraction des informations client depuis l'email
 * 3. Création/mise à jour de la fiche client
 * 4. Enregistrement du devis dans le CRM
 * 5. Export des données pour CRM externe
 *
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * @author BMAD Pipeline - Étape 7
 * @version 1.0
 */
public class CrmDemo {

    // Services du pipeline
    private EmailCleanerService cleanerService;
    private ExtractionService extractionService;
    private AnalysisService analysisService;
    private DraftService draftService;
    private ValidationService validationService;
    private CrmService crmService;

    /**
     * Point d'entrée de la démonstration.
     */
    public static void main(String[] args) {
        CrmDemo demo = new CrmDemo();
        demo.run();
    }

    /**
     * Constructeur - initialisation des services.
     */
    public CrmDemo() {
        this.cleanerService = new EmailCleanerService();
        this.extractionService = new ExtractionService();
        this.analysisService = new AnalysisService();
        this.draftService = new DraftService();
        this.validationService = new ValidationService("Commercial BMAD");
        this.crmService = new CrmService();
    }

    /**
     * Exécute la démonstration complète.
     */
    public void run() {
        printHeader();

        // Cas 1 : Nouveau client avec informations complètes
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 1 : NOUVEAU CLIENT - INFORMATIONS COMPLÈTES");
        System.out.println("=".repeat(70));
        demoCasNouveauClientComplet();

        // Cas 2 : Client existant (même email)
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 2 : CLIENT EXISTANT - NOUVELLE DEMANDE");
        System.out.println("=".repeat(70));
        demoCasClientExistant();

        // Cas 3 : Export multi-format
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 3 : EXPORT MULTI-FORMAT POUR CRM EXTERNE");
        System.out.println("=".repeat(70));
        demoCasExportMultiFormat();

        // Cas 4 : Suivi des opportunités
        System.out.println("\n" + "=".repeat(70));
        System.out.println("CAS 4 : SUIVI DES OPPORTUNITÉS ET PIPELINE");
        System.out.println("=".repeat(70));
        demoCasSuiviOpportunites();

        // Rapport final
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RAPPORT CRM FINAL");
        System.out.println("=".repeat(70));
        System.out.println(crmService.generateReport());

        printFooter();
    }

    /**
     * Affiche l'en-tête de la démo.
     */
    private void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                  ║");
        System.out.println("║          PIPELINE BMAD - ÉTAPE 7 : INTÉGRATION CRM              ║");
        System.out.println("║                                                                  ║");
        System.out.println("║  Cette étape permet de :                                        ║");
        System.out.println("║  • Extraire les informations client depuis l'email              ║");
        System.out.println("║  • Créer ou mettre à jour la fiche client                       ║");
        System.out.println("║  • Enregistrer le devis validé dans le CRM                      ║");
        System.out.println("║  • Exporter les données pour CRM externes                       ║");
        System.out.println("║  • Suivre les opportunités commerciales                         ║");
        System.out.println("║                                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Affiche le pied de page de la démo.
     */
    private void printFooter() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("FIN DE LA DÉMONSTRATION - ÉTAPE 7 TERMINÉE");
        System.out.println("=".repeat(70));
        System.out.println("\nL'Étape 7 (Intégration CRM) est maintenant opérationnelle.");
        System.out.println("Le pipeline BMAD complet est fonctionnel :");
        System.out.println("  1. Nettoyage de l'email");
        System.out.println("  2. (Normalisation)");
        System.out.println("  3. Extraction des informations");
        System.out.println("  4. Analyse et enrichissement");
        System.out.println("  5. Génération du brouillon");
        System.out.println("  6. Validation humaine");
        System.out.println("  7. Intégration CRM → DEVIS ENREGISTRÉ ET EXPORTABLE");
    }

    /**
     * Cas 1 : Nouveau client avec informations complètes.
     */
    private void demoCasNouveauClientComplet() {
        String email = """
            De: marie.dupont@techsolutions.fr
            Objet: Demande de devis mobilier

            Bonjour,

            Je me permets de vous contacter pour une demande de devis.
            Notre société TECH SOLUTIONS SAS (SIRET: 123 456 789 01234)
            souhaite aménager ses nouveaux locaux situés au :
            15 rue de l'Innovation
            75008 Paris

            Nous avons besoin de :
            - 20 bureaux opérationnels avec caisson
            - 20 fauteuils ergonomiques
            - 10 armoires de rangement
            - 2 tables de réunion 10 places

            Budget prévu : 35000 euros HT
            Livraison souhaitée : fin du mois

            Vous pouvez me joindre au 06 12 34 56 78.

            Cordialement,
            Marie DUPONT
            Directrice Administrative
            TECH SOLUTIONS SAS
            """;

        System.out.println("\n>>> EMAIL D'ORIGINE (extrait) :\n");
        System.out.println(email.substring(0, Math.min(300, email.length())) + "...\n");

        // Extraction des informations client
        System.out.println(">>> EXTRACTION DES INFORMATIONS CLIENT :\n");
        Map<String, String> clientInfo = crmService.extractClientInfoFromEmail(email);
        for (Map.Entry<String, String> entry : clientInfo.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        // Pipeline complet jusqu'à la validation
        System.out.println("\n>>> EXÉCUTION DU PIPELINE (Étapes 1-6)...\n");
        ValidatedQuote validated = executePipelineAndValidate(email);

        if (validated == null || !validated.isApproved()) {
            System.out.println("Erreur: Le devis n'a pas pu être validé.");
            return;
        }

        System.out.println("Devis validé : " + validated.getDraft().getQuoteNumber());
        System.out.println("Total HT : " + validated.getDraft().getFormattedTotalHT());

        // Intégration CRM
        System.out.println("\n>>> INTÉGRATION CRM (Étape 7) :\n");
        CrmRecord record = crmService.createRecordWithClientExtraction(validated, email);

        System.out.println("Enregistrement CRM créé : " + record.getRecordId());

        // Affichage du client créé
        Client client = record.getClient();
        if (client != null) {
            System.out.println("\n>>> FICHE CLIENT CRÉÉE :\n");
            System.out.println(client.getSummary());
        }

        // Affichage de l'enregistrement CRM
        System.out.println("\n>>> ENREGISTREMENT CRM :\n");
        System.out.println(record.getSummary());
    }

    /**
     * Cas 2 : Client existant avec nouvelle demande.
     */
    private void demoCasClientExistant() {
        String email = """
            De: marie.dupont@techsolutions.fr
            Objet: Nouvelle demande - Extension bureaux

            Bonjour,

            Suite à notre précédente commande, nous souhaitons
            agrandir notre espace avec :

            - 5 bureaux supplémentaires
            - 5 fauteuils
            - 1 table de réunion 6 places

            Budget : 8000 euros

            Merci,
            Marie DUPONT
            """;

        System.out.println("\n>>> EMAIL DE CLIENT EXISTANT :\n");
        System.out.println(email.substring(0, Math.min(250, email.length())) + "...\n");

        // Vérification client existant
        Client existingClient = crmService.findClientByEmail("marie.dupont@techsolutions.fr");
        if (existingClient != null) {
            System.out.println(">>> CLIENT EXISTANT TROUVÉ :");
            System.out.println("  ID: " + existingClient.getClientId());
            System.out.println("  Société: " + existingClient.getRaisonSociale());
            System.out.println("  Devis précédents: " + existingClient.getHistoriqueDevis().size());
        }

        // Pipeline et validation
        ValidatedQuote validated = executePipelineAndValidate(email);

        if (validated != null && validated.isApproved()) {
            // Intégration CRM (client existant sera retrouvé)
            CrmRecord record = crmService.createRecordWithClientExtraction(validated, email);

            System.out.println("\n>>> NOUVEL ENREGISTREMENT CRM :");
            System.out.println("  ID: " + record.getRecordId());
            System.out.println("  Devis: " + record.getQuoteNumber());
            System.out.println("  Montant: " + record.getFormattedMontantHT());

            // Vérification mise à jour client
            Client updatedClient = record.getClient();
            if (updatedClient != null) {
                System.out.println("\n>>> CLIENT MIS À JOUR :");
                System.out.println("  Devis enregistrés: " + updatedClient.getHistoriqueDevis());
            }
        }
    }

    /**
     * Cas 3 : Export multi-format pour CRM externe.
     */
    private void demoCasExportMultiFormat() {
        // Utiliser un enregistrement existant ou en créer un nouveau
        String email = """
            De: contact@mobilier-pro.com
            Demande urgente de 50 chaises visiteur et 10 tables pliantes.
            Budget 15000 euros.
            Sophie MARTIN
            01 23 45 67 89
            """;

        ValidatedQuote validated = executePipelineAndValidate(email);

        if (validated == null || !validated.isApproved()) {
            return;
        }

        CrmRecord record = crmService.createRecordWithClientExtraction(validated, email);

        // Export JSON
        System.out.println("\n>>> EXPORT JSON :\n");
        CrmExport jsonExport = crmService.generateJsonExport(record);
        String jsonContent = jsonExport.generate();
        // Afficher les 500 premiers caractères
        System.out.println(jsonContent.substring(0, Math.min(800, jsonContent.length())));
        System.out.println("\n... [" + jsonContent.length() + " caractères au total]\n");

        // Export CSV
        System.out.println(">>> EXPORT CSV :\n");
        CrmExport csvExport = crmService.generateCsvExport(record);
        String csvContent = csvExport.generate();
        System.out.println(csvContent.substring(0, Math.min(600, csvContent.length())));
        System.out.println("\n... [" + csvContent.length() + " caractères au total]\n");

        // Export Salesforce
        System.out.println(">>> EXPORT SALESFORCE :\n");
        CrmExport sfExport = crmService.generateSalesforceExport(record);
        System.out.println("Export ID: " + sfExport.getExportId());
        System.out.println("Format: " + sfExport.getFormat().getLabel());
        System.out.println("Cible: " + sfExport.getTarget().getLabel());
        System.out.println("Métadonnées: " + sfExport.getMetadata());
    }

    /**
     * Cas 4 : Suivi des opportunités et pipeline.
     */
    private void demoCasSuiviOpportunites() {
        // Créer quelques opportunités supplémentaires pour la démo
        createSampleOpportunities();

        System.out.println("\n>>> STATISTIQUES CRM :\n");
        Map<String, Object> stats = crmService.getStatistics();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }

        // Opportunités ouvertes
        System.out.println("\n>>> OPPORTUNITÉS OUVERTES :\n");
        for (CrmRecord record : crmService.getOpenOpportunities()) {
            System.out.println(String.format("  • %s | %s | %,.2f € | Proba: %d%%",
                record.getQuoteNumber(),
                record.getStatus().getLabel(),
                record.getMontantHT() != null ? record.getMontantHT() : 0,
                record.getProbabiliteConversion()
            ));
        }

        // Simulation de suivi
        System.out.println("\n>>> SIMULATION DE SUIVI D'UNE OPPORTUNITÉ :\n");

        CrmRecord firstRecord = crmService.getOpenOpportunities().stream().findFirst().orElse(null);
        if (firstRecord != null) {
            System.out.println("Opportunité sélectionnée : " + firstRecord.getRecordId());

            // Envoi du devis
            System.out.println("\n1. Envoi du devis au client...");
            firstRecord.enregistrerEnvoiDevis("Commercial BMAD");
            System.out.println("   Statut: " + firstRecord.getStatus().getLabel());

            // Relance
            System.out.println("\n2. Enregistrement d'une relance...");
            firstRecord.enregistrerRelance("Commercial BMAD", "Relance par téléphone - client intéressé");
            System.out.println("   Nombre de relances: " + firstRecord.getNombreRelances());

            // Passage en négociation
            System.out.println("\n3. Passage en négociation...");
            firstRecord.setStatus(CrmRecord.OpportunityStatus.NEGOCIATION);
            System.out.println("   Statut: " + firstRecord.getStatus().getLabel());
            System.out.println("   Probabilité: " + firstRecord.getProbabiliteConversion() + "%");

            // Clôture gagnée
            System.out.println("\n4. Clôture de l'opportunité (GAGNÉE)...");
            firstRecord.marquerGagnee("Commercial BMAD", "Commande confirmée par email");
            System.out.println("   Statut final: " + firstRecord.getStatus().getLabel());

            // Historique des actions
            System.out.println("\n>>> HISTORIQUE DES ACTIONS :\n");
            System.out.println(firstRecord.getActionHistory());
        }
    }

    /**
     * Crée des opportunités d'exemple pour la démo.
     */
    private void createSampleOpportunities() {
        // Opportunité 2
        String email2 = """
            Bonjour, nous cherchons 100 chaises pour notre amphithéâtre.
            contact@universite-paris.fr
            Budget: 20000 euros
            """;
        ValidatedQuote v2 = executePipelineAndValidate(email2);
        if (v2 != null && v2.isApproved()) {
            CrmRecord r2 = crmService.createRecordWithClientExtraction(v2, email2);
            r2.addTag("education");
        }

        // Opportunité 3
        String email3 = """
            Urgent: 30 bureaux pour notre nouveau site.
            rh@startup-tech.io
            Budget: 25000 euros
            Jean MARTIN
            """;
        ValidatedQuote v3 = executePipelineAndValidate(email3);
        if (v3 != null && v3.isApproved()) {
            CrmRecord r3 = crmService.createRecordWithClientExtraction(v3, email3);
            r3.addTag("startup");
            r3.addTag("urgent");
        }
    }

    /**
     * Exécute le pipeline complet et valide le devis.
     *
     * @param email Email brut
     * @return Devis validé ou null
     */
    private ValidatedQuote executePipelineAndValidate(String email) {
        try {
            // Étape 1 : Nettoyage
            String cleaned = cleanerService.clean(email);

            // Étape 3 : Extraction
            ExtractedInfo extracted = extractionService.extract(cleaned);

            // Étape 4 : Analyse
            AnalyzedInfo analyzed = analysisService.analyze(extracted);

            // Étape 5 : Brouillon
            DraftQuote draft = draftService.generateDraft(analyzed);

            // Étape 6 : Validation
            validationService = new ValidationService("Commercial BMAD");
            ValidatedQuote validation = validationService.startValidation(draft);

            // Résoudre les actions en attente et valider les articles
            while (validation.hasPendingActions()) {
                validationService.resolveAction(0, "Auto-résolu pour démo");
            }

            // Effacer les incohérences si présentes
            if (draft.hasInconsistencies()) {
                validationService.clearInconsistencies("Vérifié pour démo");
            }

            // Valider tous les articles
            for (int i = 1; i <= draft.getItemCount(); i++) {
                validationService.validateItem(i);
            }

            // Approbation
            if (validation.isReadyForApproval()) {
                validationService.approve("Approuvé automatiquement pour démo CRM");
            }

            return validation;

        } catch (Exception e) {
            System.out.println("Erreur lors du pipeline: " + e.getMessage());
            return null;
        }
    }
}
