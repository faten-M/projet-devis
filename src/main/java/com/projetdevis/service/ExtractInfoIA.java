package com.projetdevis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'extraction IA :
 *  - extractProducts() / extractProductList() : extrait les produits de l'e-mail via LLM.
 *  - extractMetadata() : extrait budget, date de livraison et urgence via LLM.
 *  - parseQuantity() : convertit les quantités floues en entiers.
 */
@Service
public class ExtractInfoIA {

    // ── Modèle ───────────────────────────────────────────────────────────────
    private static final String MODEL = "gpt-4o-mini";

    // ── Prompt : extraction des produits depuis un e-mail BTP ────────────────
    private static final String EXTRACTION_SYSTEM_PROMPT =
        "Tu es un assistant expert en analyse d'e-mails pour une entreprise de commerce de gros "
        + "de matériaux de construction (ciment, béton, parpaings, briques, tuiles, acier, "
        + "bois de charpente, tuyaux, câbles, isolants, peinture, carrelage, etc.).\n\n"
        + "Ton seul rôle est d'identifier tous les matériaux/produits mentionnés dans l'e-mail "
        + "et d'en extraire les informations clés.\n\n"
        + "Pour chaque produit détecté, tu dois fournir :\n"
        + "  - \"nom\"      : le nom exact du produit tel qu'il apparaît dans l'e-mail,\n"
        + "                  en minuscules, sans article (ex: \"ciment cem ii 32,5\", "
        + "\"parpaing creux 20x20x50\", \"fer à béton ha12\").\n"
        + "  - \"quantite\" : la quantité EXACTEMENT telle qu'elle apparaît dans l'e-mail.\n"
        + "                  Exemples : \"500\", \"une dizaine\", \"15 palettes\", \"quelques\".\n"
        + "                  Si aucune quantité n'est mentionnée, mettre \"1\".\n"
        + "  - \"unite\"    : l'unité de mesure telle qu'elle apparaît dans l'e-mail.\n"
        + "                  Exemples courants : \"sac\", \"tonne\", \"m²\", \"m³\", \"ml\",\n"
        + "                  \"palette\", \"botte\", \"rouleau\", \"u\" (unité), \"m\".\n"
        + "                  Si aucune unité n'est mentionnée, déduire la plus probable\n"
        + "                  selon le produit (ciment → \"sac\", sable → \"tonne\", "
        + "carrelage → \"m²\", câble → \"ml\").\n"
        + "  - \"details\"  : tout détail utile : grade, classe, résistance, format, dimensions,\n"
        + "                  traitement, norme, couleur. Mettre \"\" si aucun détail.\n\n"
        + "Règles strictes :\n"
        + "  - Si aucun produit n'est détecté, retourner {\"produits\":[]}\n"
        + "  - Retourner UNIQUEMENT le JSON ci-dessous, sans aucun texte avant ni après,\n"
        + "    sans bloc markdown, sans commentaire\n\n"
        + "Format de sortie attendu :\n"
        + "{\n"
        + "  \"produits\": [\n"
        + "    {\n"
        + "      \"nom\": \"string\",\n"
        + "      \"quantite\": \"string\",\n"
        + "      \"unite\": \"string\",\n"
        + "      \"details\": \"string\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    // ── Prompt : extraction des métadonnées (budget, date, urgence, client) ──
    private static final String METADATA_SYSTEM_PROMPT =
        "Tu es un assistant expert en analyse d'e-mails pour une entreprise de commerce de gros "
        + "de matériaux de construction.\n\n"
        + "Ton rôle est d'extraire uniquement les métadonnées suivantes de l'e-mail :\n"
        + "  - budget_montant      : le montant numérique du budget (ex: 8000.0). Mettre 0 si absent.\n"
        + "  - budget_unite        : l'unité du budget parmi \"HT\", \"TTC\", \"\" si inconnue.\n"
        + "  - budget_brut         : le texte brut du budget (ex: \"environ 8000 euros HT\"). \"\" si absent.\n"
        + "  - date_livraison      : la date de livraison au format ISO YYYY-MM-DD. \"\" si absente.\n"
        + "  - date_livraison_brut : le texte brut de la date (ex: \"avant le 20 juin 2026\"). \"\" si absent.\n"
        + "  - urgence             : niveau d'urgence parmi \"normal\", \"urgent\", \"très urgent\", \"critique\".\n"
        + "  - nom_client          : nom ou raison sociale de l'expéditeur (ex: \"Dupont BTP\"). \"\" si absent.\n"
        + "  - email_client        : adresse email de l'expéditeur si présente dans l'e-mail. \"\" si absente.\n\n"
        + "Règles :\n"
        + "  - Ne pas extraire les produits (traités séparément).\n"
        + "  - Retourner UNIQUEMENT le JSON, sans texte avant ni après, sans markdown.";

