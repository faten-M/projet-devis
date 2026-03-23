package com.projetdevis.service;

import com.openai.client.OpenAIClient;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ExtractionService.
 * Pipeline BMAD - Étape 3 : Extraction
 *
 * Stratégie de mock :
 *   On injecte un OpenAIClient mocké dans une instance RÉELLE d'ExtractInfoIA.
 *   Ainsi, parseQuantity() utilise sa logique locale (COMMON_EXPRESSIONS) pour
 *   les expressions connues (quelques → 3, dizaine → 10, bcp → 20…) sans aucun
 *   appel réseau.
 *
 *   À éviter : Mockito.mock(ExtractInfoIA.class) car cela court-circuite toute
 *   la logique métier de l'IA et fait retourner 0 pour chaque int, rendant les
 *   assertions sur les quantités fausses.
 */
class ExtractionServiceTest {

    private ExtractionService extractor;

    @BeforeEach
    void setUp() {
        // Client OpenAI mocké avec RETURNS_DEEP_STUBS pour gérer les appels
        // chaînés (client.chat().completions().create(...)).
        // Les tests n'atteignent jamais le réseau car toutes les expressions
        // utilisées sont couvertes par COMMON_EXPRESSIONS de ExtractInfoIA.
        OpenAIClient mockClient = mock(OpenAIClient.class, RETURNS_DEEP_STUBS);
        ExtractInfoIA realIA = new ExtractInfoIA(mockClient);
        extractor = new ExtractionService(realIA);
    }

    // ── Format liste (tirets) ─────────────────────────────────────────────────

    @Test
    void extractWithHumanQuantities_listFormat() {
        String text = "- quelques chaises\n" +
                      "- une dizaine de tables\n" +
                      "- bcp de bureaux\n" +
                      "- 5 panneaux";

        ExtractedInfo info = extractor.extract(text);

        assertNotNull(info);
        List<ItemRequest> items = info.getItems();
        assertEquals(4, items.size(), "4 lignes → 4 articles attendus");

        assertTrue(items.stream().anyMatch(i ->
                        "chaises".equals(i.getProduct()) && i.getQuantity() == 3),
                "«quelques» doit être interprété comme 3");
        assertTrue(items.stream().anyMatch(i ->
                        "tables".equals(i.getProduct()) && i.getQuantity() == 10),
                "«une dizaine» doit être interprété comme 10");
        assertTrue(items.stream().anyMatch(i ->
                        "bureaux".equals(i.getProduct()) && i.getQuantity() == 20),
                "«bcp» doit être interprété comme 20");
        assertTrue(items.stream().anyMatch(i ->
                        "panneaux".equals(i.getProduct()) && i.getQuantity() == 5),
                "«5» (entier explicite) doit être conservé tel quel");
    }

    // ── Format alternatif (produit : quantité) ────────────────────────────────

    @Test
    void extractWithHumanQuantities_altFormat() {
        String text = "chaises : plusieurs\n" +
                      "bureaux (une dizaine)\n" +
                      "tables : env 12\n" +
                      "armoires (10+)";

        ExtractedInfo info = extractor.extract(text);

        assertNotNull(info);
        List<ItemRequest> items = info.getItems();
        assertEquals(4, items.size(), "4 lignes → 4 articles attendus");

        assertTrue(items.stream().anyMatch(i ->
                        i.getProduct() != null && i.getProduct().contains("chaise") && i.getQuantity() == 5),
                "«plusieurs» doit être interprété comme 5");
        assertTrue(items.stream().anyMatch(i ->
                        i.getProduct() != null && i.getProduct().contains("bureau") && i.getQuantity() == 10),
                "«une dizaine» doit être interprété comme 10");
        assertTrue(items.stream().anyMatch(i ->
                        i.getProduct() != null && i.getProduct().contains("table") && i.getQuantity() == 12),
                "«env 12» doit être interprété comme 12 (extraction numérique après suppression de «env»)");
        assertTrue(items.stream().anyMatch(i ->
                        i.getProduct() != null && i.getProduct().contains("armoire") && i.getQuantity() == 10),
                "«10+» doit être interprété comme 10 (suppression du signe «+»)");
    }
}
