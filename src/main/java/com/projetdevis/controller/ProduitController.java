package com.projetdevis.controller;

import com.projetdevis.dto.ProduitRequest;
import com.projetdevis.dto.ProduitResponse;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import com.projetdevis.repository.ProduitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Produits", description = "Gestion du catalogue produits BTP")
@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitRepository produitRepository;

    public ProduitController(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Operation(summary = "Liste tout le catalogue", description = "Retourne tous les produits BTP disponibles dans le catalogue.")
    @GetMapping
    public ResponseEntity<List<ProduitResponse>> getAllProduits() {
        List<ProduitResponse> list = produitRepository.findAll().stream()
                .map(ProduitResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Ajouter un produit",
        description = "Ajoute un nouveau produit BTP au catalogue. Catégories valides : GROS_OEUVRE, SECOND_OEUVRE, COUVERTURE, CHARPENTE, PLOMBERIE, ELECTRICITE, VRD, ISOLATION, FINITION, AUTRE.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Produit créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou catégorie inconnue")
        }
    )
    @PostMapping
    public ResponseEntity<?> addProduit(@Valid @RequestBody ProduitRequest request) {

        // Convertir la catégorie (String → enum)
        AnalyzedItem.Category categorie;
        try {
            categorie = AnalyzedItem.Category.valueOf(request.getCategorie().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Catégorie inconnue : '" + request.getCategorie() + "'. "
                        + "Valeurs acceptées : GROS_OEUVRE, SECOND_OEUVRE, COUVERTURE, "
                        + "CHARPENTE, PLOMBERIE, ELECTRICITE, VRD, ISOLATION, FINITION, AUTRE.");
        }

        Produit produit = new Produit(
                request.getNom(),
                categorie,
                request.getPrixEconomique() != null ? request.getPrixEconomique() : request.getPrixStandard(),
                request.getPrixStandard(),
                request.getPrixPremium()    != null ? request.getPrixPremium()    : request.getPrixStandard()
        );
        produit.setDescription(request.getDescription());
        produit.setReference(request.getReference());

        Produit saved = produitRepository.save(produit);
        return ResponseEntity.status(201).body(ProduitResponse.from(saved));
    }
}
