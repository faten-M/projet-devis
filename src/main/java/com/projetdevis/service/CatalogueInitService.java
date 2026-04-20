package com.projetdevis.service;

import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import com.projetdevis.repository.ProduitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise le catalogue produits au démarrage de l'application.
 *
 * Si la table produits est vide, ce composant insère les prix de référence
 * pour chaque catégorie de matériaux de construction. Ces valeurs remplacent
 * les constantes codées en dur dans DraftService.
 */
@Component
public class CatalogueInitService implements CommandLineRunner {

    private final ProduitRepository produitRepository;

    public CatalogueInitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Override
    public void run(String... args) {
        if (produitRepository.count() > 0) {
            return; // Catalogue déjà initialisé
        }

        // Grille des prix [économique, standard, premium] — matériaux de construction
        produitRepository.save(new Produit("Gros œuvre",   AnalyzedItem.Category.GROS_OEUVRE,    6,  18,  45));
        produitRepository.save(new Produit("Second œuvre", AnalyzedItem.Category.SECOND_OEUVRE,  8,  16,  28));
        produitRepository.save(new Produit("Couverture",   AnalyzedItem.Category.COUVERTURE,    22,  38,  65));
        produitRepository.save(new Produit("Charpente",    AnalyzedItem.Category.CHARPENTE,      5,  12,  28));
        produitRepository.save(new Produit("Plomberie",    AnalyzedItem.Category.PLOMBERIE,      8,  22,  55));
        produitRepository.save(new Produit("Électricité",  AnalyzedItem.Category.ELECTRICITE,    5,  16,  42));
        produitRepository.save(new Produit("VRD",          AnalyzedItem.Category.VRD,           14,  32,  68));
        produitRepository.save(new Produit("Isolation",    AnalyzedItem.Category.ISOLATION,      8,  18,  38));
        produitRepository.save(new Produit("Finition",     AnalyzedItem.Category.FINITION,      10,  26,  58));
        produitRepository.save(new Produit("Autre",        AnalyzedItem.Category.AUTRE,         10,  25,  50));

        System.out.println("[Catalogue] " + produitRepository.count() + " produits initialisés.");
    }
}
