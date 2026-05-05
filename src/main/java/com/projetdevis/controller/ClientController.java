package com.projetdevis.controller;

import com.projetdevis.dto.ClientResponse;
import com.projetdevis.model.Client;
import com.projetdevis.repository.ClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> list = clientRepository.findAll().stream()
                .map(ClientResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(@PathVariable String clientId) {
        return clientRepository.findById(clientId)
                .map(ClientResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
