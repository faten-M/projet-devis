package com.projetdevis.repository;

import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour le catalogue produits.
 */
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    /**
     * Retourne le premier produit correspondant à la catégorie donnée.
     * Utilisé par DraftService pour récupérer la grille de prix depuis la base.
     */
    Optional<Produit> findFirstByCategorie(AnalyzedItem.Category categorie);
}
