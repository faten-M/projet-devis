package com.projetdevis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetdevis.dto.ProduitRequest;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.Produit;
import com.projetdevis.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProduitController.class)
class ProduitControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ProduitRepository produitRepository;

    // ── GET /api/produits ─────────────────────────────────────────────────────

    @Test
    void getAllProduits_retourne200AvecListe() throws Exception {
        Produit p = new Produit("Gros œuvre", AnalyzedItem.Category.GROS_OEUVRE, 6, 18, 45);
        when(produitRepository.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/produits"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].nom").value("Gros œuvre"));
    }

    // ── POST /api/produits ────────────────────────────────────────────────────

    @Test
    void addProduit_nomManquant_retourne400() throws Exception {
        ProduitRequest req = new ProduitRequest();
        req.setCategorie("GROS_OEUVRE");
        req.setPrixStandard(18.0);

        mockMvc.perform(post("/api/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void addProduit_categorieInvalide_retourne400() throws Exception {
        ProduitRequest req = new ProduitRequest();
        req.setNom("Test");
        req.setCategorie("BUREAU");   // ancienne catégorie mobilier — invalide
        req.setPrixStandard(50.0);

        mockMvc.perform(post("/api/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void addProduit_valide_retourne201() throws Exception {
        ProduitRequest req = new ProduitRequest();
        req.setNom("Ciment CEM II");
        req.setCategorie("GROS_OEUVRE");
        req.setPrixStandard(18.0);

        Produit saved = new Produit("Ciment CEM II", AnalyzedItem.Category.GROS_OEUVRE, 18, 18, 18);
        when(produitRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.nom").value("Ciment CEM II"));
    }
}
