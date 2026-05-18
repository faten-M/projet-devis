package com.projetdevis.controller;

import com.projetdevis.dto.ClientResponse;
import com.projetdevis.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Clients", description = "Consultation des fiches clients extraites des e-mails")
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Operation(summary = "Liste tous les clients", description = "Retourne tous les clients créés automatiquement lors du traitement des e-mails.")
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> list = clientRepository.findAll().stream()
                .map(ClientResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Détail d'un client", description = "Retourne un client par son identifiant UUID.")
    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClient(
            @Parameter(description = "UUID du client", example = "3f6c8b2a-1d4e-4f7a-9c2b-8e5d3f1a2b4c")
            @PathVariable String clientId) {
        return clientRepository.findById(clientId)
                .map(ClientResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
