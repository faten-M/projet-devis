package com.projetdevis.controller;

import com.projetdevis.dto.DevisRequest;
import com.projetdevis.dto.DevisResponse;
import com.projetdevis.dto.ValiderRequest;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.service.DevisPipelineService;
import com.projetdevis.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Devis", description = "Génération, consultation et validation des devis via pipeline IA")
@RestController
@RequestMapping("/api")
public class DevisController {

    private final DevisPipelineService pipelineService;
    private final PdfService           pdfService;

    public DevisController(DevisPipelineService pipelineService, PdfService pdfService) {
        this.pipelineService = pipelineService;
        this.pdfService      = pdfService;
    }

    @Operation(
        summary = "Soumettre un e-mail BTP",
        description = "Traite un e-mail client BTP et génère un devis brouillon via pipeline IA (nettoyage → extraction → analyse → tarification).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Devis généré avec succès"),
            @ApiResponse(responseCode = "400", description = "E-mail vide ou invalide"),
            @ApiResponse(responseCode = "503", description = "Clé OpenAI manquante ou service IA indisponible")
        }
    )
    @PostMapping("/devis")
    public ResponseEntity<?> createDevis(@Valid @RequestBody DevisRequest request) {
        try {
            DraftQuote draft = pipelineService.process(request.getEmailText());
            return ResponseEntity.ok(DevisResponse.from(draft));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503)
                    .body("Service indisponible : " + e.getMessage());
        }
    }

    @Operation(
        summary = "Valider ou modifier un devis",
        description = "Met à jour le statut, la remise globale ou le commentaire d'un devis existant.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Devis mis à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Devis introuvable")
        }
    )
    @PutMapping("/devis/{quoteNumber}/valider")
    public ResponseEntity<?> validerDevis(
            @Parameter(description = "Numéro du devis", example = "DEV-20260504-1234")
            @PathVariable String quoteNumber,
                                          @RequestBody ValiderRequest request) {
        try {
            DraftQuote updated = pipelineService.valider(quoteNumber, request);
            return ResponseEntity.ok(DevisResponse.from(updated));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Liste tous les devis", description = "Retourne tous les devis enregistrés en base de données.")
    @GetMapping("/devis")
    public ResponseEntity<List<DevisResponse>> getAllDevis() {
        List<DevisResponse> list = pipelineService.findAll().stream()
                .map(DevisResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Détail d'un devis", description = "Retourne un devis par son numéro unique.")
    @GetMapping("/devis/{quoteNumber}")
    public ResponseEntity<?> getDevis(
            @Parameter(description = "Numéro du devis", example = "DEV-20260504-1234")
            @PathVariable String quoteNumber) {
        return pipelineService.findByQuoteNumber(quoteNumber)
                .map(DevisResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Télécharger le PDF d'un devis",
        description = "Génère et retourne un PDF prêt à envoyer au client.",
        responses = {
            @ApiResponse(responseCode = "200", description = "PDF généré"),
            @ApiResponse(responseCode = "404", description = "Devis introuvable")
        }
    )
    @GetMapping("/devis/{quoteNumber}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(
            @Parameter(description = "Numéro du devis", example = "DEV-20260504-1234")
            @PathVariable String quoteNumber) {
        return pipelineService.findByQuoteNumber(quoteNumber)
                .map(devis -> {
                    if (devis.getStatus() != DraftQuote.DraftStatus.PRET) {
                        return ResponseEntity.status(403)
                                .<byte[]>body(null);
                    }
                    byte[] pdf = pdfService.genererPdfDevis(devis);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"devis-" + quoteNumber + ".pdf\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(pdf);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
