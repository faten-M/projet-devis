package com.projetdevis.service;

import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.model.ValidatedQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ValidationService.
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * Couverture :
 *  - Rejet d'un brouillon null (IllegalArgumentException)
 *  - Modification de quantité valide et invalide
 *  - Application d'une remise globale (10 %) et dépassement du plafond (35 %)
 *  - Rejet d'un devis avec raison
 *
 * Structure : @Nested sépare les tests "sans validation active" de ceux
 * "avec validation démarrée", éliminant le setup répété.
 */
class ValidationServiceTest {

    private ValidationService service;

    @BeforeEach
    void setUp() {
        service = new ValidationService("Commercial Test");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Construit un DraftQuote minimal avec un article au prix défini,
     * sans inconsistances ni actions en attente, prêt à être validé.
     * Item : 2 × Bureau standard à 450 € → totalHT = 900 €
     */
    private DraftQuote buildDraftWithPricedItem() {
        DraftQuote draft = new DraftQuote();

        QuoteItem item = new QuoteItem();
        item.setLineNumber(1);
        item.setDesignation("Bureau standard");
        item.setQuantity(2);
        item.setUnitPriceHT(450.0);            // déclenche updateCalculatedPrices()
        item.setCategory(AnalyzedItem.Category.GROS_OEUVRE);
        item.setReference("BUR-001");
        item.setStatus(QuoteItem.LineStatus.A_VALIDER);

        draft.addItem(item);
        draft.recalculateTotals();             // totalHT = 900, totalTTC = 1 080
        return draft;
    }

    // ── Sans validation active ────────────────────────────────────────────────

    @Test
    void startValidation_null_leveIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.startValidation(null),
                "Un brouillon null doit lever IllegalArgumentException");
    }

    // ── Avec validation démarrée ──────────────────────────────────────────────

    /**
     * Groupe de tests partageant le même état initial : un brouillon valide
     * avec une validation déjà démarrée. Le @BeforeEach interne évite de
     * répéter buildDraftWithPricedItem() + startValidation() dans chaque test.
     */
    @Nested
    class AvecValidationEnCours {

        private DraftQuote draft;

        @BeforeEach
        void demarrerValidation() {
            draft = buildDraftWithPricedItem();
            service.startValidation(draft);
        }

        // ── Modification de quantité ──────────────────────────────────────────

        @Test
        void modifyQuantity_valide_miseAJourReussie() {
            boolean result = service.modifyQuantity(1, 5);

            assertTrue(result, "La modification de quantité doit réussir");
            assertEquals(5, draft.getItems().get(0).getQuantity(),
                    "La quantité doit être mise à jour à 5");
        }

        @Test
        void modifyQuantity_zero_echoue() {
            boolean result = service.modifyQuantity(1, 0);

            assertFalse(result, "Une quantité de 0 doit être refusée");
            assertEquals(2, draft.getItems().get(0).getQuantity(),
                    "La quantité initiale (2) ne doit pas changer après un échec");
        }

        // ── Remise globale ────────────────────────────────────────────────────

        @Test
        void applyGlobalDiscount_10pct_remiseAppliqueeEtTotalRecalcule() {
            boolean result = service.applyGlobalDiscount(10.0);

            assertTrue(result, "Une remise de 10 % doit être acceptée");
            assertEquals(10.0, draft.getItems().get(0).getDiscountPercent(), 0.01,
                    "La remise sur l'article doit être 10 %");
            // 2 × 450 × (1 − 0,10) = 810 €
            assertEquals(810.0, draft.getTotalHT(), 0.01,
                    "Total HT après 10 % de remise : 2 × 450 × 0,90 = 810 €");
        }

        @Test
        void applyGlobalDiscount_depassePlafond30pct_echoue() {
            boolean result = service.applyGlobalDiscount(35.0);

            assertFalse(result,
                    "Une remise de 35 % dépasse le plafond autorisé (30 %) et doit être refusée");
            assertNull(draft.getItems().get(0).getDiscountPercent(),
                    "Aucune remise ne doit avoir été appliquée sur l'article");
        }

        // ── Rejet du devis ────────────────────────────────────────────────────

        @Test
        void reject_avecRaison_devisMarqueRejete() {
            boolean result = service.reject("Prix trop élevé pour le client");

            assertTrue(result, "Le rejet avec une raison doit réussir");
            assertEquals(DraftQuote.DraftStatus.REJETE, draft.getStatus(),
                    "Le statut du brouillon doit passer à REJETE");
            assertEquals(ValidatedQuote.ValidationStatus.REJETE,
                    service.getCurrentValidation().getValidationStatus(),
                    "Le statut de validation doit être REJETE");
        }
    }
}
