package com.projetdevis.repository;

import com.projetdevis.model.DraftQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour la persistance des devis brouillons.
 *
 * Spring génère automatiquement l'implémentation (SELECT / INSERT / UPDATE / DELETE).
 * La clé primaire est le numéro de devis (String).
 */
@Repository
public interface DraftQuoteRepository extends JpaRepository<DraftQuote, String> {
}
