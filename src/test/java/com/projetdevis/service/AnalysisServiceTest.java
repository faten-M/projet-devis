package com.projetdevis.service;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AnalysisService.
 * Pipeline BMAD - Étape 4 : Analyse
 *
 * Couverture :
 *  - Catégorisation de produits connus (bureau, chaise, armoire) via @ParameterizedTest
 *  - Filtrage des phrases décoratives (faux positifs)
 *  - Robustesse face à un input null
 */
class AnalysisServiceTest {

    private AnalysisService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisService();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Crée un ExtractedInfo avec un seul article. */
    private ExtractedInfo extracted(String product, Integer quantity) {
        ExtractedInfo info = new ExtractedInfo();
        info.addItem(new ItemRequest(product, quantity));
        return info;
    }

    // ── Catégorisation (paramétrisée) ─────────────────────────────────────────

    /**
     * Source de données : (produit, catégorie attendue).
     * Les 3 cas couvrent les catégories les plus fréquentes du catalogue.
     */
    static Stream<Arguments> produitsConnus() {
        return Stream.of(
            Arguments.of("bureau en chêne",       AnalyzedItem.Category.BUREAU),
            Arguments.of("chaise ergonomique",    AnalyzedItem.Category.SIEGE),
            Arguments.of("armoire de rangement",  AnalyzedItem.Category.RANGEMENT)
        );
    }

    @ParameterizedTest(name = "«{0}» → catégorie {1}")
    @MethodSource("produitsConnus")
    void analyze_produitConnu_categorieCorrecte(String produit, AnalyzedItem.Category attendu) {
        AnalyzedInfo result = service.analyze(extracted(produit, 5));

        assertFalse(result.getItems().isEmpty(),
                "Le produit «" + produit + "» ne doit pas être filtré");
        assertEquals(attendu, result.getItems().get(0).getCategory(),
                "La catégorie de «" + produit + "» doit être " + attendu);
    }

    // ── Filtrage des faux positifs ────────────────────────────────────────────

    @Test
    void analyze_phraseDecorative_filtree() {
        // "bonjour" figure dans DECORATIVE_PHRASES → doit être rejeté
        AnalyzedInfo result = service.analyze(extracted("bonjour", null));

        assertTrue(result.getItems().isEmpty(),
                "Une phrase décorative (ex. «bonjour») doit être filtrée");
    }

    // ── Robustesse ────────────────────────────────────────────────────────────

    @Test
    void analyze_inputNull_retourneResultatVideSansException() {
        AnalyzedInfo result = service.analyze(null);

        assertNotNull(result, "Le résultat ne doit pas être null");
        assertTrue(result.getItems().isEmpty(),
                "Aucun article attendu pour un input null");
        assertFalse(result.getInconsistencies().isEmpty(),
                "Une incohérence doit être signalée pour un input null");
    }
}
