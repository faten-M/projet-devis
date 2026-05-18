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
        save("Gros œuvre",   AnalyzedItem.Category.GROS_OEUVRE,    6,  18,  45,
            "Ciment, béton, parpaing, brique, acier, sable, gravier", "CAT-GRO");
        save("Second œuvre", AnalyzedItem.Category.SECOND_OEUVRE,  8,  16,  28,
            "Plâtre, placo, cloison sèche, faux-plafond, carreau de plâtre", "CAT-SEC");
        save("Couverture",   AnalyzedItem.Category.COUVERTURE,    22,  38,  65,
            "Tuiles, ardoise, zinc, membrane d'étanchéité, gouttière, faîtière", "CAT-COV");
        save("Charpente",    AnalyzedItem.Category.CHARPENTE,      5,  12,  28,
            "Bois de charpente, OSB, contreplaqué, lambris, chevron, madrier", "CAT-CHA");
        save("Plomberie",    AnalyzedItem.Category.PLOMBERIE,      8,  22,  55,
            "Tuyaux, raccords, robinetterie, sanitaires, lavabo, évier, WC", "CAT-PLO");
        save("Électricité",  AnalyzedItem.Category.ELECTRICITE,    5,  16,  42,
            "Câbles, gaines, tableaux électriques, disjoncteurs, prises, interrupteurs", "CAT-ELE");
        save("VRD",          AnalyzedItem.Category.VRD,           14,  32,  68,
            "Voirie, bordures, canalisations, pavage, bitume, enrobé, regard", "CAT-VRD");
        save("Isolation",    AnalyzedItem.Category.ISOLATION,      8,  18,  38,
            "Laine de verre, laine de roche, polystyrène, pare-vapeur, liège, ouate", "CAT-ISO");
        save("Finition",     AnalyzedItem.Category.FINITION,      10,  26,  58,
            "Peinture, carrelage, enduit, parquet, revêtement de sol, faïence", "CAT-FIN");
        save("Autre",        AnalyzedItem.Category.AUTRE,         10,  25,  50,
            "Produit non catégorisé — à préciser avec le commercial", "CAT-DIV");

        System.out.println("[Catalogue] " + produitRepository.count() + " produits initialisés.");
    }

    private void save(String nom, AnalyzedItem.Category categorie,
                      double eco, double std, double premium,
                      String description, String reference) {
        Produit p = new Produit(nom, categorie, eco, std, premium);
        p.setDescription(description);
        p.setReference(reference);
        produitRepository.save(p);
    }
}
