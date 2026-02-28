package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.List;

/**
 * Service de nettoyage des emails par IA (OpenAI).
 *
 * Remplace entièrement les regex de EmailCleanerService par des appels au
 * modèle gpt-5-nano. Le prompt instruit le modèle de retourner uniquement
 * le contenu utile de l'email, sans aucun ajout ni reformulation.
 *
 * Utilisation :
 *   EmailCleanerIA cleaner = new EmailCleanerIA();
 *   String cleaned = cleaner.clean(rawEmail);
 *   System.out.println(cleaner.getCleaningStats(rawEmail, cleaned));
 *
 * Prérequis :
 *   Variable d'environnement OPENAI_API_KEY définie.
 */
public class EmailCleanerIA {

    // Modèle cible
    private static final String MODEL = "gpt-5-nano";

    // Prompt système : instructions strictes pour le modèle
    private static final String SYSTEM_PROMPT =
        "Tu es un assistant spécialisé dans le nettoyage d'emails professionnels. " +
        "Ton rôle est d'extraire uniquement le contenu utile et informatif d'un email. " +
        "\n\nTu dois SUPPRIMER sans exception :\n" +
        "1. Signatures (Cordialement, Best regards, coordonnées, logo texte, --)\n" +
        "2. Répétitions : phrases identiques ou quasi-identiques présentes plusieurs fois\n" +
        "3. Phrases inutiles : salutations (Bonjour, Hello), formules de politesse " +
           "(J'espère que vous allez bien, N'hésitez pas à me contacter...)\n" +
        "4. Historique de conversation : tout ce qui suit 'Le ... a écrit :', " +
           "'On ... wrote:', 'From:', '--- Original Message ---', les lignes commençant par >\n" +
        "5. Mentions de pièces jointes : [image:...], [cid:...], [Voir la pièce jointe], " +
           "[See attached], [Attachment:...], URLs d'images\n" +
        "6. Mise en forme parasite : caractères invisibles, espaces multiples, " +
           "sauts de ligne excessifs, tabulations inutiles\n" +
        "\nTu dois CONSERVER :\n" +
        "- La demande réelle du client\n" +
        "- Les informations concrètes : produits, quantités, dates, prix, références\n" +
        "- Les questions précises\n" +
        "- Les contraintes ou exigences métier\n" +
        "\nRègles strictes :\n" +
        "- Retourne UNIQUEMENT le texte nettoyé, sans commentaire ni explication\n" +
        "- Ne reformule PAS le contenu conservé, copie-le tel quel\n" +
        "- Si après nettoyage il ne reste rien d'utile, retourne la chaîne vide\n" +
        "- Conserve les sauts de ligne entre les idées distinctes (max 1 ligne vide entre paragraphes)";

    // Client OpenAI — initialisé une seule fois (thread-safe en lecture)
    private final OpenAIClient client;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    /**
     * Constructeur par défaut.
     * Lit la clé API dans la variable d'environnement OPENAI_API_KEY.
     *
     * @throws IllegalStateException si la variable OPENAI_API_KEY est absente
     */
    public EmailCleanerIA() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Variable d'environnement OPENAI_API_KEY manquante ou vide."
            );
        }
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Constructeur avec injection du client (utile pour les tests unitaires).
     *
     * @param client Instance OpenAIClient à utiliser
     */
    public EmailCleanerIA(OpenAIClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Le client OpenAI ne peut pas être null.");
        }
        this.client = client;
    }

    // -------------------------------------------------------------------------
    // Méthode principale
    // -------------------------------------------------------------------------

    /**
     * Nettoie un email brut en utilisant l'IA.
     *
     * Le modèle supprime : signatures, répétitions, phrases inutiles,
     * historiques de conversation, mentions de pièces jointes, et tout
     * bruit de mise en forme. Il conserve uniquement le contenu informatif.
     *
     * @param email Email brut à nettoyer (peut être null, vide ou blanc)
     * @return Contenu utile de l'email, ou chaîne vide si rien d'utile
     * @throws RuntimeException si l'appel à l'API OpenAI échoue
     */
    public String clean(String email) {
        // Cas limites : null, vide, blanc
        if (email == null || email.isBlank()) {
            return "";
        }

        // Construction du prompt utilisateur
        String userPrompt = "Voici l'email à nettoyer :\n\n" + email;

        // Construction des paramètres de la requête
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage(SYSTEM_PROMPT)
                .addUserMessage(userPrompt)
                .build();

        // Appel à l'API
        ChatCompletion completion = client.chat().completions().create(params);

        // Extraction de la réponse
        String cleaned = completion.choices().get(0)
                .message()
                .content()
                .orElse("")
                .trim();

        return cleaned;
    }

    // -------------------------------------------------------------------------
    // Statistiques
    // -------------------------------------------------------------------------

    /**
     * Calcule et retourne des statistiques de nettoyage comparant l'email
     * original à l'email nettoyé.
     *
     * Exemples de métriques retournées :
     * - Taille originale et taille nettoyée (en caractères)
     * - Quantité supprimée (valeur absolue et pourcentage)
     * - Nombre de lignes avant / après
     *
     * @param original Email brut (avant nettoyage)
     * @param cleaned  Email nettoyé (après appel à {@link #clean(String)})
     * @return Rapport de statistiques sous forme de chaîne lisible
     */
    public String getCleaningStats(String original, String cleaned) {
        int originalChars = (original != null) ? original.length() : 0;
        int cleanedChars  = (cleaned  != null) ? cleaned.length()  : 0;
        int removedChars  = originalChars - cleanedChars;
        double removedPct = (originalChars > 0)
                ? (removedChars * 100.0 / originalChars)
                : 0.0;

        int originalLines = (original != null && !original.isBlank())
                ? original.split("\n", -1).length : 0;
        int cleanedLines  = (cleaned  != null && !cleaned.isBlank())
                ? cleaned.split("\n", -1).length  : 0;

        return String.format(
            "=== Statistiques de nettoyage (EmailCleanerIA) ===%n" +
            "Modèle utilisé      : %s%n" +
            "Taille originale    : %d caractères  (%d lignes)%n" +
            "Taille nettoyée     : %d caractères  (%d lignes)%n" +
            "Contenu supprimé    : %d caractères  (%.1f%%)%n" +
            "=================================================",
            MODEL,
            originalChars, originalLines,
            cleanedChars,  cleanedLines,
            removedChars,  removedPct
        );
    }
}
