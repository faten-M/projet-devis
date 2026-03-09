package com.projetdevis.demo;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.service.AnalysisService;
import com.projetdevis.service.EmailCleanerService;
import com.projetdevis.service.ExtractionService;

import java.util.List;
import java.util.Map;

/**
 * Classe de démonstration du service d'analyse BMAD.
 * Montre le pipeline complet : Email → Nettoyage → Extraction → Analyse
 *
 * Pipeline BMAD :
 * 1. Email brut → EmailCleanerService → Email nettoyé
 * 2. Email nettoyé → ExtractionService → ExtractedInfo
 * 3. ExtractedInfo → AnalysisService → AnalyzedInfo
 *
 * @author BMAD Pipeline - Étape 4
 * @version 1.0
 */
public class AnalysisDemo {

    public static void main(String[] args) {
        // Initialisation des services
        EmailCleanerService cleaner = new EmailCleanerService();
        ExtractionService extractor = new ExtractionService();
        AnalysisService analyzer = new AnalysisService();

        System.out.println("=".repeat(80));
        System.out.println("    DÉMONSTRATION - Pipeline BMAD Complet (Étapes 1-4)");
        System.out.println("=".repeat(80));

        // === EXEMPLE 1 : Email avec beaucoup de faux positifs ===
        testCase1_FauxPositifs(cleaner, extractor, analyzer);

        // === EXEMPLE 2 : Email avec doublons à fusionner ===
        testCase2_Doublons(cleaner, extractor, analyzer);

        // === EXEMPLE 3 : Email professionnel complet ===
        testCase3_EmailComplet(cleaner, extractor, analyzer);

        // === EXEMPLE 4 : Email avec incohérences ===
        testCase4_Incoherences(cleaner, extractor, analyzer);

        // === EXEMPLE 5 : Email minimaliste ===
        testCase5_Minimaliste(cleaner, extractor, analyzer);
    }

    private static void testCase1_FauxPositifs(EmailCleanerService cleaner,
                                               ExtractionService extractor,
                                               AnalysisService analyzer) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 1 : Email avec beaucoup de faux positifs");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            J'espère que vous allez bien.

            Suite à notre conversation téléphonique, je vous sollicite pour un devis
            concernant l'aménagement de nos nouveaux locaux.

            Nous aurions besoin de :
            - 50 chaises de bureau ergonomiques noires
            - 25 bureaux assis-debout en chêne clair (160x80cm)
            - 10 caissons 3 tiroirs métal
            - 5 armoires de rangement hautes

            Budget indicatif : 45 000€ HT
            Livraison souhaitée : avant le 15/03/2024

            Merci de votre retour.

            Cordialement,
            Jean DUPONT
            """;

        runPipeline(email, cleaner, extractor, analyzer);
    }

    private static void testCase2_Doublons(EmailCleanerService cleaner,
                                           ExtractionService extractor,
                                           AnalysisService analyzer) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 2 : Email avec doublons à fusionner");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            Pour notre open space, nous avons besoin de :
            - 15 bureaux blancs 140x70cm
            - 15 chaises ergonomiques noires
            - 15 caissons 3 tiroirs

            Pour la salle de réunion :
            - 1 table de réunion 10 personnes
            - 10 chaises visiteurs grises

            Note : les 15 bureaux doivent être identiques.

            Budget : 35 000 € HT
            Délai : fin février

            Merci.
            """;

