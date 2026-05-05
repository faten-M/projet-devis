package com.projetdevis.repository;

import com.projetdevis.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour la persistance des fiches clients.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    /** Recherche un client existant par son email — utilisé pour la déduplication. */
    Optional<Client> findByEmailOrigine(String emailOrigine);
}
