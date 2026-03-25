package com.projetdevis.repository;

import com.projetdevis.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour la persistance des fiches clients.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
}
