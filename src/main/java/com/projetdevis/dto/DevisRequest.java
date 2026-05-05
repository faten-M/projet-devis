package com.projetdevis.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la requête POST /api/devis.
 * Contient le texte brut de l'e-mail à traiter.
 */
public class DevisRequest {

    @NotBlank(message = "Le champ 'emailText' est obligatoire et ne peut pas être vide.")
    private String emailText;

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }
}
