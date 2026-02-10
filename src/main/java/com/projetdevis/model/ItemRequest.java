package com.projetdevis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente un article demandé dans un email de devis.
 *
 * Cette classe encapsule toutes les informations relatives à un produit :
 * - Nom/description du produit
 * - Quantité demandée
 * - Dimensions (longueur, largeur, hauteur)
 * - Modèle ou référence
 * - Matière/matériau
 * - Couleur
 * - Caractéristiques additionnelles
 *
 * Exemple d'utilisation :
 * <pre>
 * ItemRequest item = new ItemRequest();
 * item.setProduct("bureau assis-debout");
 * item.setQuantity(25);
 * item.setDimensions("160x80cm");
 * item.setMaterial("chêne clair");
 * </pre>
 *
 * @author BMAD Pipeline - Étape 3
 * @version 1.0
 */
public class ItemRequest {

    // === ATTRIBUTS ===

    /** Nom ou description du produit demandé */
    private String product;

    /** Quantité demandée (null si non spécifiée) */
    private Integer quantity;

    /** Dimensions sous forme de texte (ex: "160x80cm", "2m x 1m") */
    private String dimensions;

    /** Modèle ou référence du produit */
    private String model;

    /** Matière ou matériau du produit */
    private String material;

    /** Couleur du produit */
    private String color;

    /** Liste des caractéristiques additionnelles */
    private List<String> characteristics;

    /** Ligne originale d'où l'article a été extrait (pour traçabilité) */
    private String rawLine;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     * Initialise la liste des caractéristiques.
     */
    public ItemRequest() {
        this.characteristics = new ArrayList<>();
    }

    /**
     * Constructeur avec produit et quantité.
     *
     * @param product Nom du produit
     * @param quantity Quantité demandée
     */
    public ItemRequest(String product, Integer quantity) {
        this();
        this.product = product;
        this.quantity = quantity;
    }

    // === GETTERS ET SETTERS ===

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics != null ? characteristics : new ArrayList<>();
    }

    /**
     * Ajoute une caractéristique à la liste.
     *
     * @param characteristic Caractéristique à ajouter
     */
    public void addCharacteristic(String characteristic) {
        if (characteristic != null && !characteristic.isBlank()) {
            this.characteristics.add(characteristic.trim());
        }
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si l'article a au moins un produit défini.
     *
     * @return true si le produit est défini
     */
    public boolean isValid() {
        return product != null && !product.isBlank();
    }

    /**
     * Vérifie si l'article a des informations de dimensions.
     *
     * @return true si les dimensions sont définies
     */
    public boolean hasDimensions() {
        return dimensions != null && !dimensions.isBlank();
    }

    /**
     * Vérifie si l'article a des caractéristiques.
     *
     * @return true si au moins une caractéristique est définie
     */
    public boolean hasCharacteristics() {
        return characteristics != null && !characteristics.isEmpty();
    }

    /**
     * Retourne un résumé compact de l'article.
     * Format : "quantité x produit (détails)"
     *
     * @return Résumé de l'article
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        // Quantité x Produit
        if (quantity != null) {
            sb.append(quantity).append(" x ");
        }
        sb.append(product != null ? product : "Produit non spécifié");

        // Détails entre parenthèses
        List<String> details = new ArrayList<>();
        if (color != null && !color.isBlank()) {
            details.add(color);
        }
        if (material != null && !material.isBlank()) {
            details.add(material);
        }
        if (dimensions != null && !dimensions.isBlank()) {
            details.add(dimensions);
        }
        if (model != null && !model.isBlank()) {
            details.add("modèle: " + model);
        }

        if (!details.isEmpty()) {
            sb.append(" (").append(String.join(", ", details)).append(")");
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemRequest that = (ItemRequest) o;
        return Objects.equals(product, that.product) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(dimensions, that.dimensions) &&
               Objects.equals(model, that.model) &&
               Objects.equals(material, that.material) &&
               Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity, dimensions, model, material, color);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ItemRequest {\n");
        sb.append("  product: ").append(product != null ? "\"" + product + "\"" : "null").append("\n");
        sb.append("  quantity: ").append(quantity).append("\n");

        if (dimensions != null) {
            sb.append("  dimensions: \"").append(dimensions).append("\"\n");
        }
        if (model != null) {
            sb.append("  model: \"").append(model).append("\"\n");
        }
        if (material != null) {
            sb.append("  material: \"").append(material).append("\"\n");
        }
        if (color != null) {
            sb.append("  color: \"").append(color).append("\"\n");
        }
        if (characteristics != null && !characteristics.isEmpty()) {
            sb.append("  characteristics: ").append(characteristics).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
