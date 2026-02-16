package com.projetdevis;

import com.projetdevis.model.*;
import com.projetdevis.service.*;

/**
 * Application de test final du pipeline BMAD complet.
 *
 * Exécute les 6 étapes du pipeline pour chaque e-mail de test :
 * 1. Nettoyage de l'e-mail (EmailCleanerService)
 * 2. Extraction des informations (ExtractionService)
 * 3. Analyse et classification (AnalysisService)
 * 4. Génération du devis brouillon JSON (DraftService)
 * 5. Validation humaine - simulation (ValidationService)
 * 6. Envoi vers CRM - simulation (CrmService)
 *
 * 5 scénarios de test :
 * - E-mail simple et clair
 * - E-mail incomplet
 * - E-mail ambigu
 * - E-mail contradictoire
 * - E-mail avec signature/disclaimer
 */
public class App {

    // ========================================
    // E-MAIL 1 : Simple et clair
    // ========================================
    private static final String EMAIL_SIMPLE = """
        Objet : Demande de devis mobilier de bureau

        Bonjour,

        Je me permets de vous contacter pour une demande de devis concernant
        l'aménagement de nos nouveaux locaux.

        Nous aurions besoin des éléments suivants :
        - 25 bureaux opérationnels en chêne clair, dimensions 160x80cm
        - 25 chaises de bureau ergonomiques noires avec accoudoirs
        - 5 armoires de rangement hautes en métal blanc
        - 3 tables de réunion pour 8 personnes
        - 15 caissons mobiles 3 tiroirs

        Notre budget est de 45 000 euros HT.

        Livraison souhaitée avant le 15 mars 2026.

        Merci de votre retour.

        Cordialement,
        Marie Dupont
        Directrice Administrative
        Société TechnoPlus
        marie.dupont@technoplus.fr
        01 23 45 67 89
        """;

    // ========================================
    // E-MAIL 2 : Incomplet (informations manquantes)
    // ========================================
    private static final String EMAIL_INCOMPLET = """
        Objet : Besoin mobilier

        Bonjour,

        Nous avons besoin de mobilier pour notre entreprise.

        Il nous faudrait des bureaux et des chaises pour équiper
        une nouvelle salle.

        Pouvez-vous nous faire une proposition ?

        Merci,
        Jean Martin
        """;

    // ========================================
    // E-MAIL 3 : Ambigu (dates floues, quantités imprécises)
    // ========================================
    private static final String EMAIL_AMBIGU = """
        Objet : Demande d'équipement

        Bonjour,

        Nous envisageons de renouveler une partie de notre mobilier
        dans les prochaines semaines.

        Il nous faudrait environ :
        - Une dizaine de bureaux, peut-être plus selon les disponibilités
        - Quelques chaises confortables pour la salle de réunion
        - Des rangements, le nombre exact sera déterminé ultérieurement

        Notre enveloppe budgétaire se situe aux alentours de 15 à 20k euros,
        mais nous pourrions l'ajuster si nécessaire.

        La livraison devrait se faire courant printemps, idéalement
        fin mars ou début avril, voire plus tard si les délais l'exigent.

        Merci de nous recontacter pour en discuter.

        Bien à vous,
        Sophie Leroy
        """;

    // ========================================
    // E-MAIL 4 : Contradictoire
    // ========================================
    private static final String EMAIL_CONTRADICTOIRE = """
        Objet : Demande URGENTE - mobilier

        Bonjour,

        C'est très urgent, nous avons besoin de mobilier au plus vite !

        Voici notre demande :
        - 50 bureaux direction haut de gamme en bois massif
        - 50 fauteuils de direction en cuir
        - 20 tables de réunion pour 12 personnes
        - 30 armoires bibliothèques design
        - 100 chaises visiteurs premium

        Notre budget est de 5 000 euros TTC maximum.

        Livraison souhaitée pour décembre 2027.

        Merci de votre réactivité.

        Thomas Bernard
        contact@bernard-industries.fr
        """;

