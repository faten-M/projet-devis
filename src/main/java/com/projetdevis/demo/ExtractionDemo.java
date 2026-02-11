package com.projetdevis.demo;

import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import com.projetdevis.service.EmailCleanerService;
import com.projetdevis.service.ExtractionService;

/**
 * Classe de démonstration du service d'extraction BMAD.
 * Montre comment utiliser ExtractionService avec différents types d'emails.
 *
 * Pipeline complet :
 * 1. Email brut → EmailCleanerService → Email nettoyé
 * 2. Email nettoyé → ExtractionService → ExtractedInfo
 *
 * @author BMAD Pipeline - Étape 3
 * @version 1.0
 */
public class ExtractionDemo {

    public static void main(String[] args) {
        EmailCleanerService cleaner = new EmailCleanerService();
        ExtractionService extractor = new ExtractionService();

        System.out.println("=".repeat(80));
        System.out.println("    DÉMONSTRATION - ExtractionService (Pipeline BMAD - Étape 3)");
        System.out.println("=".repeat(80));

        // === EXEMPLE 1 : Email de demande de mobilier complet ===
        testCase1_DemandeComplete(cleaner, extractor);

        // === EXEMPLE 2 : Email avec budget et urgence ===
        testCase2_BudgetEtUrgence(cleaner, extractor);

        // === EXEMPLE 3 : Email avec liste détaillée ===
        testCase3_ListeDetaillee(cleaner, extractor);

        // === EXEMPLE 4 : Email minimaliste ===
        testCase4_EmailMinimaliste(cleaner, extractor);

        // === EXEMPLE 5 : Email avec contraintes spécifiques ===
        testCase5_ContraintesSpecifiques(cleaner, extractor);

        // === EXEMPLE 6 : Email professionnel complet ===
        testCase6_EmailProfessionnel(cleaner, extractor);
    }

    private static void testCase1_DemandeComplete(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 1 : Demande de mobilier de bureau complète");
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
            Directeur Administratif
            """;

        runExtraction(email, cleaner, extractor);
    }

    private static void testCase2_BudgetEtUrgence(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 2 : Email avec budget et urgence");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            URGENT - nous avons besoin rapidement d'un devis pour :
            - 30 chaises visiteurs
            - 3 tables de réunion 8 personnes

            Notre enveloppe maximum est de 8000 euros TTC.

            Livraison impérative fin janvier.

            Merci de traiter cette demande en urgence.

            Bien cordialement,
            Marie MARTIN
            """;

        runExtraction(email, cleaner, extractor);
    }

    private static void testCase3_ListeDetaillee(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 3 : Liste détaillée avec spécifications");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            Voici notre demande détaillée pour l'équipement de 3 salles :

            SALLE DE RÉUNION PRINCIPALE :
            - 1 table de réunion ovale 12 personnes en noyer
            - 12 fauteuils de conférence cuir noir avec accoudoirs

            OPEN SPACE :
            - 20 postes de travail complets (bureau + chaise + caisson)
            - Bureaux : 140x70cm, blanc, pieds métal
            - Chaises : ergonomiques, mesh gris, réglables en hauteur

            ESPACE DÉTENTE :
            - 2 canapés 3 places en tissu bleu
            - 4 poufs modulables
            - 2 tables basses rondes 80cm

            Budget prévu : 65k€ HT
            Délai : livraison et installation avant le 28 février 2024

            Note : montage inclus obligatoire

            Merci de votre proposition.
            """;

        runExtraction(email, cleaner, extractor);
    }

    private static void testCase4_EmailMinimaliste(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 4 : Email minimaliste");
        System.out.println("-".repeat(80));

        String email = """
            salut

            g besoin de 20 chaises noires pour le bureau svp

            merci
            """;

        runExtraction(email, cleaner, extractor);
    }

    private static void testCase5_ContraintesSpecifiques(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 5 : Email avec contraintes spécifiques");
        System.out.println("-".repeat(80));

        String email = """
            Bonjour,

            Nous recherchons pour notre nouveau showroom :
            - 8 bureaux design 180x90cm en verre trempé
            - 8 fauteuils direction cuir blanc
            - 4 étagères murales en acier noir

            Contraintes importantes :
            - Livraison gratuite obligatoire
            - Garantie minimum 5 ans
            - Possibilité de visite showroom avant commande
            - Paiement à 30 jours fin de mois

            Attention : les couleurs doivent être uniformes (noir et blanc uniquement)

            Budget max : 25 000€ HT
            Date souhaitée : 10 avril 2024

            Merci d'avance.
            """;

        runExtraction(email, cleaner, extractor);
    }

    private static void testCase6_EmailProfessionnel(EmailCleanerService cleaner, ExtractionService extractor) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("EXEMPLE 6 : Email professionnel complet (cas réaliste)");
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

            Nous restons disponibles pour une visite de vos locaux si nécessaire,
            ou pour vous recevoir dans nos bureaux pour visualiser l'espace.

            Dans l'attente de votre proposition détaillée.

            Cordialement,

            Sophie BERNARD
            Responsable Services Généraux
            TechCorp Solutions
            01 23 45 67 89
            s.bernard@techcorp.fr
            """;

        runExtraction(email, cleaner, extractor);
    }

