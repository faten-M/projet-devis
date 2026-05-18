package com.projetdevis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corps de la requête POST /api/produits.
 *
 * Permet d'ajouter un nouveau produit au catalogue avec sa grille de prix.
 */
public class ProduitRequest {

    @NotBlank(message = "Le champ 'nom' est obligatoire.")
    private String nom;

    @NotBlank(message = "Le champ 'categorie' est obligatoire.")
    private String categorie;

    private Double prixEconomique;

    @NotNull(message = "Le champ 'prixStandard' est obligatoire.")
    @Positive(message = "Le champ 'prixStandard' doit être supérieur à 0.")
    private Double prixStandard;

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