    // ── Prompt : interprétation des quantités floues ─────────────────────────
    private static final String QUANTITY_SYSTEM_PROMPT =
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

    // ── Dépendances ──────────────────────────────────────────────────────────
    private final OpenAIClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Table locale des quantités connues (évite un appel API inutile) ──────
    private static final Map<String, Integer> COMMON_EXPRESSIONS = new HashMap<>();
    static {
        COMMON_EXPRESSIONS.put("une dizaine", 10);
        COMMON_EXPRESSIONS.put("dizaine",     10);
        COMMON_EXPRESSIONS.put("10aine",      10);
        COMMON_EXPRESSIONS.put("une vingtaine", 20);
        COMMON_EXPRESSIONS.put("vingtaine",   20);
        COMMON_EXPRESSIONS.put("20aine",      20);
        COMMON_EXPRESSIONS.put("une trentaine", 30);
        COMMON_EXPRESSIONS.put("trentaine",   30);
        COMMON_EXPRESSIONS.put("30aine",      30);
        COMMON_EXPRESSIONS.put("une quarantaine", 40);
        COMMON_EXPRESSIONS.put("quarantaine", 40);
        COMMON_EXPRESSIONS.put("40aine",      40);
        COMMON_EXPRESSIONS.put("une cinquantaine", 50);
        COMMON_EXPRESSIONS.put("cinquantaine", 50);
        COMMON_EXPRESSIONS.put("50aine",      50);
        COMMON_EXPRESSIONS.put("une soixantaine", 60);
        COMMON_EXPRESSIONS.put("soixantaine", 60);
        COMMON_EXPRESSIONS.put("60aine",      60);
        COMMON_EXPRESSIONS.put("une centaine", 100);
        COMMON_EXPRESSIONS.put("centaine",    100);
        COMMON_EXPRESSIONS.put("quelques",      3);
        COMMON_EXPRESSIONS.put("plusieurs",     5);
        COMMON_EXPRESSIONS.put("beaucoup",     20);
        COMMON_EXPRESSIONS.put("bcp",          20);
        COMMON_EXPRESSIONS.put("un peu",        2);
        COMMON_EXPRESSIONS.put("à peine",       1);
        COMMON_EXPRESSIONS.put("sixaine",       6);
        COMMON_EXPRESSIONS.put("douzaine",     12);
        COMMON_EXPRESSIONS.put("quinzaine",    15);
        COMMON_EXPRESSIONS.put("huitaine",      8);
        COMMON_EXPRESSIONS.put("env",           0); // valeur sentinelle → passer au chiffre suivant
        COMMON_EXPRESSIONS.put("~",             0);
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile(
        "(?i)(\\d+)(?:\\s*(?:item|article|unité|pièce|chose|unit))?"
    );

    // ── Constructeurs ────────────────────────────────────────────────────────

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

    // ════════════════════════════════════════════════════════════════════════
    //  EXTRACTION DE PRODUITS — LLM uniquement, zéro regex, zéro mots-clés
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Analyse un e-mail complet et retourne un JSON strictement valide contenant
     * tous les produits détectés par le LLM.
     *
     * <p>Format retourné :</p>
     * <pre>
     * {
     *   "produits": [
     *     { "nom": "bureau", "quantite": "une dizaine", "details": "réglable en hauteur" }
     *   ]
     * }
     * </pre>
     *
     * @param email texte brut ou nettoyé de l'e-mail
     * @return JSON valide ; {@code {"produits":[]}} en cas d'erreur ou d'e-mail vide
     */
    public String extractProducts(String email) {
        if (email == null || email.isBlank()) {
            return "{\"produits\":[]}";
        }

        String userPrompt = "Voici l'e-mail à analyser :\n\n" + email;

        // Schéma JSON pour Structured Outputs
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        ObjectNode produitsArray = properties.putObject("produits");
        produitsArray.put("type", "array");
        ObjectNode items = produitsArray.putObject("items");
        items.put("type", "object");
        ObjectNode itemProps = items.putObject("properties");
        itemProps.putObject("nom").put("type", "string");
        itemProps.putObject("quantite").put("type", "string");
        itemProps.putObject("unite").put("type", "string");
        itemProps.putObject("details").put("type", "string");
        items.putArray("required").add("nom").add("quantite").add("unite").add("details");
        items.put("additionalProperties", false);
        schema.putArray("required").add("produits");
        schema.put("additionalProperties", false);

        ResponseFormatJsonSchema responseFormat = ResponseFormatJsonSchema.builder()
                .jsonSchema(ResponseFormatJsonSchema.JsonSchema.builder()
                        .name("extraction_produits")
                        .schema(ResponseFormatJsonSchema.JsonSchema.Schema.builder()
                                .putAdditionalProperty("type", com.openai.core.JsonValue.from("object"))
                                .putAdditionalProperty("properties", com.openai.core.JsonValue.from(
                                        objectMapper.convertValue(properties, Map.class)))
                                .putAdditionalProperty("required", com.openai.core.JsonValue.from(
                                        List.of("produits")))
                                .putAdditionalProperty("additionalProperties", com.openai.core.JsonValue.from(false))
                                .build())
                        .strict(true)
                        .build())
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage(EXTRACTION_SYSTEM_PROMPT)
                .addUserMessage(userPrompt)
                .responseFormat(responseFormat)
                .build();

        try {
            ChatCompletion completion = client.chat().completions().create(params);
            String raw = completion.choices().get(0)
                    .message()
                    .content()
                    .orElse("{\"produits\":[]}")
                    .trim();

            // Validation : lève une exception si le JSON est malformé
            JsonNode root = objectMapper.readTree(raw);

            if (!root.has("produits") || !root.get("produits").isArray()) {
                System.err.println("[ExtractInfoIA] JSON inattendu (clé 'produits' absente) : " + raw);
                return "{\"produits\":[]}";
            }

            return raw;

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("[ExtractInfoIA] JSON invalide retourné par le LLM : " + e.getMessage());
            return "{\"produits\":[]}";
        } catch (Exception e) {
            System.err.println("[ExtractInfoIA] Erreur lors de l'extraction des produits : " + e.getMessage());
            return "{\"produits\":[]}";
        }
    }

    /**
     * Représentation typée d'un produit extrait par le LLM.
     * La quantité et l'unité sont conservées telles quelles pour être traitées
     * ensuite par {@link #parseQuantity(String)}.
     */
    public record ProductInfo(String nom, String quantite, String unite, String details) {}

    /** Métadonnées extraites de l'e-mail (budget, date, urgence, client). */
    public record MetadataInfo(
            Double budgetMontant,
            String budgetUnite,
            String budgetBrut,
            String dateLivraison,
            String dateLivraisonBrut,
            String urgence,
            String nomClient,
            String emailClient) {}

    /**
     * Extrait les métadonnées de l'e-mail via le LLM : budget, date de livraison, urgence.
     *
     * @param email texte de l'e-mail
     * @return métadonnées extraites (jamais null)
     */
    public MetadataInfo extractMetadata(String email) {
        if (email == null || email.isBlank()) {
            return new MetadataInfo(null, "", "", "", "", "normal", "", "");
        }

        ObjectNode properties = objectMapper.createObjectNode();
        properties.putObject("budget_montant").put("type", "number");
        properties.putObject("budget_unite").put("type", "string");
        properties.putObject("budget_brut").put("type", "string");
        properties.putObject("date_livraison").put("type", "string");
        properties.putObject("date_livraison_brut").put("type", "string");
        properties.putObject("urgence").put("type", "string");
        properties.putObject("nom_client").put("type", "string");
        properties.putObject("email_client").put("type", "string");

        ResponseFormatJsonSchema responseFormat = ResponseFormatJsonSchema.builder()
                .jsonSchema(ResponseFormatJsonSchema.JsonSchema.builder()
                        .name("extraction_metadata")
                        .schema(ResponseFormatJsonSchema.JsonSchema.Schema.builder()
                                .putAdditionalProperty("type", com.openai.core.JsonValue.from("object"))
                                .putAdditionalProperty("properties", com.openai.core.JsonValue.from(
                                        objectMapper.convertValue(properties, Map.class)))
                                .putAdditionalProperty("required", com.openai.core.JsonValue.from(List.of(
                                        "budget_montant", "budget_unite", "budget_brut",
                                        "date_livraison", "date_livraison_brut", "urgence",
                                        "nom_client", "email_client")))
                                .putAdditionalProperty("additionalProperties", com.openai.core.JsonValue.from(false))
                                .build())
                        .strict(true)
                        .build())
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage(METADATA_SYSTEM_PROMPT)
                .addUserMessage("Voici l'e-mail à analyser :\n\n" + email)
                .responseFormat(responseFormat)
                .build();

        try {
            ChatCompletion completion = client.chat().completions().create(params);
            String raw = completion.choices().get(0).message().content()
                    .orElse("{}").trim();

            JsonNode root = objectMapper.readTree(raw);

            double montantRaw = root.path("budget_montant").asDouble(0.0);
            Double montant = montantRaw > 0 ? montantRaw : null;
            String unite      = root.path("budget_unite").asText("").trim();
            String budgetBrut = root.path("budget_brut").asText("").trim();
            String dateLiv    = root.path("date_livraison").asText("").trim();
            String dateLivBrut = root.path("date_livraison_brut").asText("").trim();
            String urgence    = root.path("urgence").asText("normal").trim();
            String nomClient  = root.path("nom_client").asText("").trim();
            String emailClient = root.path("email_client").asText("").trim();

            return new MetadataInfo(montant, unite, budgetBrut, dateLiv, dateLivBrut,
                                    urgence, nomClient, emailClient);

        } catch (Exception e) {
            System.err.println("[ExtractInfoIA] Erreur extraction métadonnées : " + e.getMessage());
            return new MetadataInfo(null, "", "", "", "", "normal", "", "");
        }
    }

    /**
     * Version typée de {@link #extractProducts(String)}.
     * Retourne une liste de {@link ProductInfo} prête à l'emploi.
     *
     * @param email texte de l'e-mail
     * @return liste (potentiellement vide) des produits détectés
     */
    public List<ProductInfo> extractProductList(String email) {
        String json = extractProducts(email);
        List<ProductInfo> result = new ArrayList<>();

        try {
            JsonNode root    = objectMapper.readTree(json);
            JsonNode tableau = root.get("produits");

            if (tableau != null && tableau.isArray()) {
                for (JsonNode node : tableau) {
                    String nom      = node.path("nom").asText("").trim();
                    String quantite = node.path("quantite").asText("1").trim();
                    String unite    = node.path("unite").asText("u").trim();
                    String details  = node.path("details").asText("").trim();

                    if (!nom.isBlank()) {
                        result.add(new ProductInfo(nom, quantite, unite, details));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ExtractInfoIA] Erreur parsing liste produits : " + e.getMessage());
        }

        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PARSEUR INTERNE DES QUANTITÉS FLOUES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Convertit une expression de quantité (floue ou exacte) en entier.
     * Consulte d'abord la table locale, puis le chiffre présent dans le texte,
     * et enfin appelle le LLM en dernier recours.
     *
     * @param quantityText expression brute (ex: "une dizaine", "environ 5", "3")
     * @return entier &gt;= 1
     */
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

        return parseQuantityWithAI(quantityText);
    }

    private int extractNumericQuantity(String normalizedText) {
        String text = normalizedText
                .replaceAll("(?i)(environ|à peu près|peut-être|au moins|minimum|ou plus|env)\\s*", "")
                .replaceAll("~", "")
                .replaceAll("\\+", "")
                .replaceAll("\\bmini\\b", "");

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

    private int parseQuantityWithAI(String quantityText) {
        String userPrompt = "Quantité à interpréter : " + quantityText;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage(QUANTITY_SYSTEM_PROMPT)
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
            System.err.println("[ExtractInfoIA] Erreur OpenAI (parseQuantity) : " + e.getMessage());
        }
        return 1;
    }

    // ── Accesseurs utilitaires ───────────────────────────────────────────────

    public OpenAIClient getClient() { return client; }

    public Integer getLocalValue(String expression) {
        if (expression == null) return null;
        return COMMON_EXPRESSIONS.get(expression.toLowerCase().trim());
    }

    public boolean isKnownLocally(String expression) {
        return expression != null && getLocalValue(expression) != null;
    }
}
