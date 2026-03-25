package com.projetdevis.controller;

import com.projetdevis.dto.DevisRequest;
import com.projetdevis.dto.DevisResponse;
import com.projetdevis.dto.ValiderRequest;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.service.DevisPipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST exposant le pipeline de génération de devis.
 *
 * Endpoint :  POST /api/devis
 * Corps      : { "emailText": "..." }
 * Réponse    : devis brouillon en JSON (DevisResponse)
 */
@RestController
@RequestMapping("/api")
public class DevisController {

    private final DevisPipelineService pipelineService;

    public DevisController(DevisPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * Traite un e-mail et retourne un devis brouillon en JSON.
     *
     * @param request corps JSON contenant le texte de l'e-mail
     * @return 200 + DevisResponse, ou 400 si l'e-mail est vide,
     *         ou 503 si la clé OpenAI est manquante
     */
    @PostMapping("/devis")
    public ResponseEntity<?> createDevis(@RequestBody DevisRequest request) {

        if (request.getEmailText() == null || request.getEmailText().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Le champ 'emailText' est obligatoire et ne peut pas être vide.");
        }

        try {
            DraftQuote draft = pipelineService.process(request.getEmailText());
            return ResponseEntity.ok(DevisResponse.from(draft));

        } catch (IllegalStateException e) {
            // OPENAI_API_KEY manquante
            return ResponseEntity.status(503)
                    .body("Service indisponible : " + e.getMessage());
        }
    }

    /**
     * Valide ou modifie un devis (statut, remise, conditions de paiement).
     *
     * PUT /api/devis/{quoteNumber}/valider
     * Corps : { "statut": "PRET", "commentaire": "...", "remiseGlobale": 5.0 }
     */
    @PutMapping("/devis/{quoteNumber}/valider")
    public ResponseEntity<?> validerDevis(@PathVariable String quoteNumber,
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

    /**
     * Retourne tous les devis sauvegardés en base.
     *
     * GET /api/devis
     */
    @GetMapping("/devis")
    public ResponseEntity<List<DevisResponse>> getAllDevis() {
        List<DevisResponse> list = pipelineService.findAll().stream()
                .map(DevisResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Retourne un devis par son numéro.
     *
     * GET /api/devis/{quoteNumber}
     */
    @GetMapping("/devis/{quoteNumber}")
    public ResponseEntity<?> getDevis(@PathVariable String quoteNumber) {
        return pipelineService.findByQuoteNumber(quoteNumber)
                .map(DevisResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
