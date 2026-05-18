package com.projetdevis.dto;

/**
 * Corps de la requête PUT /api/devis/{id}/valider.
 *
 * Le commercial peut :
 *  - changer le statut du devis (PRET, REJETE, A_VALIDER, ...)
 *  - ajouter un commentaire
 *  - appliquer une remise globale (%)
 *  - modifier les conditions de paiement
 */
public class ValiderRequest {

    /** Nouveau statut : BROUILLON | A_COMPLETER | A_VALIDER | PRET | REJETE */
    private String statut;

    /** Commentaire libre du validateur */
    private String commentaire;

    /** Remise globale à appliquer à toutes les lignes (ex : 5.0 pour 5%) */
    private Double remiseGlobale;

    /** Nouvelles conditions de paiement */
    private String conditionsPaiement;

    // === Getters / Setters ===

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Double getRemiseGlobale() { return remiseGlobale; }
    public void setRemiseGlobale(Double remiseGlobale) { this.remiseGlobale = remiseGlobale; }

    public String getConditionsPaiement() { return conditionsPaiement; }
    public void setConditionsPaiement(String conditionsPaiement) { this.conditionsPaiement = conditionsPaiement; }
}