    /**
     * Exécute le pipeline complet : nettoyage + extraction + affichage.
     */
    private static void runExtraction(String rawEmail, EmailCleanerService cleaner, ExtractionService extractor) {
        // Étape 1 : Nettoyage
        String cleanedEmail = cleaner.clean(rawEmail);

        // Étape 2 : Extraction
        ExtractedInfo info = extractor.extract(cleanedEmail);

        // Affichage des résultats
        printResults(rawEmail, cleanedEmail, info, cleaner, extractor);
    }

    /**
     * Affiche les résultats de l'extraction de manière détaillée.
     */
    private static void printResults(String original, String cleaned,
                                     ExtractedInfo info,
                                     EmailCleanerService cleaner,
                                     ExtractionService extractor) {

        // Email original
        System.out.println("\n[EMAIL ORIGINAL]");
        System.out.println(truncate(original, 500));

        // Email nettoyé
        System.out.println("\n[EMAIL NETTOYÉ]");
        System.out.println(cleaned.isEmpty() ? "(vide)" : truncate(cleaned, 400));

        // Statistiques de nettoyage
        System.out.println("\n" + cleaner.getCleaningStats(original, cleaned));

        // Résultats de l'extraction
        System.out.println("\n" + "~".repeat(40));
        System.out.println("RÉSULTATS DE L'EXTRACTION");
        System.out.println("~".repeat(40));

        // Articles extraits
        System.out.println("\n[ARTICLES EXTRAITS] (" + info.getItemCount() + ")");
        if (info.hasItems()) {
            int i = 1;
            for (ItemRequest item : info.getItems()) {
                System.out.println("\n  Article #" + i + ":");
                System.out.println("    Produit    : " + item.getProduct());
                System.out.println("    Quantité   : " + (item.getQuantity() != null ? item.getQuantity() : "?"));

                if (item.getColor() != null) {
                    System.out.println("    Couleur    : " + item.getColor());
                }
                if (item.getMaterial() != null) {
                    System.out.println("    Matériau   : " + item.getMaterial());
                }
                if (item.getDimensions() != null) {
                    System.out.println("    Dimensions : " + item.getDimensions());
                }
                if (item.hasCharacteristics()) {
                    System.out.println("    Caract.    : " + String.join(", ", item.getCharacteristics()));
                }
                i++;
            }
            System.out.println("\n    → Total : " + info.getTotalQuantity() + " unités");
        } else {
            System.out.println("  (Aucun article détecté)");
        }

        // Budget
        System.out.println("\n[BUDGET]");
        if (info.hasBudget()) {
            System.out.println("  Montant  : " + info.getFormattedBudget());
            if (info.getBudgetRaw() != null) {
                System.out.println("  Brut     : \"" + info.getBudgetRaw() + "\"");
            }
        } else {
            System.out.println("  (Non spécifié)");
        }

        // Date de livraison
        System.out.println("\n[DATE DE LIVRAISON]");
        if (info.hasDeliveryDate()) {
            if (info.getDeliveryDate() != null) {
                System.out.println("  Date     : " + info.getDeliveryDate());
            }
            if (info.getDeliveryDateRaw() != null) {
                System.out.println("  Brut     : \"" + info.getDeliveryDateRaw() + "\"");
            }
        } else {
            System.out.println("  (Non spécifiée)");
        }

        // Urgence
        System.out.println("\n[URGENCE]");
        System.out.println("  Niveau   : " + info.getUrgency());

        // Notes additionnelles
        System.out.println("\n[NOTES ADDITIONNELLES]");
        if (!info.getAdditionalNotes().isEmpty()) {
            for (String note : info.getAdditionalNotes()) {
                System.out.println("  • " + note);
            }
        } else {
            System.out.println("  (Aucune)");
        }

        // Confiance
        System.out.println("\n[CONFIANCE]");
        String confidenceBar = getConfidenceBar(info.getConfidence());
        System.out.println("  Score    : " + String.format("%.0f%%", info.getConfidence() * 100));
        System.out.println("  Barre    : " + confidenceBar);
        System.out.println("  Fiable   : " + (info.isReliable() ? "OUI" : "NON"));

        // Statistiques d'extraction
        System.out.println("\n" + extractor.getExtractionStats(info));
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
     * Génère une barre de progression pour la confiance.
     */
    private static String getConfidenceBar(double confidence) {
        int filled = (int) (confidence * 20);
        int empty = 20 - filled;

        String bar = "█".repeat(filled) + "░".repeat(empty);

        // Couleur indicative (textuelle)
        String level;
        if (confidence >= 0.8) {
            level = "EXCELLENT";
        } else if (confidence >= 0.6) {
            level = "BON";
        } else if (confidence >= 0.4) {
            level = "MOYEN";
        } else {
            level = "FAIBLE";
        }

        return "[" + bar + "] " + level;
    }
}
