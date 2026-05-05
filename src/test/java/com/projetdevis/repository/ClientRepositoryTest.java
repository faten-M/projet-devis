package com.projetdevis.repository;

import com.projetdevis.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired ClientRepository repo;

    // ── save / findById ───────────────────────────────────────────────────────

    @Test
    void save_etFindById_retourneMemeClient() {
        Client c = new Client("Dupont BTP");
        repo.save(c);

        Optional<Client> result = repo.findById(c.getClientId());

        assertTrue(result.isPresent(), "Le client sauvegardé doit être retrouvé par son id");
        assertEquals("Dupont BTP", result.get().getRaisonSociale());
    }

    @Test
    void findById_idInconnu_retourneEmpty() {
        Optional<Client> result = repo.findById("CLI-INCONNU");

        assertFalse(result.isPresent(), "Un id inconnu doit retourner Optional.empty()");
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_avecPlusieursClients_retourneTous() {
        repo.save(new Client("Client A"));
        repo.save(new Client("Client B"));

        List<Client> clients = repo.findAll();

        assertEquals(2, clients.size());
    }
}
