package com.projetdevis.controller;

import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.CorrectionIARepository;
import com.projetdevis.repository.DraftQuoteRepository;
import com.projetdevis.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  DraftQuoteRepository   quoteRepository;
    @MockBean  ClientRepository       clientRepository;
    @MockBean  ProduitRepository      produitRepository;
    @MockBean  CorrectionIARepository correctionRepository;

    // ── GET /api/stats — base vide ────────────────────────────────────────────

    @Test
    void getStats_baseDonneeVide_retourne200AvecZeros() throws Exception {
        when(quoteRepository.findAll()).thenReturn(List.of());
        when(clientRepository.count()).thenReturn(0L);
        when(produitRepository.count()).thenReturn(0L);
        when(correctionRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/stats"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalDevis").value(0))
               .andExpect(jsonPath("$.totalClients").value(0))
               .andExpect(jsonPath("$.totalProduits").value(0))
               .andExpect(jsonPath("$.montantTotalHT").value(0.0))
               .andExpect(jsonPath("$.totalCorrections").value(0))
               .andExpect(jsonPath("$.tauxPrecisionIa").value(100.0));
    }

    // ── GET /api/stats — précision IA avec corrections ────────────────────────

    @Test
    void getStats_avecCorrections_tauxPrecisionCalculeCorrectement() throws Exception {
        // Un devis PRET avec 9 lignes → 1 correction → précision = 88.9%
        DraftQuote devisPret = devisPret(9);
        when(quoteRepository.findAll()).thenReturn(List.of(devisPret));
        when(clientRepository.count()).thenReturn(1L);
        when(produitRepository.count()).thenReturn(5L);
        when(correctionRepository.count()).thenReturn(1L);

        mockMvc.perform(get("/api/stats"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalCorrections").value(1))
               .andExpect(jsonPath("$.tauxPrecisionIa").value(88.9));
    }

    @Test
    void getStats_sansCorrections_precisionEstCent() throws Exception {
        DraftQuote devisPret = devisPret(5);
        when(quoteRepository.findAll()).thenReturn(List.of(devisPret));
        when(clientRepository.count()).thenReturn(1L);
        when(produitRepository.count()).thenReturn(5L);
        when(correctionRepository.count()).thenReturn(0L);

        mockMvc.perform(get("/api/stats"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalCorrections").value(0))
               .andExpect(jsonPath("$.tauxPrecisionIa").value(100.0));
    }

    @Test
    void getStats_tousLesDevisNonValides_precisionEstCent() throws Exception {
        // Devis BROUILLON → pas compté dans les lignes validées
        DraftQuote brouillon = new DraftQuote();
        brouillon.setStatus(DraftQuote.DraftStatus.BROUILLON);

        when(quoteRepository.findAll()).thenReturn(List.of(brouillon));
        when(clientRepository.count()).thenReturn(0L);
        when(produitRepository.count()).thenReturn(0L);
        when(correctionRepository.count()).thenReturn(2L);

        mockMvc.perform(get("/api/stats"))
               .andExpect(status().isOk())
               // 0 lignes validées → précision reste 100% (aucune base de calcul)
               .andExpect(jsonPath("$.tauxPrecisionIa").value(100.0));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private DraftQuote devisPret(int nbLignes) {
        DraftQuote d = new DraftQuote();
        d.setStatus(DraftQuote.DraftStatus.PRET);
        d.setCreatedAt(LocalDateTime.now().minusHours(2));
        d.setModifiedAt(LocalDateTime.now());
        for (int i = 1; i <= nbLignes; i++) {
            QuoteItem item = new QuoteItem();
            item.setLineNumber(i);
            item.setDesignation("Article " + i);
            item.setQuantity(10);
            item.setUnitPriceHT(50.0);
            d.getItems().add(item);
        }
        return d;
    }
}
