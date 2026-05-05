package com.projetdevis.service;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.DraftQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DraftService.
 * Pipeline BMAD - Étape 5 : Brouillon du devis
 *
 * Couverture :
 *  - Rejet automatique d'une analyse vide ou nulle
 *  - Application de la grille tarifaire standard (bureau = 450 €)
 *  - Calcul du total HT (quantité × prix unitaire)
 *  - Calcul du total TTC (HT × 1,20 avec TVA 20 %)
 */
class DraftServiceTest {

    private DraftService service;

    @BeforeEach
    void setUp() {
        service = new DraftService();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Construit un AnalyzedItem valide (statut VALID) prêt à être injecté
     * dans une AnalyzedInfo pour déclencher la génération d'un brouillon.
     */
    private AnalyzedItem buildItem(String product, int qty, AnalyzedItem.Category category) {
        AnalyzedItem item = new AnalyzedItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setCategory(category);
        item.setStatus(AnalyzedItem.ValidationStatus.VALID);
        item.setConfidence(0.9);
        return item;
    }

    // ── Cas dégradés ─────────────────────────────────────────────────────────

    @Test
    void generateDraft_analyseNull_statutRejete() {
        DraftQuote draft = service.generateDraft(null);

        assertEquals(DraftQuote.DraftStatus.REJETE, draft.getStatus(),
                "Une analyse nulle doit produire un brouillon REJETE");
    }

    @Test
    void generateDraft_sansArticles_statutRejete() {
        DraftQuote draft = service.generateDraft(new AnalyzedInfo());

        assertEquals(DraftQuote.DraftStatus.REJETE, draft.getStatus(),
                "Une analyse sans articles doit produire un brouillon REJETE");
    }

    // ── Grille tarifaire ─────────────────────────────────────────────────────

    @Test
    void generateDraft_bureauStandard_prixUnitaire450() {
        AnalyzedInfo analysis = new AnalyzedInfo();
        analysis.addItem(buildItem("bureau", 1, AnalyzedItem.Category.BUREAU));

        DraftQuote draft = service.generateDraft(analysis);

        assertFalse(draft.getItems().isEmpty(),
                "Le brouillon doit contenir au moins un article");
        assertEquals(450.0, draft.getItems().get(0).getUnitPriceHT(), 0.01,
                "Le prix standard d'un bureau est 450 € (grille tarifaire)");
    }

    // ── Calcul des totaux ─────────────────────────────────────────────────────

    @Test
    void generateDraft_deuxBureaux_totalHTEgal900() {
        AnalyzedInfo analysis = new AnalyzedInfo();
        analysis.addItem(buildItem("bureau", 2, AnalyzedItem.Category.BUREAU));

        DraftQuote draft = service.generateDraft(analysis);

        assertNotNull(draft.getTotalHT(), "Le total HT ne doit pas être null");
        assertEquals(900.0, draft.getTotalHT(), 0.01,
                "2 bureaux × 450 € = 900 € HT");
    }

    @Test
    void generateDraft_totalTTC_correspond_totalHT_fois_1point20() {
        AnalyzedInfo analysis = new AnalyzedInfo();
        analysis.addItem(buildItem("fauteuil", 1, AnalyzedItem.Category.SIEGE));

        DraftQuote draft = service.generateDraft(analysis);

        assertNotNull(draft.getTotalHT(),  "Total HT ne doit pas être null");
        assertNotNull(draft.getTotalTTC(), "Total TTC ne doit pas être null");
        assertEquals(draft.getTotalHT() * 1.20, draft.getTotalTTC(), 0.01,
                "TTC doit être égal à HT × 1,20 (TVA 20 %)");
    }
}
