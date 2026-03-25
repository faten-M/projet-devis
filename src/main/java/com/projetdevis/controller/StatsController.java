package com.projetdevis.controller;

import com.projetdevis.dto.StatsResponse;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import com.projetdevis.repository.ProduitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour le tableau de bord global.
 *
 * GET /api/stats — retourne des indicateurs agrégés sur l'activité
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final DraftQuoteRepository quoteRepository;
    private final ClientRepository     clientRepository;
    private final ProduitRepository    produitRepository;

    public StatsController(DraftQuoteRepository quoteRepository,
                           ClientRepository clientRepository,
                           ProduitRepository produitRepository) {
        this.quoteRepository   = quoteRepository;
        this.clientRepository  = clientRepository;
        this.produitRepository = produitRepository;
    }

    /**
     * Retourne les statistiques globales de l'application :
     * - Nombre total de devis
     * - Montant total HT et montant moyen HT
     * - Répartition par statut
     * - Nombre de clients et de produits
     * - Taux de confiance moyen de l'IA
     */
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        List<DraftQuote> allQuotes = quoteRepository.findAll();

        StatsResponse stats = new StatsResponse();

        // Comptages de base
        stats.setTotalDevis(allQuotes.size());
        stats.setTotalClients(clientRepository.count());
        stats.setTotalProduits(produitRepository.count());

        // Montants
        double totalHT = allQuotes.stream()
                .filter(q -> q.getTotalHT() != null)
                .mapToDouble(DraftQuote::getTotalHT)
                .sum();
        stats.setMontantTotalHT(totalHT);
        stats.setMontantMoyenHT(allQuotes.isEmpty() ? 0.0 : totalHT / allQuotes.size());

        // Répartition par statut
        Map<String, Long> parStatut = allQuotes.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getStatus() != null ? q.getStatus().getLabel() : "Inconnu",
                        Collectors.counting()
                ));
        stats.setDevisParStatut(parStatut);

        // Confiance moyenne de l'IA
        double avgConfidence = allQuotes.stream()
                .mapToDouble(DraftQuote::getConfidence)
                .average()
                .orElse(0.0);
        stats.setTauxConfidenceMoyen(Math.round(avgConfidence * 100.0) / 100.0);

        return ResponseEntity.ok(stats);
    }
}
