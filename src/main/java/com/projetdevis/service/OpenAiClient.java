package com.projetdevis.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenAiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public OpenAiClient() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("La variable d'environnement OPENAI_API_KEY n'est pas définie.");
        }
        this.apiKey = key;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Constructeur package-private pour les tests (injection du mock)
    OpenAiClient(HttpClient httpClient, String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    public String retirerSignature(String email) {
        String prompt = "Retire uniquement la signature de cet email et renvoie le texte nettoyé sans la signature. "
                + "Ne modifie rien d'autre dans le contenu. Voici l'email :\n\n" + email;

        JsonObject body = new JsonObject();
        // Utiliser un modèle stable compatible si besoin (changer si vous avez un modèle spécifique)
        body.addProperty("model", "gpt-3.5-turbo");

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);
        body.add("messages", messages);

        body.addProperty("temperature", 0.0);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonObject resp = gson.fromJson(response.body(), JsonObject.class);
                JsonArray choices = resp.getAsJsonArray("choices");
                if (choices != null && choices.size() > 0) {
                    JsonObject first = choices.get(0).getAsJsonObject();
                    JsonObject message = first.getAsJsonObject("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").getAsString().trim();
                    }
                }
            } else {
                throw new RuntimeException("OpenAI API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur lors de l'appel à l'API OpenAI", e);
        }

        return "";
    }
}
