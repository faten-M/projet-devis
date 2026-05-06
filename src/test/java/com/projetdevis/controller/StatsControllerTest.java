package com.projetdevis.controller;

import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import com.projetdevis.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  DraftQuoteRepository quoteRepository;
    @MockBean  ClientRepository clientRepository;
    @MockBean  ProduitRepository produitRepository;

    // ── GET /api/stats ────────────────────────────────────────────────────────

    @Test
    void getStats_baseDonneeVide_retourne200AvecZeros() throws Exception {
        when(quoteRepository.findAll()).thenReturn(List.of());
        when(clientRepository.count()).thenReturn(0L);
        when(produitRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/stats"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalDevis").value(0))
               .andExpect(jsonPath("$.totalClients").value(0))
               .andExpect(jsonPath("$.totalProduits").value(0))
               .andExpect(jsonPath("$.montantTotalHT").value(0.0));
    }
}
