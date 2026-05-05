package com.projetdevis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetdevis.dto.DevisRequest;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.service.DevisPipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevisController.class)
class DevisControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  DevisPipelineService pipelineService;

    // ── Helper ───────────────────────────────────────────────────────────────

    private DraftQuote devisMinimal(String numero) {
        DraftQuote d = new DraftQuote();
        d.setQuoteNumber(numero);
        return d;
    }

    // ── GET /api/devis ────────────────────────────────────────────────────────

    @Test
    void getAllDevis_listeVide_retourne200AvecTableauVide() throws Exception {
        when(pipelineService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/devis"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllDevis_avecDevis_retourne200AvecListe() throws Exception {
        when(pipelineService.findAll()).thenReturn(List.of(devisMinimal("DEV-TEST-001")));

        mockMvc.perform(get("/api/devis"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].quoteNumber").value("DEV-TEST-001"));
    }

    // ── GET /api/devis/{quoteNumber} ──────────────────────────────────────────

    @Test
    void getDevis_numeroInconnu_retourne404() throws Exception {
        when(pipelineService.findByQuoteNumber("DEV-INCONNU")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/devis/DEV-INCONNU"))
               .andExpect(status().isNotFound());
    }

    @Test
    void getDevis_numeroConnu_retourne200() throws Exception {
        when(pipelineService.findByQuoteNumber("DEV-TEST-001"))
                .thenReturn(Optional.of(devisMinimal("DEV-TEST-001")));

        mockMvc.perform(get("/api/devis/DEV-TEST-001"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.quoteNumber").value("DEV-TEST-001"));
    }

    // ── POST /api/devis ───────────────────────────────────────────────────────

    @Test
    void createDevis_emailVide_retourne400() throws Exception {
        DevisRequest req = new DevisRequest();
        req.setEmailText("");

        mockMvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void createDevis_emailValide_retourne200() throws Exception {
        when(pipelineService.process(anyString())).thenReturn(devisMinimal("DEV-TEST-002"));

        DevisRequest req = new DevisRequest();
        req.setEmailText("Bonjour, je voudrais 100 sacs de ciment.");

        mockMvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.quoteNumber").value("DEV-TEST-002"));
    }
}
