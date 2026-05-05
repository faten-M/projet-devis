package com.projetdevis.repository;

import com.projetdevis.model.DraftQuote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DraftQuoteRepositoryTest {

    @Autowired DraftQuoteRepository repo;

    // ── save / findById ───────────────────────────────────────────────────────

    @Test
    void save_etFindById_retourneMemeDevis() {
        DraftQuote d = new DraftQuote();
        d.setQuoteNumber("DEV-TEST-001");
        d.setSubject("Test devis ciment");
        repo.save(d);

        Optional<DraftQuote> result = repo.findById("DEV-TEST-001");

        assertTrue(result.isPresent(), "Le devis sauvegardé doit être retrouvé par son numéro");
        assertEquals("Test devis ciment", result.get().getSubject());
        assertEquals(DraftQuote.DraftStatus.BROUILLON, result.get().getStatus());
    }

    @Test
    void findById_numeroInconnu_retourneEmpty() {
        Optional<DraftQuote> result = repo.findById("DEV-INCONNU");

        assertFalse(result.isPresent(), "Un numéro inconnu doit retourner Optional.empty()");
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_repositoryVide_retourneListeVide() {
        List<DraftQuote> result = repo.findAll();

        assertTrue(result.isEmpty(), "Un repository vide doit retourner une liste vide");
    }

    @Test
    void findAll_avecPlusieursDevis_retourneTous() {
        DraftQuote d1 = new DraftQuote(); d1.setQuoteNumber("DEV-001");
        DraftQuote d2 = new DraftQuote(); d2.setQuoteNumber("DEV-002");
        repo.save(d1);
        repo.save(d2);

        assertEquals(2, repo.findAll().size());
    }
}
