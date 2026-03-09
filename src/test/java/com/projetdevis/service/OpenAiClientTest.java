package com.projetdevis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiClientTest {

    @Mock
    private HttpClient mockHttpClient;

    @SuppressWarnings("unchecked")
    @Mock
    private HttpResponse<String> mockResponse;

    // ---------------------------------------------------------------
    // Test 1 : cas nominal — la signature est bien retirée
    // ---------------------------------------------------------------
    @Test
    void retirerSignature_retourneTexteNettoye() throws Exception {
        String jsonReponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "Bonjour,\\nVoici ma demande."
                      }
                    }
                  ]
                }
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonReponse);
        // doReturn évite le conflit de type générique avec HttpClient.send()
        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        OpenAiClient client = new OpenAiClient(mockHttpClient, "fake-api-key");

        String result = client.retirerSignature("Bonjour,\nVoici ma demande.\nCordialement,\nJean");

        assertEquals("Bonjour,\nVoici ma demande.", result);
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    // ---------------------------------------------------------------
    // Test 2 : choices vide — retourne une chaîne vide
    // ---------------------------------------------------------------
    @Test
    void retirerSignature_retourneVideSiChoicesVide() throws Exception {
        String jsonReponse = """
                {
                  "choices": []
                }
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonReponse);
        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        OpenAiClient client = new OpenAiClient(mockHttpClient, "fake-api-key");

        String result = client.retirerSignature("email quelconque");

        assertEquals("", result);
    }

    // ---------------------------------------------------------------
    // Test 3 : statut HTTP erreur — lève une RuntimeException
    // ---------------------------------------------------------------
    @Test
    void retirerSignature_leveExceptionSiStatutErreur() throws Exception {
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"error\": \"Unauthorized\"}");
        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        OpenAiClient client = new OpenAiClient(mockHttpClient, "fake-api-key");

        assertThrows(RuntimeException.class, () -> client.retirerSignature("email quelconque"));
    }

    // ---------------------------------------------------------------
    // Test 4 : IOException — lève une RuntimeException wrappée
    // ---------------------------------------------------------------
    @Test
    void retirerSignature_leveExceptionSiIOException() throws Exception {
        doThrow(new java.io.IOException("Connexion refusée"))
                .when(mockHttpClient).send(any(HttpRequest.class), any());

        OpenAiClient client = new OpenAiClient(mockHttpClient, "fake-api-key");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.retirerSignature("email quelconque"));
        assertTrue(ex.getMessage().contains("OpenAI"));
    }
}
