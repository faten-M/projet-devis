package com.projetdevis.repository;

import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProduitRepositoryTest {

    @Autowired ProduitRepository repo;

    // ── findFirstByCategorie ──────────────────────────────────────────────────

    @Test
    void findFirstByCategorie_categorieExistante_retourneProduit() {
        Produit p = new Produit("Gros œuvre", AnalyzedItem.Category.GROS_OEUVRE, 6, 18, 45);
        repo.save(p);

        Optional<Produit> result = repo.findFirstByCategorie(AnalyzedItem.Category.GROS_OEUVRE);

        assertTrue(result.isPresent(), "Un produit GROS_OEUVRE doit être trouvé");
        assertEquals("Gros œuvre", result.get().getNom());
        assertEquals(18.0, result.get().getPrixStandard(), 0.01);
    }

    @Test
    void findFirstByCategorie_categorieAbsente_retourneEmpty() {
        Optional<Produit> result = repo.findFirstByCategorie(AnalyzedItem.Category.FINITION);

        assertFalse(result.isPresent(), "Aucun produit FINITION ne doit être trouvé");
    }

    // ── save / findAll ────────────────────────────────────────────────────────

    @Test
    void save_etFindAll_retourneLesProduitsSauvegardes() {
        repo.save(new Produit("Isolation", AnalyzedItem.Category.ISOLATION, 8, 18, 38));
        repo.save(new Produit("Plomberie", AnalyzedItem.Category.PLOMBERIE, 8, 22, 55));

        assertEquals(2, repo.findAll().size());
    }
}
