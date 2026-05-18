package com.projetdevis.dto;

import com.projetdevis.model.Produit;

/**
 * DTO de réponse pour GET /api/produits.
 */
public class ProduitResponse {

    private Long   id;
    private String nom;
    private String categorie;
    private String description;
    private String reference;
    private Double prixEconomique;
    private Double prixStandard;
    private Double prixPremium;

    public static ProduitResponse from(Produit p) {
        ProduitResponse r = new ProduitResponse();
        r.id            = p.getId();
        r.nom           = p.getNom();
        r.categorie     = p.getCategorie() != null ? p.getCategorie().name() : null;
        r.description   = p.getDescription();
        r.reference     = p.getReference();
        r.prixEconomique = p.getPrixEconomique();
        r.prixStandard  = p.getPrixStandard();
        r.prixPremium   = p.getPrixPremium();
        return r;
    }

    public Long   getId()            { return id; }
    public String getNom()           { return nom; }
    public String getCategorie()     { return categorie; }
    public String getDescription()   { return description; }
    public String getReference()     { return reference; }
    public Double getPrixEconomique(){ return prixEconomique; }
    public Double getPrixStandard()  { return prixStandard; }
    public Double getPrixPremium()   { return prixPremium; }
}
