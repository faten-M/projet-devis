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

    /** Nom du client saisi manuellement par le commercial */
    private String nomClient;

    /** Priorité modifiée par le commercial */
    private String priorite;

    /** Budget client révisé après contact */
    private Double budgetClient;

    /** Aperçu du besoin révisé par le commercial */
    private String sujetBesoin;

    /** Date de livraison corrigée manuellement (format ISO : YYYY-MM-DD) */
    private String dateLivraison;

    /** Frais de livraison (null = pas de livraison, retrait en entreprise) */
    private Double deliveryFees;

    /** true = livraison à domicile, false = retrait en entreprise */
    private Boolean deliveryIncluded;

    /** Garantie proposée (ex : "2 ans pièces et main d'œuvre") */
    private String warranty;

    /** Articles mis à jour par le commercial (liste complète) */
    private java.util.List<ItemUpdate> items;

    public static class ItemUpdate {
        private Integer lineNumber;
        private String  designation;
        private Integer quantity;
        private Double  unitPriceHT;
        private Double  discountPercent;

        public Integer getLineNumber()    { return lineNumber; }
        public void setLineNumber(Integer v) { this.lineNumber = v; }
        public String  getDesignation()   { return designation; }
        public void setDesignation(String v) { this.designation = v; }
        public Integer getQuantity()      { return quantity; }
        public void setQuantity(Integer v)   { this.quantity = v; }
        public Double  getUnitPriceHT()   { return unitPriceHT; }
        public void setUnitPriceHT(Double v) { this.unitPriceHT = v; }
        public Double  getDiscountPercent() { return discountPercent; }
        public void setDiscountPercent(Double v) { this.discountPercent = v; }
    }

    // === Getters / Setters ===

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Double getRemiseGlobale() { return remiseGlobale; }
    public void setRemiseGlobale(Double remiseGlobale) { this.remiseGlobale = remiseGlobale; }

    public String getConditionsPaiement() { return conditionsPaiement; }
    public void setConditionsPaiement(String conditionsPaiement) { this.conditionsPaiement = conditionsPaiement; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public Double getBudgetClient() { return budgetClient; }
    public void setBudgetClient(Double budgetClient) { this.budgetClient = budgetClient; }

    public String getSujetBesoin() { return sujetBesoin; }
    public void setSujetBesoin(String sujetBesoin) { this.sujetBesoin = sujetBesoin; }

    public String getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(String dateLivraison) { this.dateLivraison = dateLivraison; }

    public java.util.List<ItemUpdate> getItems() { return items; }
    public void setItems(java.util.List<ItemUpdate> items) { this.items = items; }

    public Double  getDeliveryFees()    { return deliveryFees; }
    public void    setDeliveryFees(Double v) { this.deliveryFees = v; }

    public Boolean getDeliveryIncluded() { return deliveryIncluded; }
    public void    setDeliveryIncluded(Boolean v) { this.deliveryIncluded = v; }

    public String  getWarranty()        { return warranty; }
    public void    setWarranty(String v) { this.warranty = v; }
}