        runPipeline(email, cleaner, extractor, analyzer);
    }

    private static void testCase3_EmailComplet(EmailCleanerService cleaner,
                                               ExtractionService extractor,
                                               AnalysisService analyzer) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 3 : Email professionnel complet");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour Madame, Monsieur,

            Dans le cadre de l'extension de nos bureaux au 3ème étage, nous
            souhaitons obtenir un devis pour l'équipement mobilier suivant :

            Pour l'open space (15 collaborateurs) :
            • 15 bureaux opérationnels assis-debout (dimensions 160x80cm)
              Coloris : plateau chêne clair, piètement blanc
            • 15 sièges ergonomiques avec accoudoirs réglables
              Coloris : mesh noir, structure grise
            • 15 caissons mobiles 3 tiroirs, métal anthracite

            Pour la salle de réunion (capacité 10 personnes) :
            • 1 table de réunion 280x120cm, plateau blanc, pied central métal
            • 10 chaises visiteurs empilables, tissu gris foncé

            Pour le bureau direction :
            • 1 bureau executive 200x100cm, finition noyer
            • 1 fauteuil direction cuir noir
            • 1 armoire basse 2 portes assortie

            Informations complémentaires :
            - Budget alloué : 80 000 € HT (négociable selon qualité)
            - Livraison souhaitée : semaine du 15 mars 2024
            - Installation et montage à inclure dans le devis
            - Garantie minimum 3 ans souhaitée

            Nous restons disponibles pour une visite de vos locaux.

            Cordialement,
            Sophie BERNARD
            """;

        runPipeline(email, cleaner, extractor, analyzer);
    }

    private static void testCase4_Incoherences(EmailCleanerService cleaner,
                                               ExtractionService extractor,
                                               AnalysisService analyzer) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 4 : Email avec incohérences");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            Demande URGENTE pour :
            - 500 bureaux
            - 500 chaises

            Budget : 1000 euros

            Livraison demain svp.

            Merci.
            """;

        runPipeline(email, cleaner, extractor, analyzer);
    }

    private static void testCase5_Minimaliste(EmailCleanerService cleaner,
                                              ExtractionService extractor,
                                              AnalysisService analyzer) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 5 : Email minimaliste");
        System.out.println("-".repeat(80));

        String email = """
            Salut,

            Il me faut 20 chaises noires pour le bureau.

            Merci
            """;

        runPipeline(email, cleaner, extractor, analyzer);
    }

    /**
     * Exécute le pipeline complet et affiche les résultats.
     */
    private static void runPipeline(String rawEmail,
                                    EmailCleanerService cleaner,
                                    ExtractionService extractor,
                                    AnalysisService analyzer) {

        System.out.println("\n[1] EMAIL ORIGINAL");
        System.out.println(truncate(rawEmail, 400));

        // Étape 1-2 : Nettoyage
        String cleanedEmail = cleaner.clean(rawEmail);
        System.out.println("\n[2] EMAIL NETTOYÉ");
        System.out.println(truncate(cleanedEmail, 300));

        // Étape 3 : Extraction
        ExtractedInfo extracted = extractor.extract(cleanedEmail);
        System.out.println("\n[3] EXTRACTION (avant analyse)");
        System.out.println("    Articles bruts extraits: " + extracted.getItemCount());

        // Étape 4 : Analyse
        AnalyzedInfo analyzed = analyzer.analyze(extracted);

        // Affichage des résultats
        printAnalysisResults(analyzed, analyzer);
    }

    /**
     * Affiche les résultats de l'analyse de manière détaillée.
     */
    private static void printAnalysisResults(AnalyzedInfo analyzed, AnalysisService analyzer) {
        System.out.println("\n" + "~".repeat(60));
        System.out.println("[4] RÉSULTATS DE L'ANALYSE");
        System.out.println("~".repeat(60));

        // Rapport du pipeline
        System.out.println("\n" + analyzer.getAnalysisReport(analyzed));

        // Articles par catégorie
        System.out.println("\nARTICLES VALIDÉS PAR CATÉGORIE:");
        Map<AnalyzedItem.Category, List<AnalyzedItem>> byCategory = analyzed.getItemsByCategory();

        int itemNum = 1;
        for (AnalyzedItem.Category cat : AnalyzedItem.Category.values()) {
            List<AnalyzedItem> items = byCategory.get(cat);
            if (items != null && !items.isEmpty()) {
                System.out.println("\n  " + cat.getLabel().toUpperCase() + " (" + items.size() + "):");
                for (AnalyzedItem item : items) {
                    String statusIcon = getStatusIcon(item.getStatus());
                    String confBar = getConfidenceBar(item.getConfidence(), 10);

                    System.out.println("    " + itemNum + ". " + statusIcon + " " + item.getSummary());
                    System.out.println("       Confiance: " + confBar + " " +
                                       String.format("%.0f%%", item.getConfidence() * 100));

                    if (item.hasCharacteristics()) {
                        System.out.println("       Caractéristiques: " +
                                           String.join(", ", item.getCharacteristics()));
                    }
                    if (item.hasWarnings()) {
                        for (String warn : item.getWarnings()) {
                            System.out.println("       ⚠ " + warn);
                        }
                    }
                    itemNum++;
                }
            }
        }

        // Totaux
        System.out.println("\n  TOTAUX:");
        System.out.println("    → " + analyzed.getItemCount() + " articles validés");
        System.out.println("    → " + analyzed.getTotalQuantity() + " unités totales");
        System.out.println("    → " + analyzed.getValidItems().size() + " articles sans problème");

        // Budget
        System.out.println("\nBUDGET:");
        if (analyzed.hasBudget()) {
            System.out.println("  Montant: " + analyzed.getFormattedBudget());
            System.out.println("  Type: " + (analyzed.isBudgetHT() ? "HT" : "TTC"));
        } else {
            System.out.println("  (Non spécifié)");
        }

        // Date de livraison
        System.out.println("\nDATE DE LIVRAISON:");
        if (analyzed.hasDeliveryDate()) {
            System.out.println("  Date: " + analyzed.getDeliveryDate());
            if (analyzed.getDeliveryDateRaw() != null) {
                System.out.println("  Brut: \"" + analyzed.getDeliveryDateRaw() + "\"");
            }
        } else if (analyzed.getDeliveryDateRaw() != null) {
            System.out.println("  Brut: \"" + analyzed.getDeliveryDateRaw() + "\" (non parsée)");
        } else {
            System.out.println("  (Non spécifiée)");
        }

        // Urgence
        System.out.println("\nURGENCE: " +
                           (analyzed.getUrgency() != null ? analyzed.getUrgency().toUpperCase() : "NORMAL"));

        // Notes
        if (!analyzed.getNotes().isEmpty()) {
            System.out.println("\nNOTES:");
            for (String note : analyzed.getNotes()) {
                System.out.println("  • " + note);
            }
        }

        // Incohérences
        if (analyzed.hasInconsistencies()) {
            System.out.println("\n⚠ INCOHÉRENCES DÉTECTÉES:");
            for (String inc : analyzed.getInconsistencies()) {
                System.out.println("  ✗ " + inc);
            }
        }

        // Avertissements globaux
        if (!analyzed.getWarnings().isEmpty()) {
            System.out.println("\nAVERTISSEMENTS:");
            for (String warn : analyzed.getWarnings()) {
                System.out.println("  ! " + warn);
            }
        }

        // Score final
        System.out.println("\n" + "=".repeat(40));
        System.out.println("SCORE FINAL");
        System.out.println("=".repeat(40));
        String fullConfBar = getConfidenceBar(analyzed.getConfidence(), 30);
        String level = getConfidenceLevel(analyzed.getConfidence());
        System.out.println("  Confiance: " + fullConfBar);
        System.out.println("  Score: " + String.format("%.0f%%", analyzed.getConfidence() * 100) +
                           " - " + level);
        System.out.println("  Fiabilité: " + (analyzed.isReliable() ? "✓ FIABLE" : "✗ NON FIABLE"));
    }

    /**
     * Tronque un texte si nécessaire.
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "\n... [tronqué]";
    }

    /**
     * Retourne une icône selon le statut.
     */
    private static String getStatusIcon(AnalyzedItem.ValidationStatus status) {
        return switch (status) {
            case VALID -> "✓";
            case WARNING -> "⚠";
            case INVALID -> "✗";
        };
    }

    /**
     * Génère une barre de confiance.
     */
    private static String getConfidenceBar(double confidence, int width) {
        int filled = (int) (confidence * width);
        int empty = width - filled;
        return "[" + "█".repeat(filled) + "░".repeat(empty) + "]";
    }

    /**
     * Retourne le niveau de confiance en texte.
     */
    private static String getConfidenceLevel(double confidence) {
        if (confidence >= 0.8) return "EXCELLENT";
        if (confidence >= 0.6) return "BON";
        if (confidence >= 0.4) return "MOYEN";
        if (confidence >= 0.2) return "FAIBLE";
        return "TRÈS FAIBLE";
    }
}
