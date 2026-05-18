package com.projetdevis.model;

import jakarta.persistence.*;

/**
 * Entité JPA représentant un produit du catalogue.
 *
 * Chaque ligne correspond à une catégorie de mobilier avec trois niveaux de prix :
 * économique, standard, premium.
 *
 * Ces prix remplacent les valeurs codées en dur dans DraftService.
 */
@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom lisible du produit / catégorie (ex : "Bureau") */
    private String nom;

    /** Description courte */
    @Column(length = 1000)
    private String description;

    /** Référence interne optionnelle */
    private String reference;

    /** Catégorie de mobilier (correspond à AnalyzedItem.Category) */
    @Enumerated(EnumType.STRING)
    private AnalyzedItem.Category categorie;

    /** Prix HT entrée de gamme */
    private Double prixEconomique;

    /** Prix HT intermédiaire */
    private Double prixStandard;

    /** Prix HT haut de gamme */
    private Double prixPremium;

    // === CONSTRUCTEURS ===

    public Produit() {}

    public Produit(String nom, AnalyzedItem.Category categorie,
                   double prixEconomique, double prixStandard, double prixPremium) {
        this.nom = nom;
        this.categorie = categorie;
        this.prixEconomique = prixEconomique;
        this.prixStandard = prixStandard;
        this.prixPremium = prixPremium;
    }

    // === GETTERS / SETTERS ===

    public Long getId() { return id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public AnalyzedItem.Category getCategorie() { return categorie; }
    public void setCategorie(AnalyzedItem.Category categorie) { this.categorie = categorie; }

    public Double getPrixEconomique() { return prixEconomique; }
    public void setPrixEconomique(Double prixEconomique) { this.prixEconomique = prixEconomique; }

    public Double getPrixStandard() { return prixStandard; }
    public void setPrixStandard(Double prixStandard) { this.prixStandard = prixStandard; }

    public Double getPrixPremium() { return prixPremium; }
    public void setPrixPremium(Double prixPremium) { this.prixPremium = prixPremium; }

    @Override
    public String toString() {
        return String.format("Produit{categorie=%s, eco=%.0f, std=%.0f, premium=%.0f}",
            categorie, prixEconomique, prixStandard, prixPremium);
    }
}
