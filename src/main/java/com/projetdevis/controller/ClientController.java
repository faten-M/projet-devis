package com.projetdevis.controller;

import com.projetdevis.model.Client;
import com.projetdevis.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la consultation des fiches clients.
 *
 * GET /api/clients          — liste tous les clients créés
 * GET /api/clients/{id}     — détail d'un client par son identifiant
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Liste tous les clients enregistrés (extraits des e-mails traités).
     */
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    /**
     * Retourne le détail d'un client par son identifiant.
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(@PathVariable String clientId) {
        return clientRepository.findById(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
