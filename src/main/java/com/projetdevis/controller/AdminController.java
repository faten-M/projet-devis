package com.projetdevis.controller;

import com.projetdevis.model.CorrectionIA;
import com.projetdevis.repository.CorrectionIARepository;
import com.projetdevis.repository.DraftQuoteRepository;
import com.projetdevis.service.EmailReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin", description = "Actions d'administration — réservées au développement et aux tests")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmailReaderService     emailReaderService;
    private final DraftQuoteRepository   quoteRepository;
    private final CorrectionIARepository correctionRepository;

    public AdminController(EmailReaderService emailReaderService,
                           DraftQuoteRepository quoteRepository,
                           CorrectionIARepository correctionRepository) {
        this.emailReaderService   = emailReaderService;
        this.quoteRepository      = quoteRepository;
        this.correctionRepository = correctionRepository;
    }

    @Operation(
        summary = "Supprimer un devis (admin)",
        description = "Supprime définitivement un devis et toutes ses lignes associées. Réservé aux tests."
    )
    @ApiResponse(responseCode = "200", description = "Devis supprimé")
    @ApiResponse(responseCode = "404", description = "Devis introuvable")
    @DeleteMapping("/devis/{quoteNumber}")
    public ResponseEntity<Map<String, String>> supprimerDevis(
            @Parameter(description = "Numéro du devis", example = "DEV-20260609-1592")
            @PathVariable String quoteNumber) {
        if (!quoteRepository.existsById(quoteNumber)) {
            return ResponseEntity.notFound().build();
        }
        correctionRepository.deleteByQuoteNumber(quoteNumber);
        quoteRepository.deleteById(quoteNumber);
        return ResponseEntity.ok(Map.of(
            "status", "supprimé",
            "quoteNumber", quoteNumber
        ));
    }

    @Operation(summary = "Liste toutes les corrections apportées par les commerciaux aux extractions IA")
    @GetMapping("/corrections")
    public ResponseEntity<List<CorrectionIA>> getCorrections() {
        return ResponseEntity.ok(correctionRepository.findAllByOrderByCreatedAtDesc());
    }

    @Operation(
        summary = "Déclencher le scan email immédiatement",
        description = "Lance manuellement la lecture de la boîte mail sans attendre le délai de 5 minutes. "
                    + "Utile pendant les tests. Requiert que email.imap.enabled soit true."
    )
    @ApiResponse(responseCode = "200", description = "Scan déclenché — voir les logs pour le résultat")
    @PostMapping("/scan-emails")
    public ResponseEntity<Map<String, String>> scanEmails() {
        emailReaderService.lireEmailsNonLus();
        return ResponseEntity.ok(Map.of(
            "status", "scan déclenché",
            "message", "Consultez les logs [EmailReader] dans la console pour voir le résultat."
        ));
    }
}