    // ========================================
    // E-MAIL 5 : Avec signature et disclaimer
    // ========================================
    private static final String EMAIL_SIGNATURE = """
        Objet : Demande de devis - Équipement salle de formation

        Bonjour,

        Suite à notre conversation téléphonique de ce matin, je vous confirme
        notre besoin en mobilier pour notre nouvelle salle de formation.

        Nous souhaitons commander :
        - 20 tables pliantes 140x70cm en mélaminé blanc
        - 40 chaises empilables noires
        - 2 armoires basses à rideaux
        - 1 bureau pour le formateur avec retour

        Budget alloué : 12 000 euros HT

        Livraison impérative avant le 28 février 2026.

        Dans l'attente de votre retour, je reste à votre disposition.

        Cordialement,

        --
        Pierre Moreau
        Responsable Services Généraux
        ACME Corporation
        12 rue de l'Industrie
        75001 Paris
        Tél : 01 23 45 67 89
        Mobile : 06 12 34 56 78
        pierre.moreau@acme-corp.fr
        www.acme-corp.fr

        LinkedIn : linkedin.com/in/pierremoreau

        ---
        Ce message et toutes les pièces jointes sont confidentiels et établis
        à l'intention exclusive de ses destinataires. Toute utilisation ou
        diffusion non autorisée est interdite. Si vous n'êtes pas le
        destinataire de ce message, merci de le détruire et d'en avertir
        l'expéditeur.

        This email and any attachments are confidential and intended solely
        for the use of the individual or entity to whom they are addressed.
        Any unauthorized use or dissemination is prohibited.

        Pensez à l'environnement avant d'imprimer ce mail.

        Envoyé depuis mon iPhone
        """;

