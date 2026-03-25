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
 * pour chaque catégorie de mobilier. Ces valeurs remplacent les constantes
 * codées en dur dans DraftService.
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

        // Grille des prix [économique, standard, premium] par catégorie
        produitRepository.save(new Produit("Bureau",         AnalyzedItem.Category.BUREAU,         250, 450,  800));
        produitRepository.save(new Produit("Siège",          AnalyzedItem.Category.SIEGE,           150, 350,  650));
        produitRepository.save(new Produit("Rangement",      AnalyzedItem.Category.RANGEMENT,       200, 400,  700));
        produitRepository.save(new Produit("Table",          AnalyzedItem.Category.TABLE,           300, 600, 1200));
        produitRepository.save(new Produit("Éclairage",      AnalyzedItem.Category.ECLAIRAGE,        50, 120,  250));
        produitRepository.save(new Produit("Accessoire",     AnalyzedItem.Category.ACCESSOIRE,       30,  80,  150));
        produitRepository.save(new Produit("Espace détente", AnalyzedItem.Category.ESPACE_DETENTE,  400, 800, 1500));
        produitRepository.save(new Produit("Cloison",        AnalyzedItem.Category.CLOISON,         150, 300,  500));
        produitRepository.save(new Produit("Autre",          AnalyzedItem.Category.AUTRE,           100, 200,  400));

        System.out.println("[Catalogue] " + produitRepository.count() + " produits initialisés.");
    }
}
