package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatCompletionUserMessageParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Client OpenAI utilisant le SDK officiel openai-java 4.23.0.
 * 
 * Permet d'appeler l'API OpenAI pour deux opérations principales :
 * - Retirer les signatures des emails
 * - Supprimer les répétitions dans un texte
 * 
 * Configuration requise :
 * - Variable d'environnement OPENAI_API_KEY définie
 * - Dépendance openai-java 4.23.0 dans pom.xml
 * 
 * @author Projet Devis
 * @version 1.0
 */
public class OpenAiClient {

    private static final String MODEL = "gpt-5-nano";
    
    private final OpenAIClient client;

    /**
     * Constructeur : initialise le client OpenAI avec la clé API.
     * 
     * La clé API est lue depuis la variable d'environnement OPENAI_API_KEY.
     * Si elle n'est pas définie, une exception est levée.
     * 
     * @throws IllegalStateException si OPENAI_API_KEY n'est pas définie
     */
    public OpenAiClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Erreur : la variable d'environnement OPENAI_API_KEY n'est pas définie. " +
                "Veuillez la configurer avant de lancer l'application."
            );
        }

        // Initialiser le client OpenAI avec le builder OpenAIOkHttpClient
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Supprime la signature d'un email.
     * 
     * La signature est détectée et supprimée de manière intelligente via OpenAI.
     * Le reste du contenu de l'email est conservé intact.
     * 
     * @param email Texte brut de l'email (potentiellement avec signature)
     * @return Texte de l'email sans la signature
     * @throws RuntimeException si l'appel à l'API échoue
     */
    public String retirerSignature(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }

        String prompt = "Retire uniquement la signature de cet email et retourne le texte nettoyé sans la signature. " +
                "Ne modifie rien d'autre dans le contenu. Retourne UNIQUEMENT le texte nettoyé, sans commentaire.\n\n" +
                "Email :\n" + email;

        return callOpenAiApi(prompt, "Suppression de signature");
    }

    /**
     * Supprime les répétitions dans un texte.
     * 
     * Détecte et supprime :
     * - Les lignes identiques
     * - Les phrases répétées
     * - Les variations minimes (ponctuation, espaces, casse)
     * 
     * @param texte Texte potentiellement contenant des répétitions
     * @return Texte nettoyé sans répétitions
     * @throws RuntimeException si l'appel à l'API échoue
     */
    public String supprimerRepetitions(String texte) {
        if (texte == null || texte.isBlank()) {
            return "";
        }

        String prompt = "Supprime TOUTES les répétitions du texte suivant :\n\n" +
                "- Supprime les lignes identiques\n" +
                "- Supprime les phrases répétées (même avec variations mineures)\n" +
                "- Gère les variations minimes : ponctuation, espaces, casse\n" +
                "- Garde l'ordre du premier occurrence\n" +
                "- Préserve les sauts de ligne logiques\n" +
                "- Maintient la lisibilité du texte\n\n" +
                "Texte :\n" + texte + "\n\n" +
                "Retourne UNIQUEMENT le texte nettoyé, sans commentaire.";

        return callOpenAiApi(prompt, "Suppression de répétitions");
    }

    /**
     * Appelle l'API OpenAI via le SDK.
     * 
     * Méthode interne qui gère la communication avec l'API OpenAI,
     * la gestion des erreurs et l'extraction de la réponse.
     * 
     * @param prompt Texte du prompt à envoyer à l'API
     * @param operationName Nom de l'opération (pour les logs d'erreur)
     * @return Texte de la réponse de l'API
     * @throws RuntimeException si l'appel échoue
     */
    private String callOpenAiApi(String prompt, String operationName) {
        try {
            // Construire le message utilisateur
            ChatCompletionUserMessageParam userMessage = ChatCompletionUserMessageParam.builder()
                    .content(prompt)
                    .build();

            // Convertir en ChatCompletionMessage
            List<ChatCompletionCreateParams.ChatCompletionMessage> messages = new ArrayList<>();
            messages.add(ChatCompletionCreateParams.ChatCompletionMessage.of(userMessage));

            // Créer les paramètres de la requête
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .messages(messages)
                    .temperature(0.2)        // Température basse pour plus de cohérence
                    .maxTokens(4096)         // Limite tokens pour éviter dépassements
                    .build();

            // Appeler l'API OpenAI
            ChatCompletion completion = client.chat().completions().create(params);

            // Extraire la réponse
            if (completion.choices() != null && !completion.choices().isEmpty()) {
                ChatCompletionMessage responseMessage = completion.choices().get(0).message();
                if (responseMessage != null) {
                    String content = responseMessage.content();
                    if (content != null) {
                        return content.trim();
                    }
                }
            }

            throw new RuntimeException(operationName + " : aucune réponse reçue de l'API OpenAI");

        } catch (Exception e) {
            String errorMsg = operationName + " : erreur lors de l'appel à l'API OpenAI : " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }
}
