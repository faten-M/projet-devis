package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailCleanerIA.
 *
 * Le client OpenAI est mocké avec RETURNS_DEEP_STUBS pour gérer la chaîne
 * d'appels : client.chat().completions().create(...).
 * Aucun appel réseau réel n'est effectué.
 */
@ExtendWith(MockitoExtension.class)
class EmailCleanerIATest {

    /** Client mocké avec deep stubs pour supporter la chaîne d'appels. */
    private OpenAIClient mockClient; // deep-stub used for chained calls

    /** Instance testée, initialisée avec le client mocké. */
    private EmailCleanerIA cleaner;

    @BeforeEach
    void setUp() {
        mockClient = mock(OpenAIClient.class, RETURNS_DEEP_STUBS);
        cleaner = new EmailCleanerIA(mockClient);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Configure le mock pour retourner {@code responseText} comme réponse du modèle.
     */
    private void stubApiResponse(String responseText) {
        // build a real ChatCompletion object instead of mocking final classes
        ChatCompletionMessage msg = ChatCompletionMessage.builder()
                .content(responseText)
                .refusal("")
                // JsonValue.of is inherited from JsonField and returns a JsonField, which does
                // not satisfy the builder. Use JsonValue.from to create a JsonValue instead.
                .role(com.openai.core.JsonValue.from("assistant"))
                .build();

        // build an empty Logprobs instance; the Choice builder requires this field
        // but tests don't actually inspect its contents.
        ChatCompletion.Choice.Logprobs logprobs = ChatCompletion.Choice.Logprobs.builder()
                .content(java.util.List.of())  // empty list for content
                .refusal(java.util.List.of())  // empty list for refusal
                .build();

        Choice choice = Choice.builder()
                .message(msg)
                // The API requires an index for each choice; use 0 for a single stubbed
                // result.
                .index(0)
                // finishReason expects a ChatCompletion.Choice.FinishReason object. Use
                // the factory method to create one from a string.
                .finishReason(ChatCompletion.Choice.FinishReason.of("stop"))
                .logprobs(logprobs)
                .build();

        ChatCompletion completion = ChatCompletion.builder()
                .id("stub-id")
                .created(System.currentTimeMillis() / 1000)
                .model("gpt-4")
                .choices(List.of(choice))
                .build();

        when(mockClient.chat().completions().create(any(ChatCompletionCreateParams.class)))
                .thenReturn(completion);
    }

    // =========================================================================
    // Tests du constructeur
    // =========================================================================

    @Test
    void constructor_clientNull_leveIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmailCleanerIA(null),
                "Un client null doit lever IllegalArgumentException");
    }

    @Test
    void constructor_clientValide_creeInstanceSansErreur() {
        assertDoesNotThrow(() -> new EmailCleanerIA(mockClient));
    }

    // =========================================================================
    // Tests de clean() — cas limites (pas d'appel API attendu)
    // =========================================================================

    @Test
    void clean_emailNull_retourneChainneVide() {
        assertEquals("", cleaner.clean(null));
        verify(mockClient, never()).chat();
    }

    @Test
    void clean_emailVide_retourneChainneVide() {
        assertEquals("", cleaner.clean(""));
        verify(mockClient, never()).chat();
    }

    @Test
    void clean_emailBlanc_retourneChainneVide() {
        assertEquals("", cleaner.clean("   \n\t   "));
        verify(mockClient, never()).chat();
    }

    // =========================================================================
    // Tests de clean() — appels API normaux
    // =========================================================================

    @Test
    void clean_emailAvecSignature_retourneContenuUtile() {
        stubApiResponse("Je souhaite commander 10 unités du produit REF-42.");

        String raw = """
                Bonjour,

                Je souhaite commander 10 unités du produit REF-42.

                Cordialement,
                Jean Dupont
                """;

        assertEquals(
                "Je souhaite commander 10 unités du produit REF-42.",
                cleaner.clean(raw)
        );
    }

    @Test
    void clean_reponseAvecEspacesBords_retourneResultatTrimme() {
        stubApiResponse("  Commande urgente pour 5 pièces.  ");

        String raw = "Bonjour,\nCommande urgente pour 5 pièces.\n\nCordialement";

        assertEquals("Commande urgente pour 5 pièces.", cleaner.clean(raw));
    }

    @Test
    void clean_reponseOptionalVide_retourneChainneVide() {
        ChatCompletion mockCompletion    = mock(ChatCompletion.class);
        ChatCompletion.Choice mockChoice = mock(ChatCompletion.Choice.class);
        ChatCompletionMessage mockMsg    = mock(ChatCompletionMessage.class);

        when(mockClient.chat().completions().create(any(ChatCompletionCreateParams.class)))
                .thenReturn(mockCompletion);
        when(mockCompletion.choices()).thenReturn(List.of(mockChoice));
        when(mockChoice.message()).thenReturn(mockMsg);
        when(mockMsg.content()).thenReturn(Optional.empty());

        assertEquals("", cleaner.clean("Email sans contenu utile."));
    }

    @Test
    void clean_apiLanceRuntimeException_propageLException() {
        when(mockClient.chat().completions().create(any(ChatCompletionCreateParams.class)))
                .thenThrow(new RuntimeException("API indisponible"));

        assertThrows(RuntimeException.class,
                () -> cleaner.clean("Email de test"),
                "Une exception de l'API doit être propagée");
    }

    @Test
    void clean_emailMultiLignes_appelAPIUneSeuleFois() {
        stubApiResponse("Livraison souhaitée avant le 15 mars.");

        String raw = """
                Bonjour,

                Nous avons besoin de 50 tables modèle T-200.
                Livraison souhaitée avant le 15 mars.

                Merci,
                Marie Martin
                """;

        cleaner.clean(raw);

        // Vérifie que l'API n'est appelée qu'une seule fois
        verify(mockClient.chat().completions(), times(1))
                .create(any(ChatCompletionCreateParams.class));
    }

    // =========================================================================
    // Tests de getCleaningStats() — logique pure, sans mock API
    // =========================================================================

    @Test
    void getCleaningStats_casNormal_contientTaillesEtModele() {
        String original = "Bonjour,\nCommande de 5 unités.\nCordialement";
        String cleaned  = "Commande de 5 unités.";

        String stats = cleaner.getCleaningStats(original, cleaned);

        assertAll(
                () -> assertTrue(stats.contains(String.valueOf(original.length())),
                        "Doit contenir la taille originale"),
                () -> assertTrue(stats.contains(String.valueOf(cleaned.length())),
                        "Doit contenir la taille nettoyée"),
                () -> assertTrue(stats.contains("EmailCleanerIA"),
                        "Doit mentionner EmailCleanerIA"),
                () -> assertTrue(stats.contains("gpt-5-nano"),
                        "Doit mentionner le modèle utilisé")
        );
    }

    @Test
    void getCleaningStats_originalEtCleanedNull_retourneStatsAvecZero() {
        String stats = cleaner.getCleaningStats(null, null);

        assertAll(
                () -> assertNotNull(stats),
                () -> assertTrue(stats.contains("0 caractères"))
        );
    }

    @Test
    void getCleaningStats_chainesIdentiques_afficheSuppression0() {
        String text  = "Commande de 5 unités.";
        String stats = cleaner.getCleaningStats(text, text);

        // 0 caractères supprimés et 0.0 % (ou 0,0 % selon la locale)
        assertTrue(
                stats.contains("0,0%") || stats.contains("0.0%"),
                "Le pourcentage supprimé doit être 0.0 %"
        );
    }

    @Test
    void getCleaningStats_cleanedPlusLongQueOriginal_afficheSuppressionNegative() {
        String original = "Court";
        String cleaned  = "Texte beaucoup plus long que l'original.";

        String stats = cleaner.getCleaningStats(original, cleaned);

        assertTrue(stats.contains("-"),
                "Des caractères « supprimés » négatifs doivent afficher un signe -");
    }

    @Test
    void getCleaningStats_entreeMultiLigne_compteLignesCorrectement() {
        String original = "Ligne 1\nLigne 2\nLigne 3";
        String cleaned  = "Ligne 1";

        String stats = cleaner.getCleaningStats(original, cleaned);

        assertAll(
                () -> assertTrue(stats.contains("3 lignes"),
                        "Doit indiquer 3 lignes pour l'original"),
                () -> assertTrue(stats.contains("1 lignes"),
                        "Doit indiquer 1 ligne pour le nettoyé")
        );
    }

    @Test
    void getCleaningStats_chainesVides_retourneStatsAvecZeroLignes() {
        String stats = cleaner.getCleaningStats("", "");

        // Chaînes vides → isBlank() = true → 0 lignes
        assertFalse(stats.contains("1 lignes"),
                "Des chaînes vides ne doivent pas compter comme 1 ligne");
    }
}
