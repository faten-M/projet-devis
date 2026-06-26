package com.projetdevis.repository;

import com.projetdevis.model.DraftQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository Spring Data JPA pour la persistance des devis brouillons.
 *
 * Spring génère automatiquement l'implémentation (SELECT / INSERT / UPDATE / DELETE).
 * La clé primaire est le numéro de devis (String).
 */
@Repository
public interface DraftQuoteRepository extends JpaRepository<DraftQuote, String> {

    List<DraftQuote> findByClientReference(String clientId);

    @Modifying
    @Query("UPDATE DraftQuote d SET d.emailOriginal = NULL, d.emailRetentionExpiry = NULL WHERE d.emailRetentionExpiry < :now AND d.emailOriginal IS NOT NULL")
    int purgerEmailsExpires(LocalDateTime now);
}