    /**
     * Point d'entrée principal.
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 TEST FINAL DU PIPELINE BMAD COMPLET                  ║");
        System.out.println("║   Étapes : Nettoyage → Extraction → Analyse → Devis → Valid → CRM   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Initialisation de TOUS les services
        EmailCleanerService cleaner = new EmailCleanerService();
        ExtractionService extractor = new ExtractionService();
        AnalysisService analyzer = new AnalysisService();
        DraftService draftService = new DraftService();
        ValidationService validator = new ValidationService("Commercial Test");
        CrmService crmService = new CrmService();

        // Exécution des 5 tests avec le pipeline complet
        runFullPipeline("E-MAIL SIMPLE ET CLAIR", EMAIL_SIMPLE,
            cleaner, extractor, analyzer, draftService, validator, crmService);

        runFullPipeline("E-MAIL INCOMPLET", EMAIL_INCOMPLET,
            cleaner, extractor, analyzer, draftService, validator, crmService);

        runFullPipeline("E-MAIL AMBIGU", EMAIL_AMBIGU,
            cleaner, extractor, analyzer, draftService, validator, crmService);

        runFullPipeline("E-MAIL CONTRADICTOIRE", EMAIL_CONTRADICTOIRE,
            cleaner, extractor, analyzer, draftService, validator, crmService);

        runFullPipeline("E-MAIL AVEC SIGNATURE/DISCLAIMER", EMAIL_SIGNATURE,
            cleaner, extractor, analyzer, draftService, validator, crmService);

        // Rapport CRM final
        System.out.println();
        System.out.println(crmService.generateReport());

        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         FIN DES TESTS                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Exécute le pipeline complet (6 étapes) sur un e-mail.
     */
    private static void runFullPipeline(
            String testName,
            String rawEmail,
            EmailCleanerService cleaner,
            ExtractionService extractor,
            AnalysisService analyzer,
            DraftService draftService,
            ValidationService validator,
            CrmService crmService) {

        System.out.println("┌────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ TEST : " + padRight(testName, 64) + "│");
        System.out.println("└────────────────────────────────────────────────────────────────────────┘");
        System.out.println();

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 1 : NETTOYAGE
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 1/6 : NETTOYAGE DE L'E-MAIL");
        System.out.println("─".repeat(70));

        String cleanedEmail = cleaner.clean(rawEmail);
        System.out.println(cleaner.getCleaningStats(rawEmail, cleanedEmail));
        System.out.println("\nTexte nettoyé :");
        System.out.println("---");
        System.out.println(cleanedEmail.length() > 400 ?
            cleanedEmail.substring(0, 400) + "\n[...]" : cleanedEmail);
        System.out.println("---\n");

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 2 : EXTRACTION
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 2/6 : EXTRACTION DES INFORMATIONS");
        System.out.println("─".repeat(70));

        ExtractedInfo extracted = extractor.extract(cleanedEmail);
        System.out.println(extractor.getExtractionStats(extracted));
        System.out.println(extracted.toString());
        System.out.println();

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 3 : ANALYSE
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 3/6 : ANALYSE ET CLASSIFICATION");
        System.out.println("─".repeat(70));

        AnalyzedInfo analyzed = analyzer.analyze(extracted);
        System.out.println(analyzer.getAnalysisReport(analyzed));

        // Vérifier si l'analyse est fiable pour continuer
        if (!analyzed.isReliable()) {
            System.out.println("⚠ ANALYSE NON FIABLE - Pipeline interrompu");
            System.out.println("  Raisons :");
            if (!analyzed.hasItems()) {
                System.out.println("    - Aucun article valide détecté");
            }
            if (analyzed.getConfidence() < 0.5) {
                System.out.println("    - Confiance trop faible (" +
                    String.format("%.0f%%", analyzed.getConfidence() * 100) + ")");
            }
            // Afficher les incohérences majeures en premier
            if (analyzed.hasMajorInconsistency()) {
                System.out.println("  Incohérences MAJEURES (bloquantes) :");
                for (String inc : analyzed.getMajorInconsistencies()) {
                    System.out.println("    ✗ " + inc);
                }
            }
            // Puis les autres incohérences
            if (analyzed.hasInconsistencies()) {
                for (String inc : analyzed.getInconsistencies()) {
                    if (!inc.startsWith("[MAJEURE]")) {
                        System.out.println("    - " + inc);
                    }
                }
            }
            System.out.println("\n" + "═".repeat(70) + "\n");
            return;
        }

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 4 : GÉNÉRATION DU DEVIS BROUILLON
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 4/6 : GÉNÉRATION DU DEVIS BROUILLON");
        System.out.println("─".repeat(70));

        DraftQuote draft = draftService.generateDraft(analyzed);
        System.out.println(draftService.getDraftReport(draft));

        // Afficher le résumé JSON-like du brouillon
        System.out.println("BROUILLON DE DEVIS :");
        System.out.println("  Numéro: " + draft.getQuoteNumber());
        System.out.println("  Objet: " + draft.getSubject());
        System.out.println("  Statut: " + draft.getStatus().getLabel());
        System.out.println("  Priorité: " + draft.getPriority().getLabel());
        System.out.println("  Articles: " + draft.getItemCount());
        System.out.println("  Total HT: " + draft.getFormattedTotalHT());
        System.out.println("  Total TTC: " + draft.getFormattedTotalTTC());
        if (draft.getClientBudget() != null) {
            System.out.println("  Budget client: " + String.format("%,.0f €", draft.getClientBudget()));
            System.out.println("  Budget respecté: " + (draft.isBudgetRespected() ? "OUI" : "NON"));
        }
        System.out.println("  Confiance: " + String.format("%.0f%%", draft.getConfidence() * 100));

        if (!draft.getRecommendations().isEmpty()) {
            System.out.println("  Recommandations:");
            for (String rec : draft.getRecommendations()) {
                System.out.println("    • " + rec);
            }
        }

        if (draft.hasInconsistencies()) {
            System.out.println("  Incohérences:");
            for (String inc : draft.getInconsistencies()) {
                System.out.println("    ⚠ " + inc);
            }
        }
        System.out.println();

        // Vérifier si le brouillon peut être validé
        if (draft.getStatus() == DraftQuote.DraftStatus.REJETE) {
            System.out.println("⚠ BROUILLON REJETÉ - Pipeline interrompu");
            if (draft.hasInconsistencies()) {
                System.out.println("  Raisons du rejet :");
                for (String inc : draft.getInconsistencies()) {
                    System.out.println("    ✗ " + inc);
                }
            }
            if (!draft.getWarnings().isEmpty()) {
                System.out.println("  Avertissements :");
                for (String warn : draft.getWarnings()) {
                    System.out.println("    ⚠ " + warn);
                }
            }
            System.out.println("\n" + "═".repeat(70) + "\n");
            return;
        }

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 5 : VALIDATION HUMAINE (SIMULATION)
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 5/6 : VALIDATION HUMAINE (SIMULATION)");
        System.out.println("─".repeat(70));

        ValidatedQuote validated = validator.startValidation(draft);
        System.out.println("Validation démarrée par : " + validator.getOperatorName());
        System.out.println("Actions en attente : " + validated.getPendingActions().size());

        // Simulation des actions du commercial
        simulateHumanValidation(validator, validated, draft);

        System.out.println(validator.getValidationReport());

        // Vérifier si la validation a réussi
        if (validated.getValidationStatus() != ValidatedQuote.ValidationStatus.APPROUVE) {
            System.out.println("⚠ VALIDATION NON APPROUVÉE - Statut: " +
                validated.getValidationStatus().getLabel());
            System.out.println("\n" + "═".repeat(70) + "\n");
            return;
        }

        // ══════════════════════════════════════════════════════════════
        // ÉTAPE 6 : ENVOI VERS CRM (SIMULATION)
        // ══════════════════════════════════════════════════════════════
        System.out.println(">>> ÉTAPE 6/6 : ENVOI VERS CRM (SIMULATION)");
        System.out.println("─".repeat(70));

        // Création de l'enregistrement CRM avec extraction client
        CrmRecord record = crmService.createRecordWithClientExtraction(validated, rawEmail);

        System.out.println("ENREGISTREMENT CRM CRÉÉ :");
        System.out.println("  ID: " + record.getRecordId());
        System.out.println("  Opportunité: " + record.getOpportunityName());
        System.out.println("  Statut: " + record.getStatus().getLabel());
        System.out.println("  Montant HT: " + String.format("%,.2f €", record.getMontantHT()));
        System.out.println("  Probabilité: " + record.getProbabiliteConversion() + "%");
        System.out.println("  Tags: " + record.getTags());

        // Informations client
        Client client = crmService.findClientById(record.getClientId());
        if (client != null) {
            System.out.println("\n  CLIENT ASSOCIÉ :");
            System.out.println("    ID: " + client.getClientId());
            System.out.println("    Raison sociale: " + client.getRaisonSociale());
            System.out.println("    Email: " + client.getEmail());
            System.out.println("    Type: " + (client.isProspect() ? "Prospect" : "Client"));
        }

        // Export JSON
        CrmExport export = crmService.generateJsonExport(record);
        System.out.println("\n  EXPORT JSON :");
        String jsonContent = export.getExportedContent();
        // Afficher les premières lignes du JSON
        String[] jsonLines = jsonContent.split("\n");
        for (int i = 0; i < Math.min(15, jsonLines.length); i++) {
            System.out.println("    " + jsonLines[i]);
        }
        if (jsonLines.length > 15) {
            System.out.println("    [... " + (jsonLines.length - 15) + " lignes supplémentaires ...]");
        }

        // ══════════════════════════════════════════════════════════════
        // RÉSUMÉ FINAL
        // ══════════════════════════════════════════════════════════════
        System.out.println();
        System.out.println("─".repeat(70));
        System.out.println("✓ PIPELINE COMPLET RÉUSSI");
        System.out.println("  E-mail traité → Devis " + draft.getQuoteNumber() +
            " → CRM " + record.getRecordId());
        System.out.println("═".repeat(70));
        System.out.println();
    }

