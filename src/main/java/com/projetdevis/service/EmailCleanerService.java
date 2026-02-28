package com.projetdevis.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

/**
 * Service de nettoyage des emails pour extraction du contenu utile.
 * Pipeline : signature -> replies -> images -> phrases inutiles -> extraction
 */
public class EmailCleanerService {

    // Client IA désactivé - utiliser EmailCleanerIA à la place pour le nettoyage par IA
    // private final OpenAiClient ai = new OpenAiClient();


    // === PATTERNS REGEX ===

    // Signatures : détecte les marqueurs courants de signature
    private static final Pattern SIGNATURE_PATTERNS = Pattern.compile(
        "(?m)^\\s*(" +
        "--\\s*$|" +                                    // -- seul sur une ligne
        "_{3,}|" +                                      // ___ ou plus
        "-{3,}|" +                                      // --- ou plus
        "Cordialement,?|" +                             // Cordialement
        "Bien cordialement,?|" +
        "Cdlt,?|" +
        "Cdt,?|" +
        "Salutations,?|" +
        "Best regards,?|" +
        "Kind regards,?|" +
        "Regards,?|" +
        "Thanks,?|" +
        "Thank you,?|" +
        "Merci,?|" +
        "Envoyé depuis mon iPhone|" +
        "Envoyé depuis mon iPad|" +
        "Sent from my iPhone|" +
        "Sent from my iPad|" +
        "Get Outlook for|" +
        "Téléchargez Outlook" +
        ").*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Réponses précédentes : détecte le début des messages cités
    private static final Pattern REPLY_PATTERNS = Pattern.compile(
        "(?m)^\\s*(" +
        "Le .+ a écrit\\s*:|" +                         // Le 12/01/2024 à 10:00, Jean a écrit :
        "On .+ wrote\\s*:|" +                           // On Mon, Jan 12, 2024, John wrote:
        "From:\\s*.+|" +                                // From: sender@email.com
        "De\\s*:\\s*.+|" +                              // De : expéditeur@email.com
        "-{3,}\\s*Original Message\\s*-{3,}|" +         // --- Original Message ---
        "-{3,}\\s*Message d'origine\\s*-{3,}|" +
        ">{1,}\\s*|" +                                  // > texte cité
        "\\|{1,}\\s*" +                                 // | texte cité (certains clients)
        ").*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Images et pièces jointes : références aux images inline ou attachées
    private static final Pattern IMAGE_PATTERNS = Pattern.compile(
        "(?i)(" +
        "\\[image\\s*:?[^\\]]*\\]|" +                   // [image: description]
        "\\[cid:[^\\]]+\\]|" +                          // [cid:image001.png]
        "<image\\d*>|" +                                // <image001>
        "\\[Voir la pièce jointe[^\\]]*\\]|" +          // [Voir la pièce jointe]
        "\\[See attached[^\\]]*\\]|" +                  // [See attached file]
        "\\[Attachment[^\\]]*\\]|" +                    // [Attachment: file.png]
        "\\[Pièce jointe[^\\]]*\\]|" +
        "<[^>]*\\.(?:png|jpg|jpeg|gif|bmp|svg)[^>]*>|" + // <image.png>
        "https?://[^\\s]+\\.(?:png|jpg|jpeg|gif|bmp|svg)(?:\\?[^\\s]*)?" + // URLs d'images
        ")"
    );

    // Phrases inutiles : formules de politesse, phrases génériques
    private static final List<Pattern> USELESS_PATTERNS = Arrays.asList(
        // Salutations
        Pattern.compile("(?im)^\\s*Bonjour[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Bonsoir[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Hello[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Hi[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Dear[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Cher[^,]*,?\\s*$"),
        Pattern.compile("(?im)^\\s*Chère[^,]*,?\\s*$"),

        // Formules de politesse génériques
        Pattern.compile("(?im)^\\s*J'espère que (vous allez|tu vas|ce mail vous trouve) bien[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*I hope (this email finds you well|you are doing well)[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*Comment (allez-vous|vas-tu)\\s*\\?\\s*$"),

        // Phrases de transition vides
        Pattern.compile("(?im)^\\s*Suite à notre (conversation|échange|discussion)[^.]*\\.?\\s*$"),
        Pattern.compile("(?im)^\\s*Following (up on|our conversation)[^.]*\\.?\\s*$"),
        Pattern.compile("(?im)^\\s*Pour faire suite[^.]*\\.?\\s*$"),

        // Remerciements génériques en début
        Pattern.compile("(?im)^\\s*Merci (pour votre|de votre) (réponse|retour|mail)[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*Thank you for (your|getting back)[^.]*[.!]?\\s*$"),

        // Phrases de clôture
        Pattern.compile("(?im)^\\s*N'hésitez pas (à|si)[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*(Feel free to|Don't hesitate to)[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*Je reste (à votre disposition|disponible)[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*I (remain|am) at your disposal[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*Dans l'attente de[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*Looking forward to[^.]*[.!]?\\s*$"),
        Pattern.compile("(?im)^\\s*En vous remerciant[^.]*[.!]?\\s*$"),

        // Lignes vides multiples (seront normalisées)
        Pattern.compile("(?m)^\\s*$")
    );

    // Caractères de mise en forme bizarres
    private static final Pattern FORMATTING_NOISE = Pattern.compile(
        "[\\u200B\\u200C\\u200D\\uFEFF]|" +  // Zero-width chars
        "\\u00A0{2,}|" +                      // Multiple non-breaking spaces
        "\\t{2,}|" +                          // Multiple tabs
        " {3,}"                               // Plus de 2 espaces consécutifs
    );

    // === MÉTHODES DE NETTOYAGE ===

    /**
     * 1. Supprime la signature de l'email.
     *
     * Logique : Détecte les marqueurs courants de signature (---, Cordialement,
     * Envoyé depuis mon iPhone, etc.) et supprime tout ce qui suit.
     * Gère les signatures longues avec coordonnées, logos textuels, etc.
     *
     * @param text Texte brut de l'email
     * @return Texte sans la signature
     */
    public String removeSignature(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Chercher le premier marqueur de signature
        Matcher matcher = SIGNATURE_PATTERNS.matcher(text);
        if (matcher.find()) {
            // Garder uniquement ce qui précède la signature
            return text.substring(0, matcher.start()).trim();
        }

        return text;
    }

    /**
     * 2. Supprime les réponses précédentes (historique de conversation).
     *
     * Logique : Détecte les marqueurs de citation (>, "Le ... a écrit :",
     * "From:", "Original Message", etc.) et supprime tout le contenu cité.
     *
     * @param text Texte de l'email
     * @return Texte sans les réponses précédentes
     */
    public String removePreviousReplies(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Chercher le début des réponses précédentes
        Matcher matcher = REPLY_PATTERNS.matcher(text);
        if (matcher.find()) {
            // Garder uniquement ce qui précède les citations
            return text.substring(0, matcher.start()).trim();
        }

        return text;
    }

    /**
     * 3. Supprime les références aux images et pièces jointes.
     *
     * Logique : Détecte les placeholders d'images ([image:...], [cid:...],
     * URLs d'images, etc.) et les supprime car non exploitables en texte.
     *
     * @param text Texte de l'email
     * @return Texte sans les références aux images
     */
    public String removeImages(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return IMAGE_PATTERNS.matcher(text).replaceAll("").trim();
    }

    /**
     * 4. Supprime les phrases inutiles (politesses, formules génériques).
     *
     * Logique : Identifie et supprime les salutations, formules de politesse,
     * phrases de transition vides qui n'apportent pas d'information utile
     * pour l'analyse de la demande.
     *
     * @param text Texte de l'email
     * @return Texte sans les phrases inutiles
     */
    public String removeUselessSentences(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;

        // Appliquer chaque pattern de phrase inutile
        for (Pattern pattern : USELESS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }

        return result.trim();
    }

    /**
     * 5. Extrait et normalise le contenu utile.
     *
     * Logique : Nettoie la mise en forme (espaces multiples, tabs, caractères
     * invisibles), normalise les sauts de ligne, et s'assure que le texte
     * final est propre et lisible.
     *
     * @param text Texte de l'email
     * @return Texte propre et normalisé
     */
    public String extractUsefulContent(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Supprimer les caractères de formatage parasites
        String result = FORMATTING_NOISE.matcher(text).replaceAll(" ");

        // Normaliser les sauts de ligne (max 2 consécutifs)
        result = result.replaceAll("(\\r?\\n){3,}", "\n\n");

        // Supprimer les espaces en début/fin de ligne
        result = result.replaceAll("(?m)^[ \\t]+|[ \\t]+$", "");

        // Normaliser les espaces
        result = result.replaceAll("[ \\t]+", " ");

        // Supprimer les lignes vides au début et à la fin
        result = result.replaceAll("^(\\s*\\n)+|\\n(\\s*\\n)*$", "");

        return result.trim();
    }

    /**
     * 6. Méthode principale : exécute le pipeline complet de nettoyage.
     *
     * Ordre du pipeline :
     * 1. removeSignature - Enlever la signature d'abord (souvent en fin)
     * 2. removePreviousReplies - Enlever l'historique de conversation
     * 3. removeImages - Enlever les références aux images
     * 4. removeUselessSentences - Enlever les phrases génériques
     * 5. extractUsefulContent - Normaliser et nettoyer le reste
     *
     * @param rawEmail Email brut à nettoyer
     * @return Email nettoyé, prêt pour analyse
     */
    public String clean(String rawEmail) {
        if (rawEmail == null || rawEmail.isEmpty()) {
            return "";
        }

        String result = rawEmail;

        // Pipeline de nettoyage dans l'ordre
        result = removeSignature(result);
        result = removePreviousReplies(result);
        result = removeImages(result);
        result = removeUselessSentences(result);
        result = extractUsefulContent(result);

        return result;
    }

    /**
     * Méthode utilitaire pour obtenir des statistiques de nettoyage.
     * Utile pour le debug et comprendre ce qui a été supprimé.
     *
     * @param rawEmail Email brut
     * @param cleanedEmail Email nettoyé
     * @return Statistiques sous forme de texte
     */
    public String getCleaningStats(String rawEmail, String cleanedEmail) {
        int originalLength = rawEmail != null ? rawEmail.length() : 0;
        int cleanedLength = cleanedEmail != null ? cleanedEmail.length() : 0;
        int removed = originalLength - cleanedLength;
        double percentage = originalLength > 0 ? (removed * 100.0 / originalLength) : 0;

        return String.format(
            "Statistiques de nettoyage:\n" +
            "- Taille originale: %d caractères\n" +
            "- Taille nettoyée: %d caractères\n" +
            "- Contenu supprimé: %d caractères (%.1f%%)",
            originalLength, cleanedLength, removed, percentage
        );
    }
}
