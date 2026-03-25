package com.projetdevis.dto;

/**
 * Corps de la requête POST /api/produits.
 *
 * Permet d'ajouter un nouveau produit au catalogue avec sa grille de prix.
 */
public class ProduitRequest {

    /** Nom lisible du produit (ex : "Bureau assis-debout") */
    private String nom;

    /**
     * Catégorie : BUREAU | SIEGE | RANGEMENT | TABLE |
     *             ECLAIRAGE | ACCESSOIRE | ESPACE_DETENTE | CLOISON | AUTRE
     */
    private String categorie;

    /** Prix HT entrée de gamme */
    private Double prixEconomique;

    /** Prix HT intermédiaire */
    private Double prixStandard;

    /** Prix HT haut de gamme */
    private Double prixPremium;

    /** Description courte (optionnel) */
    private String description;

    /** Référence interne (optionnel) */
    private String reference;

    // === Getters / Setters ===

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public Double getPrixEconomique() { return prixEconomique; }
    public void setPrixEconomique(Double prixEconomique) { this.prixEconomique = prixEconomique; }

    public Double getPrixStandard() { return prixStandard; }
    public void setPrixStandard(Double prixStandard) { this.prixStandard = prixStandard; }

    public Double getPrixPremium() { return prixPremium; }
    public void setPrixPremium(Double prixPremium) { this.prixPremium = prixPremium; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
