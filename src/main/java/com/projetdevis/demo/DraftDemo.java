package com.projetdevis.demo;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.service.AnalysisService;
import com.projetdevis.service.DraftService;
import com.projetdevis.service.EmailCleanerService;
import com.projetdevis.service.ExtractionService;

import java.util.List;
import java.util.Map;

/**
 * Classe de démonstration du service de génération de brouillon BMAD.
 * Montre le pipeline complet : Email → Nettoyage → Extraction → Analyse → Brouillon
 *
 * Pipeline BMAD :
 * 1. Email brut → EmailCleanerService → Email nettoyé
 * 2. Email nettoyé → ExtractionService → ExtractedInfo
 * 3. ExtractedInfo → AnalysisService → AnalyzedInfo
 * 4. AnalyzedInfo → DraftService → DraftQuote
 *
 * @author BMAD Pipeline - Étape 5
 * @version 1.0
 */
public class DraftDemo {

    public static void main(String[] args) {
        // Initialisation des services
        EmailCleanerService cleaner = new EmailCleanerService();
        ExtractionService extractor = new ExtractionService();
        AnalysisService analyzer = new AnalysisService();
        DraftService drafter = new DraftService();

        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║      DÉMONSTRATION - Pipeline BMAD Complet (Étapes 1-5)                      ║");
        System.out.println("║      Nettoyage → Extraction → Analyse → Brouillon de Devis                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

        // === EXEMPLE 1 : Demande professionnelle complète ===
        testCase1_DemandeComplete(cleaner, extractor, analyzer, drafter);

        // === EXEMPLE 2 : Demande avec budget serré ===
        testCase2_BudgetSerre(cleaner, extractor, analyzer, drafter);

        // === EXEMPLE 3 : Demande urgente ===
        testCase3_DemandeUrgente(cleaner, extractor, analyzer, drafter);

        // === EXEMPLE 4 : Demande minimaliste ===
        testCase4_DemandeMinimaliste(cleaner, extractor, analyzer, drafter);

        // === EXEMPLE 5 : Grande commande avec remise ===
        testCase5_GrandeCommande(cleaner, extractor, analyzer, drafter);
    }

    private static void testCase1_DemandeComplete(EmailCleanerService cleaner,
                                                   ExtractionService extractor,
                                                   AnalysisService analyzer,
                                                   DraftService drafter) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("EXEMPLE 1 : Demande professionnelle complète");
        System.out.println("═".repeat(80));

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
            - Budget alloué : 80 000 € HT
            - Livraison souhaitée : semaine du 15 mars 2024
            - Installation et montage à inclure dans le devis
            - Garantie minimum 3 ans souhaitée

            Nous restons disponibles pour une visite de vos locaux.

            Cordialement,
            Sophie BERNARD
            Directrice Administrative
            """;

        runFullPipeline(email, cleaner, extractor, analyzer, drafter);
    }

    private static void testCase2_BudgetSerre(EmailCleanerService cleaner,
                                               ExtractionService extractor,
                                               AnalysisService analyzer,
                                               DraftService drafter) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("EXEMPLE 2 : Demande avec budget serré");
        System.out.println("═".repeat(80));

        String email = """
            Bonjour,

            Nous sommes une startup et avons un budget très limité pour équiper
            notre premier bureau (5 personnes).

            Nous aurions besoin de :
            - 5 bureaux simples 120x60cm (blanc ou gris)
            - 5 chaises de bureau basiques avec roulettes
            - 1 armoire de rangement moyenne
            - 5 caissons 2 tiroirs

            Budget maximum : 3 000 € TTC (non négociable)

            Livraison dans les 2 semaines si possible.

            Merci pour votre retour rapide.

            Thomas
            """;

        runFullPipeline(email, cleaner, extractor, analyzer, drafter);
    }

    private static void testCase3_DemandeUrgente(EmailCleanerService cleaner,
                                                  ExtractionService extractor,
                                                  AnalysisService analyzer,
                                                  DraftService drafter) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("EXEMPLE 3 : Demande urgente");
        System.out.println("═".repeat(80));

        String email = """
            URGENT !!!

            Bonjour,

            Suite à un dégât des eaux, nous devons remplacer en urgence
            le mobilier de notre salle de réunion :

            - 1 grande table de réunion 12 personnes
            - 12 chaises de réunion noires
            - 1 meuble bas de rangement

            C'est TRÈS URGENT, nous avons besoin de tout pour lundi prochain !

            Budget : 8 000 € HT

            Merci de nous appeler dès réception de ce mail.

            Cordialement,
            Pierre MARTIN
            Tél: 06 12 34 56 78
            """;

        runFullPipeline(email, cleaner, extractor, analyzer, drafter);
    }

    private static void testCase4_DemandeMinimaliste(EmailCleanerService cleaner,
                                                      ExtractionService extractor,
                                                      AnalysisService analyzer,
                                                      DraftService drafter) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("EXEMPLE 4 : Demande minimaliste");
        System.out.println("═".repeat(80));

        String email = """
            Salut,

            Je cherche 3 bureaux et 3 chaises pour mon bureau.
            Couleur noire de préférence.

            Combien ça coûte ?

            Merci
            """;

        runFullPipeline(email, cleaner, extractor, analyzer, drafter);
    }

    private static void testCase5_GrandeCommande(EmailCleanerService cleaner,
                                                  ExtractionService extractor,
                                                  AnalysisService analyzer,
                                                  DraftService drafter) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("EXEMPLE 5 : Grande commande avec remise attendue");
        System.out.println("═".repeat(80));

        String email = """
            Bonjour,

            Nous déménageons dans de nouveaux locaux de 2000m² et devons
            équiper entièrement les espaces suivants :

            Open space principal (50 postes) :
            - 50 bureaux assis-debout électriques 160x80cm
            - 50 sièges ergonomiques haut de gamme
            - 50 caissons 3 tiroirs métal

            Salles de réunion (3 salles) :
            - 3 tables de réunion 10 personnes
            - 30 chaises visiteurs

            Espaces détente (2 zones) :
            - 4 canapés 3 places
            - 4 tables basses
            - 8 fauteuils lounge

            Cloisonnement :
            - 20 cloisons acoustiques 180x120cm

            Budget global : 250 000 € HT
            (nous attendons une remise significative vu le volume)

            Livraison échelonnée sur 2 mois à partir du 1er avril.

            Cordialement,
            Marie LEROY
            Responsable des Services Généraux
            """;

        runFullPipeline(email, cleaner, extractor, analyzer, drafter);
    }

    /**
     * Exécute le pipeline complet et affiche les résultats détaillés.
     */
    private static void runFullPipeline(String rawEmail,
                                        EmailCleanerService cleaner,
                                        ExtractionService extractor,
                                        AnalysisService analyzer,
                                        DraftService drafter) {

        System.out.println("\n┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ [1] EMAIL ORIGINAL                                                  │");
        System.out.println("└─────────────────────────────────────────────────────────────────────┘");
        System.out.println(truncate(rawEmail, 400));

        // Étape 1-2 : Nettoyage
        String cleanedEmail = cleaner.clean(rawEmail);

        // Étape 3 : Extraction
        ExtractedInfo extracted = extractor.extract(cleanedEmail);
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ [2-3] NETTOYAGE & EXTRACTION                                        │");
        System.out.println("└─────────────────────────────────────────────────────────────────────┘");
        System.out.println("  Articles extraits: " + extracted.getItemCount());
        System.out.println("  Budget détecté: " + (extracted.hasBudget() ?
            String.format("%,.0f %s", extracted.getBudget(), extracted.getBudgetUnit()) : "Non spécifié"));
        System.out.println("  Date livraison: " + (extracted.hasDeliveryDate() ?
            extracted.getDeliveryDate() : "Non spécifiée"));

        // Étape 4 : Analyse
        AnalyzedInfo analyzed = analyzer.analyze(extracted);
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ [4] ANALYSE                                                         │");
        System.out.println("└─────────────────────────────────────────────────────────────────────┘");
        System.out.println("  Articles validés: " + analyzed.getValidItems().size());
        System.out.println("  Quantité totale: " + analyzed.getTotalQuantity() + " unités");
        System.out.println("  Confiance analyse: " + String.format("%.0f%%", analyzed.getConfidence() * 100));
        System.out.println("  Fiable: " + (analyzed.isReliable() ? "OUI" : "NON"));

        // Étape 5 : Brouillon du devis
        DraftQuote draft = drafter.generateDraft(analyzed);

        // Affichage détaillé du brouillon
        printDraftResults(draft, drafter);
    }

    /**
     * Affiche les résultats détaillés du brouillon.
     */
    private static void printDraftResults(DraftQuote draft, DraftService drafter) {
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ [5] BROUILLON DU DEVIS                                              │");
        System.out.println("└─────────────────────────────────────────────────────────────────────┘");

        // En-tête du devis
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  DEVIS N° " + draft.getQuoteNumber());
        System.out.println("─".repeat(70));
        System.out.println("  Créé le:        " + draft.getFormattedCreatedAt());
        System.out.println("  Valide jusqu'au: " + draft.getFormattedValidUntil() +
                          " (" + draft.getDaysUntilExpiry() + " jours)");
        System.out.println("  Statut:         " + draft.getStatus().getLabel() +
                          " - " + draft.getStatus().getDescription());
        System.out.println("  Priorité:       " + draft.getPriority().getLabel());

        if (draft.getSubject() != null) {
            System.out.println("\n  Objet: " + truncate(draft.getSubject(), 60));
        }

        // Lignes du devis par catégorie
        System.out.println("\n" + "─".repeat(70));
        System.out.println("  DÉTAIL DES ARTICLES");
        System.out.println("─".repeat(70));

        Map<AnalyzedItem.Category, List<QuoteItem>> sections = draft.getSections();
        int lineNum = 1;

        for (AnalyzedItem.Category category : AnalyzedItem.Category.values()) {
            List<QuoteItem> items = sections.get(category);
            if (items != null && !items.isEmpty()) {
                System.out.println("\n  ▶ " + category.getLabel().toUpperCase() +
                                  " (" + items.size() + " article(s))");
                System.out.println("  " + "─".repeat(66));

                for (QuoteItem item : items) {
                    printQuoteItem(item, lineNum);
                    lineNum++;
                }
            }
        }

        // Récapitulatif financier
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  RÉCAPITULATIF FINANCIER");
        System.out.println("═".repeat(70));

        if (draft.getTotalHTBrut() != null && draft.getTotalHTBrut() > 0) {
            System.out.println(String.format("  %-40s %20s", "Total HT brut:",
                String.format("%,.2f €", draft.getTotalHTBrut())));

            if (draft.getTotalDiscount() != null && draft.getTotalDiscount() > 0) {
                System.out.println(String.format("  %-40s %20s", "Remises:",
                    String.format("- %,.2f €", draft.getTotalDiscount())));
            }

            if (!draft.isDeliveryIncluded() && draft.getDeliveryFees() != null) {
                System.out.println(String.format("  %-40s %20s", "Frais de livraison:",
                    String.format("+ %,.2f €", draft.getDeliveryFees())));
            } else if (draft.isDeliveryIncluded()) {
                System.out.println(String.format("  %-40s %20s", "Frais de livraison:", "OFFERTS"));
            }

            System.out.println("  " + "─".repeat(66));
            System.out.println(String.format("  %-40s %20s", "TOTAL HT:",
                draft.getFormattedTotalHT()));
            System.out.println(String.format("  %-40s %20s",
                "TVA (" + String.format("%.0f", draft.getTvaRate()) + "%):",
                draft.getFormattedTVA()));
            System.out.println("  " + "═".repeat(66));
            System.out.println(String.format("  %-40s %20s", "TOTAL TTC:",
                draft.getFormattedTotalTTC()));
        } else {
            System.out.println("  (Prix à définir)");
        }

        // Analyse du budget
        if (draft.getClientBudget() != null) {
            System.out.println("\n  ANALYSE DU BUDGET:");
            System.out.println(String.format("    Budget client: %,.2f € HT", draft.getClientBudget()));
            System.out.println("    Écart: " + draft.getFormattedBudgetDelta());
            System.out.println("    Budget respecté: " +
                (draft.isBudgetRespected() ? "✓ OUI" : "✗ NON"));
        }

        // Livraison
        System.out.println("\n  LIVRAISON:");
        if (draft.getRequestedDeliveryDate() != null) {
            System.out.println("    Date demandée: " + draft.getRequestedDeliveryDate());
        }
        System.out.println("    Date estimée: " + draft.getEstimatedDeliveryDate());
        System.out.println("    Délai: " + draft.getDeliveryDays() + " jours ouvrés");
        System.out.println("    Installation: " +
            (draft.isInstallationIncluded() ? "Incluse" : "Non incluse"));

        // Conditions
        System.out.println("\n  CONDITIONS:");
        System.out.println("    Paiement: " + draft.getPaymentTerms());
        System.out.println("    Garantie: " + draft.getWarranty());

        if (!draft.getSpecialConditions().isEmpty()) {
            System.out.println("    Conditions particulières:");
            for (String condition : draft.getSpecialConditions()) {
                System.out.println("      • " + condition);
            }
        }

        // Actions requises
        if (draft.hasRequiredActions()) {
            System.out.println("\n  ⚠ ACTIONS REQUISES AVANT FINALISATION:");
            for (String action : draft.getRequiredActions()) {
                System.out.println("    □ " + action);
            }
        }

        // Recommandations
        if (!draft.getRecommendations().isEmpty()) {
            System.out.println("\n  💡 RECOMMANDATIONS POUR LE COMMERCIAL:");
            for (String reco : draft.getRecommendations()) {
                System.out.println("    → " + reco);
            }
        }

        // Avertissements
        if (draft.hasWarnings()) {
            System.out.println("\n  ⚠ AVERTISSEMENTS:");
            for (String warning : draft.getWarnings()) {
                System.out.println("    ⚠ " + warning);
            }
        }

        // Incohérences
        if (draft.hasInconsistencies()) {
            System.out.println("\n  ✗ INCOHÉRENCES DÉTECTÉES:");
            for (String inc : draft.getInconsistencies()) {
                System.out.println("    ✗ " + inc);
            }
        }

        // Score final
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  ÉVALUATION DU BROUILLON");
        System.out.println("═".repeat(70));

        String confBar = getConfidenceBar(draft.getConfidence(), 30);
        String level = getConfidenceLevel(draft.getConfidence());

        System.out.println("  Confiance: " + confBar);
        System.out.println("  Score: " + String.format("%.0f%%", draft.getConfidence() * 100) +
                          " - " + level);
        System.out.println("  Complet: " + (draft.isComplete() ? "✓ OUI" : "✗ NON"));

        // Statistiques
        DraftQuote.DraftStats stats = draft.getStats();
        if (stats != null) {
            System.out.println("\n  STATISTIQUES:");
            System.out.println(String.format("    Articles: %d (complets: %d, à compléter: %d)",
                stats.totalItems, stats.completeItems, stats.incompleteItems));
            System.out.println(String.format("    Prix définis: %d/%d",
                stats.itemsWithPrice, stats.totalItems));
            System.out.println(String.format("    Catégories: %d", stats.sectionsCount));
            System.out.println(String.format("    Temps de génération: %d ms", stats.generationTimeMs));
        }
    }

    /**
     * Affiche une ligne de devis.
     */
    private static void printQuoteItem(QuoteItem item, int lineNum) {
        String statusIcon = getStatusIcon(item.getStatus());

        System.out.println("\n    " + lineNum + ". " + statusIcon + " " + item.getReference());
        System.out.println("       " + item.getQuantity() + " x " + item.getDesignation());

        if (item.getDescription() != null) {
            System.out.println("       " + item.getDescription());
        }

        System.out.println("       Gamme: " + item.getPriceRange().getLabel());
        System.out.println("       Prix unitaire: " + item.getFormattedUnitPriceHT());
        System.out.println("       Total ligne: " + item.getFormattedTotalPriceHT());

        if (item.hasWarnings()) {
            for (String warn : item.getWarnings()) {
                System.out.println("       ⚠ " + warn);
            }
        }

        if (item.hasOptions() && !item.getOptions().isEmpty()) {
            System.out.println("       Options disponibles: " +
                String.join(", ", item.getOptions().subList(0, Math.min(2, item.getOptions().size()))) +
                (item.getOptions().size() > 2 ? "..." : ""));
        }
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
    private static String getStatusIcon(QuoteItem.LineStatus status) {
        return switch (status) {
            case COMPLETE -> "✓";
            case A_VALIDER -> "○";
            case A_COMPLETER -> "◐";
            case OPTIONNEL -> "◇";
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
