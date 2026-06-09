package com.projetdevis.repository;

import com.projetdevis.model.CorrectionIA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CorrectionIARepositoryTest {

    @Autowired CorrectionIARepository repo;

    // ── save / count ──────────────────────────────────────────────────────────

    @Test
    void save_correction_estPersisteeEnBase() {
        CorrectionIA c = new CorrectionIA();
        c.setQuoteNumber("DEV-TEST-001");
        c.setLineNumber(1);
        c.setChamp(CorrectionIA.ChampCorrige.QUANTITE);
        c.setValeurIa("20");
        c.setValeurCommerciale("15");

        CorrectionIA saved = repo.save(c);

        assertNotNull(saved.getId(), "L'id doit être généré automatiquement");
        assertNotNull(saved.getCreatedAt(), "La date de création doit être remplie automatiquement");
        assertEquals("DEV-TEST-001", saved.getQuoteNumber());
        assertEquals("20", saved.getValeurIa());
        assertEquals("15", saved.getValeurCommerciale());
    }

    @Test
    void findAllByOrderByCreatedAtDesc_retourneCorrectionsTrieesParDate() {
        CorrectionIA c1 = correction("DEV-001", 1, CorrectionIA.ChampCorrige.QUANTITE, "10", "5");
        CorrectionIA c2 = correction("DEV-002", 1, CorrectionIA.ChampCorrige.PRIX_UNITAIRE, "50.00", "45.00");
        CorrectionIA c3 = correction("DEV-003", 2, CorrectionIA.ChampCorrige.DESIGNATION, "Ciment", "Ciment CEM II");
        repo.save(c1);
        repo.save(c2);
        repo.save(c3);

        List<CorrectionIA> result = repo.findAllByOrderByCreatedAtDesc();

        assertEquals(3, result.size(), "Les 3 corrections doivent être retournées");
    }

    @Test
    void countByChamp_compteCorrectementParType() {
        repo.save(correction("DEV-001", 1, CorrectionIA.ChampCorrige.QUANTITE,     "10", "5"));
        repo.save(correction("DEV-001", 2, CorrectionIA.ChampCorrige.QUANTITE,     "20", "15"));
        repo.save(correction("DEV-002", 1, CorrectionIA.ChampCorrige.PRIX_UNITAIRE,"50.00", "45.00"));

        assertEquals(2, repo.countByChamp(CorrectionIA.ChampCorrige.QUANTITE),
                "Doit compter 2 corrections de quantité");
        assertEquals(1, repo.countByChamp(CorrectionIA.ChampCorrige.PRIX_UNITAIRE),
                "Doit compter 1 correction de prix");
        assertEquals(0, repo.countByChamp(CorrectionIA.ChampCorrige.DESIGNATION),
                "Doit compter 0 correction de désignation");
    }

    @Test
    void count_basVide_retourneZero() {
        assertEquals(0, repo.count(), "Table vide → 0 corrections");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private CorrectionIA correction(String quoteNumber, int line,
                                     CorrectionIA.ChampCorrige champ,
                                     String valeurIa, String valeurCommerciale) {
        CorrectionIA c = new CorrectionIA();
        c.setQuoteNumber(quoteNumber);
        c.setLineNumber(line);
        c.setChamp(champ);
        c.setValeurIa(valeurIa);
        c.setValeurCommerciale(valeurCommerciale);
        return c;
    }
}
