package com.projetdevis.controller;

import com.projetdevis.dto.ProduitRequest;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import com.projetdevis.repository.ProduitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion du catalogue produits.
 *
 * GET  /api/produits       — liste tout le catalogue
 * POST /api/produits       — ajoute un nouveau produit
 */
@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitRepository produitRepository;

    public ProduitController(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    /**
     * Liste tous les produits du catalogue.
     */
    @GetMapping
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(produitRepository.findAll());
    }

    /**
     * Ajoute un nouveau produit au catalogue.
     *
     * Corps attendu :
     * {
     *   "nom": "Bureau assis-debout",
     *   "categorie": "BUREAU",
     *   "prixEconomique": 350.0,
     *   "prixStandard": 700.0,
     *   "prixPremium": 1400.0,
     *   "description": "...",
     *   "reference": "BUR-AD-001"
     * }
     */
    @PostMapping
    public ResponseEntity<?> addProduit(@RequestBody ProduitRequest request) {

        if (request.getNom() == null || request.getNom().isBlank()) {
            return ResponseEntity.badRequest().body("Le champ 'nom' est obligatoire.");
        }
        if (request.getCategorie() == null) {
            return ResponseEntity.badRequest().body("Le champ 'categorie' est obligatoire.");
        }
        if (request.getPrixStandard() == null || request.getPrixStandard() <= 0) {
            return ResponseEntity.badRequest().body("Le champ 'prixStandard' doit être > 0.");
        }

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
        return ResponseEntity.status(201).body(saved);
    }
}
