package com.projetdevis.service;

import com.projetdevis.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour CrmService.
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * Couverture :
 *  - Extraction d'email depuis un texte brut
 *  - Extraction d'un numéro de téléphone français
 *  - Création d'un client et récupération par email
 *  - Recherche d'un email inexistant (retourne null)
 */
class CrmServiceTest {

    private CrmService service;

    @BeforeEach
    void setUp() {
        service = new CrmService();
    }

    // ── Extraction depuis l'email ─────────────────────────────────────────────

    @Test
    void extractClientInfo_emailPresent_emailExtrait() {
        String emailContent =
                "Bonjour,\n" +
                "Je suis Jean Dupont de TechCorp.\n" +
                "Merci de me contacter à jean.dupont@techcorp.fr\n" +
                "Cordialement,\nJean Dupont";

        Map<String, String> info = service.extractClientInfoFromEmail(emailContent);

        assertNotNull(info.get("email"),
                "Un email doit être extrait du contenu");
        assertEquals("jean.dupont@techcorp.fr", info.get("email"),
                "L'adresse email extraite doit correspondre à celle du texte");
    }

    @Test
    void extractClientInfo_telephoneFrancais_telephoneExtrait() {
        String emailContent =
                "Pour me joindre : 01 23 45 67 89\n" +
                "ou par email : contact@example.fr";

        Map<String, String> info = service.extractClientInfoFromEmail(emailContent);

        // normalizePhone() reformate "0123456789" → "01 23 45 67 89"
        assertEquals("01 23 45 67 89", info.get("telephone"),
                "Le numéro doit être normalisé au format «XX XX XX XX XX»");
    }

    @Test
    void extractClientInfo_contentVide_mapVide() {
        Map<String, String> info = service.extractClientInfoFromEmail("   ");

        assertTrue(info.isEmpty(),
                "Un contenu vide doit retourner une map vide");
    }

    // ── Gestion des clients ───────────────────────────────────────────────────

    @Test
    void createClient_puisFindByEmail_retourneLeClient() {
        service.createClient("TechCorp SAS", "contact@techcorp.fr");

        Client found = service.findClientByEmail("contact@techcorp.fr");

        assertNotNull(found, "Le client créé doit être retrouvé par son email");
        assertEquals("TechCorp SAS", found.getRaisonSociale(),
                "La raison sociale doit correspondre");
    }

    @Test
    void findClientByEmail_emailInconnu_retourneNull() {
        Client found = service.findClientByEmail("inconnu@nowhere.com");

        assertNull(found, "Un email inexistant doit retourner null");
    }
}
