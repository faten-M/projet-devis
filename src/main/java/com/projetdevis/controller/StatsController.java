package com.projetdevis.controller;

import com.projetdevis.dto.StatsResponse;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import com.projetdevis.repository.ProduitRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Statistiques", description = "Tableau de bord global de l'activité devis")
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

    @Operation(
        summary = "Tableau de bord global",
        description = "Retourne des indicateurs agrégés : nombre de devis, montants HT, répartition par statut, taux de validation, temps moyen de validation, confiance IA."
    )
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        List<DraftQuote> allQuotes = quoteRepository.findAll();

        StatsResponse stats = new StatsResponse();

        stats.setTotalDevis(allQuotes.size());
        stats.setTotalClients(clientRepository.count());
        stats.setTotalProduits(produitRepository.count());

        double totalHT = allQuotes.stream()
                .filter(q -> q.getTotalHT() != null)
                .mapToDouble(DraftQuote::getTotalHT)
                .sum();
        stats.setMontantTotalHT(totalHT);
        stats.setMontantMoyenHT(allQuotes.isEmpty() ? 0.0 : totalHT / allQuotes.size());

        Map<String, Long> parStatut = allQuotes.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getStatus() != null ? q.getStatus().getLabel() : "Inconnu",
                        Collectors.counting()
                ));
        stats.setDevisParStatut(parStatut);

        double avgConfidence = allQuotes.stream()
                .mapToDouble(DraftQuote::getConfidence)
                .average()
                .orElse(0.0);
        stats.setTauxConfidenceMoyen(Math.round(avgConfidence * 100.0) / 100.0);

        // Taux de validation : % de devis au statut PRET
        long nbPret = allQuotes.stream()
                .filter(q -> q.getStatus() == DraftQuote.DraftStatus.PRET)
                .count();
        double tauxValidation = allQuotes.isEmpty() ? 0.0
                : Math.round((nbPret * 100.0 / allQuotes.size()) * 10.0) / 10.0;
        stats.setTauxValidation(tauxValidation);

        // Temps moyen de validation (en heures) pour les devis PRET
        double tempsMoyen = allQuotes.stream()
                .filter(q -> q.getStatus() == DraftQuote.DraftStatus.PRET
                        && q.getCreatedAt() != null && q.getModifiedAt() != null)
                .mapToLong(q -> Duration.between(q.getCreatedAt(), q.getModifiedAt()).toMinutes())
                .average()
                .orElse(0.0);
        stats.setTempsMoyenValidationMinutes(Math.round(tempsMoyen * 10.0) / 10.0);

        return ResponseEntity.ok(stats);
    }
}