    /**
     * Simule les actions de validation humaine.
     */
    private static void simulateHumanValidation(ValidationService validator,
                                                  ValidatedQuote validated,
                                                  DraftQuote draft) {

        System.out.println("\n[Simulation des actions du commercial...]");

        // 1. Résoudre les actions en attente
        while (validated.hasPendingActions()) {
            validator.resolveAction(0, "Vérifié et validé par le commercial");
        }
        System.out.println("  ✓ Actions en attente résolues");

        // 2. Effacer les incohérences si présentes (après vérification manuelle)
        if (draft.hasInconsistencies()) {
            validator.clearInconsistencies("Incohérences vérifiées manuellement");
            System.out.println("  ✓ Incohérences vérifiées et effacées");
        }

        // 3. Valider tous les articles
        for (QuoteItem item : draft.getItems()) {
            if (item.getStatus() != QuoteItem.LineStatus.COMPLETE) {
                // Compléter le prix si manquant
                if (!item.hasPrice()) {
                    validator.modifyPrice(item.getLineNumber(), 250.0);
                }
                validator.validateItem(item.getLineNumber());
            }
        }
        System.out.println("  ✓ Articles validés");

        // 4. Ajouter un commentaire
        validator.addComment("Devis vérifié et prêt pour envoi au client");

        // 5. Approuver le devis
        if (validated.isReadyForApproval()) {
            validator.approve("Approuvé après vérification complète");
            System.out.println("  ✓ Devis approuvé");
        } else {
            System.out.println("  ⚠ Impossible d'approuver - conditions non remplies");
            // Forcer l'approbation pour la démo
            validator.approve("Approuvé malgré avertissements (démo)");
        }

        System.out.println();
    }

    /**
     * Complète une chaîne avec des espaces à droite.
     */
    private static String padRight(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }
}
