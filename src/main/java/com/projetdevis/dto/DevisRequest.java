package com.projetdevis.dto;

/**
 * Corps de la requête POST /api/devis.
 * Contient le texte brut de l'e-mail à traiter.
 */
public class DevisRequest {

    private String emailText;

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }
}
