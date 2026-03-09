package com.projetdevis.demo;

import com.projetdevis.service.EmailCleanerService;

/**
 * Classe de démonstration du service de nettoyage d'emails.
 * Montre différents cas d'usage avec des exemples réalistes.
 */
public class EmailCleanerDemo {

    public static void main(String[] args) {
        EmailCleanerService cleaner = new EmailCleanerService();

        System.out.println("=".repeat(70));
        System.out.println("    DÉMONSTRATION - EmailCleanerService");
        System.out.println("=".repeat(70));

        // === EXEMPLE 1 : Email avec signature longue ===
        testCase1_SignatureLongue(cleaner);

        // === EXEMPLE 2 : Email avec historique de conversation ===
        testCase2_HistoriqueConversation(cleaner);

        // === EXEMPLE 3 : Email avec images et pièces jointes ===
        testCase3_ImagesEtPiecesJointes(cleaner);

        // === EXEMPLE 4 : Email avec phrases inutiles ===
        testCase4_PhrasesInutiles(cleaner);

        // === EXEMPLE 5 : Email complet réaliste ===
        testCase5_EmailComplet(cleaner);

        // === EXEMPLE 6 : Email mal formaté ===
        testCase6_EmailMalFormate(cleaner);
    }

    private static void testCase1_SignatureLongue(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 1 : Email avec signature longue");
        System.out.println("-".repeat(70));

        String email = """
            Je souhaite un devis pour 50 chaises de bureau ergonomiques
            et 25 bureaux assis-debout.

            Livraison souhaitée pour le 15 mars.

            Cordialement,

            Jean-Pierre DUPONT
            Responsable Achats
            ACME Corporation
            Tél: 01 23 45 67 89
            Fax: 01 23 45 67 90
            Email: jp.dupont@acme.fr

            123 Avenue des Champs-Élysées
            75008 Paris

            ---
            Ce message est confidentiel. Si vous n'êtes pas le destinataire...
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void testCase2_HistoriqueConversation(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 2 : Email avec historique de conversation");
        System.out.println("-".repeat(70));

        String email = """
            Oui parfait, ajoutez aussi 10 lampes de bureau LED.

            Le 12/01/2024 à 14:32, Service Commercial <commercial@fournisseur.fr> a écrit :

            > Bonjour,
            >
            > Nous avons bien reçu votre demande pour les chaises.
            > Souhaitez-vous ajouter d'autres articles ?
            >
            > Cordialement,
            > L'équipe commerciale
            >
            > Le 11/01/2024 à 09:15, client@entreprise.fr a écrit :
            >> Je voudrais un devis pour des chaises de bureau.
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void testCase3_ImagesEtPiecesJointes(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 3 : Email avec images et pièces jointes");
        System.out.println("-".repeat(70));

        String email = """
            Voici le modèle de bureau souhaité :

            [image: bureau-modele.png]

            Je voudrais 15 unités de ce modèle en chêne clair.

            [cid:image001.jpg@01D9F2B3.4C5D6E70]

            Voir aussi le plan de l'espace en pièce jointe.

            [Voir la pièce jointe: plan-bureau.pdf]

            Dimensions souhaitées : 160x80cm
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void testCase4_PhrasesInutiles(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 4 : Email avec phrases inutiles");
        System.out.println("-".repeat(70));

        String email = """
            Bonjour,

            J'espère que vous allez bien.

            Suite à notre conversation téléphonique de ce matin.

            Je souhaiterais obtenir un devis pour :
            - 30 fauteuils de direction
            - 30 caissons 3 tiroirs

            Merci de votre retour.

            N'hésitez pas à me contacter si vous avez des questions.

            Je reste à votre disposition pour tout complément d'information.

            Dans l'attente de votre réponse.
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void testCase5_EmailComplet(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 5 : Email complet réaliste (tous les problèmes)");
        System.out.println("-".repeat(70));

        String email = """
            Bonjour Madame, Monsieur,

            J'espère que ce mail vous trouve bien.

            Suite à notre échange de la semaine dernière, je reviens vers vous
            pour notre projet d'aménagement de bureaux.

            Nous aurions besoin de :
            - 45 postes de travail complets (bureau + chaise + caisson)
            - 3 salles de réunion équipées (table 10 personnes + chaises)
            - 1 espace détente (canapés, tables basses, poufs)

            [image: plan-etage.png]

            Budget indicatif : 80 000€ HT
            Délai souhaité : fin février

            Voir le plan en pièce jointe pour les dimensions exactes.

            [Voir la pièce jointe: specifications.xlsx]

            Merci de votre retour rapide.

            N'hésitez pas si vous avez besoin de précisions.

            Cordialement,

            Marie MARTIN
            Directrice Administrative
            StartUp Innovante SAS
            Tel: 06 12 34 56 78
            www.startup-innovante.fr

            ________________________________
            De : Commercial <contact@mobilier-pro.fr>
            Envoyé : lundi 8 janvier 2024 10:00
            À : Marie Martin <m.martin@startup.fr>
            Objet : RE: Demande de devis mobilier

            > Bonjour,
            >
            > Merci pour votre intérêt.
            > Pourriez-vous nous préciser vos besoins ?
            >
            > Cordialement,
            > L'équipe commerciale
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void testCase6_EmailMalFormate(EmailCleanerService cleaner) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println("EXEMPLE 6 : Email mal formaté");
        System.out.println("-".repeat(70));

        String email = """
            salut!!!



            g besoin de chaises pr le bureau    urgent svp

            genre 20 chaises          noires


            et ossi des tables rondes 4 personnes  on en veut 5




            merci!!!



            envoyé de mon iphone
            """;

        printResult(email, cleaner.clean(email), cleaner);
    }

    private static void printResult(String original, String cleaned, EmailCleanerService cleaner) {
        System.out.println("\n[ORIGINAL]");
        System.out.println(original);

        System.out.println("\n[NETTOYÉ]");
        System.out.println(cleaned.isEmpty() ? "(vide)" : cleaned);

        System.out.println("\n" + cleaner.getCleaningStats(original, cleaned));
    }
}
