package com.projetdevis.repository;

import com.projetdevis.model.CorrectionIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorrectionIARepository extends JpaRepository<CorrectionIA, Long> {
    List<CorrectionIA> findAllByOrderByCreatedAtDesc();
    long countByChamp(CorrectionIA.ChampCorrige champ);
    void deleteByQuoteNumber(String quoteNumber);
}
