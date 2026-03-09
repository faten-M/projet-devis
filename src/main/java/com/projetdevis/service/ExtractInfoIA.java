package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractInfoIA {

    private static final String MODEL = "gpt-5-nano";

    private static final String SYSTEM_PROMPT =
        "Tu es un assistant spécialisé dans l'interprétation des quantités exprimées "
        + "en langage humain. Ton rôle unique est de convertir une expression vague ou "
        + "imprécise en un nombre entier précis et exploitable.\n\n"
        + "Règles de conversion :\n"
        + "- 'une dizaine', 'une 10aine' ou variantes similaires → 10\n"
        + "- 'une vingtaine' → 20\n"
        + "- 'une trentaine' → 30\n"
        + "- 'une quarantaine' → 40\n"
        + "- 'une cinquantaine' → 50\n"
        + "- 'une soixantaine' → 60\n"
        + "- 'une centaine' → 100\n"
        + "- 'quelques' (sans contexte) → 3\n"
        + "- 'plusieurs' (sans contexte) → 5\n"
        + "- 'beaucoup' (sans contexte) → 20\n"
        + "- 'un peu' (sans contexte) → 2\n"
        + "- 'à peine' (sans contexte) → 1\n"
        + "- 'environ X', 'à peu près X', 'peut-être X' → X\n"
        + "- 'X ou plus', 'X minimum', 'au moins X' → X\n"
        + "- Si un nombre exact est présent, retourne-le tel quel\n\n"
        + "Instruction STRICTE :\n"
        + "- Retourne UNIQUEMENT un nombre entier positif\n"
        + "- Aucun texte, aucune explication, aucune unité\n"
        + "- Un seul nombre par réponse\n"
        + "- Si elle ne peut pas être interprétée, retourne 1 par défaut";

    private final OpenAIClient client;

    private static final Map<String, Integer> COMMON_EXPRESSIONS = new HashMap<>();

    static {
        COMMON_EXPRESSIONS.put("une dizaine", 10);
        COMMON_EXPRESSIONS.put("dizaine", 10);
        COMMON_EXPRESSIONS.put("10aine", 10);
        COMMON_EXPRESSIONS.put("une vingtaine", 20);
        COMMON_EXPRESSIONS.put("vingtaine", 20);
        COMMON_EXPRESSIONS.put("20aine", 20);
        COMMON_EXPRESSIONS.put("une trentaine", 30);
        COMMON_EXPRESSIONS.put("trentaine", 30);
        COMMON_EXPRESSIONS.put("30aine", 30);
        COMMON_EXPRESSIONS.put("une quarantaine", 40);
        COMMON_EXPRESSIONS.put("quarantaine", 40);
        COMMON_EXPRESSIONS.put("40aine", 40);
        COMMON_EXPRESSIONS.put("une cinquantaine", 50);
        COMMON_EXPRESSIONS.put("cinquantaine", 50);
        COMMON_EXPRESSIONS.put("50aine", 50);
        COMMON_EXPRESSIONS.put("une soixantaine", 60);
        COMMON_EXPRESSIONS.put("soixantaine", 60);
        COMMON_EXPRESSIONS.put("60aine", 60);
        COMMON_EXPRESSIONS.put("une centaine", 100);
        COMMON_EXPRESSIONS.put("centaine", 100);
        COMMON_EXPRESSIONS.put("quelques", 3);
        COMMON_EXPRESSIONS.put("plusieurs", 5);
        COMMON_EXPRESSIONS.put("beaucoup", 20);
        COMMON_EXPRESSIONS.put("un peu", 2);
        COMMON_EXPRESSIONS.put("à peine", 1);
        COMMON_EXPRESSIONS.put("sixaine", 6);
        COMMON_EXPRESSIONS.put("douzaine", 12);
        COMMON_EXPRESSIONS.put("quinzaine", 15);
        COMMON_EXPRESSIONS.put("huitaine", 8);
        COMMON_EXPRESSIONS.put("bcp", 20);
        COMMON_EXPRESSIONS.put("env", 0);
        COMMON_EXPRESSIONS.put("~", 0);
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile(
        "(?i)(\\d+)(?:\\s*(?:item|article|unité|pièce|chose|unit))?"
    );

    public ExtractInfoIA() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Variable d'environnement OPENAI_API_KEY manquante ou vide.");
        }
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }

    public ExtractInfoIA(OpenAIClient client) {
        if (client == null) throw new IllegalArgumentException("Le client OpenAI ne peut pas être null.");
        this.client = client;
    }

    public int parseQuantity(String quantityText) {
        if (quantityText == null || quantityText.isBlank())
            throw new IllegalArgumentException("Le texte de quantité ne peut pas être null ou vide.");

        String normalized = quantityText.toLowerCase().trim();

        for (Map.Entry<String, Integer> entry : COMMON_EXPRESSIONS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                int v = entry.getValue();
                if (v != 0) return v;
            }
        }

        int numericQuantity = extractNumericQuantity(normalized);
        if (numericQuantity > 0) {
            return numericQuantity;
        }

        return parseWithAI(quantityText);
    }

    private int extractNumericQuantity(String normalizedText) {
        String text = normalizedText;
        text = text.replaceAll("(?i)(environ|à peu près|peut-être|au moins|minimum|ou plus|env)\\s*", "");
        text = text.replaceAll("~", "");
        text = text.replaceAll("\\+", "");
        text = text.replaceAll("\\bmini\\b", "");

        Matcher m = NUMBER_PATTERN.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private int parseWithAI(String quantityText) {
        String userPrompt = "Quantité à interpréter : " + quantityText;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage(SYSTEM_PROMPT)
                .addUserMessage(userPrompt)
                .build();

        try {
            ChatCompletion completion = client.chat().completions().create(params);
            String response = completion.choices().get(0)
                    .message()
                    .content()
                    .orElse("1")
                    .trim();
            Matcher matcher = Pattern.compile("\\d+").matcher(response);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception e) {
            System.err.println("Erreur OpenAI : " + e.getMessage());
        }
        return 1;
    }

    public OpenAIClient getClient() { return client; }
    public Integer getLocalValue(String expression) {
        if (expression == null) return null;
        return COMMON_EXPRESSIONS.get(expression.toLowerCase().trim());
    }
    public boolean isKnownLocally(String expression) {
        return expression != null && getLocalValue(expression) != null;
    }
}
