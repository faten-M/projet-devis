package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;

import java.util.List;

public class OpenAiClient {

    private final OpenAIClient client;

    public OpenAiClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La variable d'environnement OPENAI_API_KEY n'est pas définie.");
        }
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String retirerSignature(String email) {
        String prompt = "Retire uniquement la signature de cet email et renvoie le texte nettoyé sans la signature. "
                + "Ne modifie rien d'autre dans le contenu. Voici l'email :\n\n" + email;

        ChatCompletionMessageParam userMessage = ChatCompletionUserMessageParam.builder()
                .content(ChatCompletionUserMessageParam.Content.ofString(prompt))
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("gpt-4o-mini")
                .messages(List.of(userMessage))
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return completion.choices().get(0).message().content().orElse("");
    }
}
