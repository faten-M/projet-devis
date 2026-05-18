package com.projetdevis.controller;

import com.projetdevis.model.Client;
import com.projetdevis.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ClientRepository clientRepository;

    // ── GET /api/clients ──────────────────────────────────────────────────────

    @Test
    void getAllClients_listeVide_retourne200() throws Exception {
        when(clientRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllClients_avecClients_retourneListe() throws Exception {
        Client c = new Client("Dupont BTP");
        when(clientRepository.findAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/clients"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].raisonSociale").value("Dupont BTP"));
    }

    // ── GET /api/clients/{id} ─────────────────────────────────────────────────

    @Test
    void getClient_idInconnu_retourne404() throws Exception {
        when(clientRepository.findById("CLI-INCONNU")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clients/CLI-INCONNU"))
               .andExpect(status().isNotFound());
    }

    @Test
    void getClient_idConnu_retourne200() throws Exception {
        Client c = new Client("Dupont BTP");
        when(clientRepository.findById(c.getClientId())).thenReturn(Optional.of(c));

        mockMvc.perform(get("/api/clients/" + c.getClientId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.raisonSociale").value("Dupont BTP"));
    }
}
