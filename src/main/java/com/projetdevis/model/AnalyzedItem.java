package com.projetdevis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente un article analysé, validé et enrichi.
 *
 * Cette classe étend les informations de ItemRequest avec :
 * - Une catégorie de produit (mobilier, rangement, éclairage...)
 * - Un statut de validation
 * - Des avertissements éventuels
 * - Un score de confiance par article
 *
 * Pipeline BMAD - Étape 4 : Analyse
 *
 * @author BMAD Pipeline - Étape 4
 * @version 1.0
 */
public class AnalyzedItem {

    // === ÉNUMÉRATIONS ===

    /**
     * Catégories de produits pour le mobilier de bureau.
     */
    public enum Category {
        BUREAU("Bureau", "Bureaux, postes de travail"),
        SIEGE("Siège", "Chaises, fauteuils, tabourets"),
        RANGEMENT("Rangement", "Armoires, caissons, étagères"),
        TABLE("Table", "Tables de réunion, tables basses"),
        ECLAIRAGE("Éclairage", "Lampes, luminaires"),
        ACCESSOIRE("Accessoire", "Accessoires de bureau"),
        ESPACE_DETENTE("Espace détente", "Canapés, poufs, mobilier lounge"),
        CLOISON("Cloison", "Cloisons, panneaux, séparateurs"),
        AUTRE("Autre", "Produit non catégorisé");

        private final String label;
        private final String description;

        Category(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Statut de validation d'un article.
     */
    public enum ValidationStatus {
        VALID("Valide", "Article correctement identifié"),
        WARNING("Avertissement", "Article valide avec des informations manquantes"),
        INVALID("Invalide", "Article non reconnu ou incohérent");

        private final String label;
        private final String description;

        ValidationStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }
    }

    // === ATTRIBUTS ===

    /** Nom du produit normalisé */
    private String product;

    /** Quantité validée */
    private Integer quantity;

    /** Dimensions normalisées */
    private String dimensions;

    /** Modèle ou référence */
    private String model;

    /** Matériau normalisé */
    private String material;

    /** Couleur normalisée */
    private String color;

    /** Caractéristiques du produit */
    private List<String> characteristics;

    /** Catégorie du produit */
    private Category category;

    /** Statut de validation */
    private ValidationStatus status;

    /** Liste des avertissements */
    private List<String> warnings;

    /** Score de confiance (0.0 à 1.0) */
    private double confidence;

    /** Référence à l'article source (ItemRequest) */
    private ItemRequest sourceItem;

    /** Indique si l'article résulte d'une fusion */
    private boolean merged;

    /** Nombre d'articles fusionnés */
    private int mergedCount;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public AnalyzedItem() {
        this.characteristics = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.status = ValidationStatus.VALID;
        this.category = Category.AUTRE;
        this.confidence = 0.0;
        this.merged = false;
        this.mergedCount = 1;
    }

    /**
     * Constructeur à partir d'un ItemRequest.
     *
     * @param source Article source de l'extraction
     */
    public AnalyzedItem(ItemRequest source) {
        this();
        if (source != null) {
            this.product = source.getProduct();
            this.quantity = source.getQuantity();
            this.dimensions = source.getDimensions();
            this.model = source.getModel();
            this.material = source.getMaterial();
            this.color = source.getColor();
            if (source.getCharacteristics() != null) {
                this.characteristics.addAll(source.getCharacteristics());
            }
            this.sourceItem = source;
        }
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

    public void addCharacteristic(String characteristic) {
        if (characteristic != null && !characteristic.isBlank()) {
            String normalized = characteristic.trim().toLowerCase();
            if (!this.characteristics.contains(normalized)) {
                this.characteristics.add(normalized);
            }
        }
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.isBlank()) {
            this.warnings.add(warning);
        }
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public ItemRequest getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(ItemRequest sourceItem) {
        this.sourceItem = sourceItem;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public int getMergedCount() {
        return mergedCount;
    }

    public void setMergedCount(int mergedCount) {
        this.mergedCount = mergedCount;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si l'article est valide.
     *
     * @return true si le statut est VALID
     */
    public boolean isValid() {
        return status == ValidationStatus.VALID;
    }

    /**
     * Vérifie si l'article a des avertissements.
     *
     * @return true si au moins un avertissement est présent
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Vérifie si l'article a des caractéristiques.
     *
     * @return true si au moins une caractéristique est présente
     */
    public boolean hasCharacteristics() {
        return characteristics != null && !characteristics.isEmpty();
    }

    /**
     * Retourne un résumé de l'article.
     *
     * @return Résumé formaté
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        // Quantité x Produit
        if (quantity != null) {
            sb.append(quantity).append(" x ");
        }
        sb.append(product != null ? product : "Produit inconnu");

        // Catégorie
        sb.append(" [").append(category.getLabel()).append("]");

        // Détails
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

        if (!details.isEmpty()) {
            sb.append(" (").append(String.join(", ", details)).append(")");
        }

        // Indicateur de fusion
        if (merged && mergedCount > 1) {
            sb.append(" [fusionné x").append(mergedCount).append("]");
        }

        return sb.toString();
    }

    /**
     * Calcule une clé unique pour identifier les articles similaires.
     * Utilisé pour le regroupement.
     *
     * @return Clé de regroupement
     */
    public String getGroupingKey() {
        StringBuilder key = new StringBuilder();

        // Produit normalisé
        if (product != null) {
            key.append(product.toLowerCase().trim());
        }

        // Couleur
        if (color != null && !color.isBlank()) {
            key.append("|").append(color.toLowerCase().trim());
        }

        // Matériau
        if (material != null && !material.isBlank()) {
            key.append("|").append(material.toLowerCase().trim());
        }

        // Dimensions
        if (dimensions != null && !dimensions.isBlank()) {
            key.append("|").append(dimensions.toLowerCase().trim());
        }

        return key.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedItem that = (AnalyzedItem) o;
        return Objects.equals(product, that.product) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(color, that.color) &&
               Objects.equals(material, that.material) &&
               Objects.equals(dimensions, that.dimensions) &&
               category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity, color, material, dimensions, category);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AnalyzedItem {\n");
        sb.append("  product: \"").append(product).append("\"\n");
        sb.append("  quantity: ").append(quantity).append("\n");
        sb.append("  category: ").append(category.getLabel()).append("\n");
        sb.append("  status: ").append(status.getLabel()).append("\n");
        sb.append("  confidence: ").append(String.format("%.0f%%", confidence * 100)).append("\n");

        if (color != null) {
            sb.append("  color: \"").append(color).append("\"\n");
        }
        if (material != null) {
            sb.append("  material: \"").append(material).append("\"\n");
        }
        if (dimensions != null) {
            sb.append("  dimensions: \"").append(dimensions).append("\"\n");
        }
        if (!characteristics.isEmpty()) {
            sb.append("  characteristics: ").append(characteristics).append("\n");
        }
        if (!warnings.isEmpty()) {
            sb.append("  warnings: ").append(warnings).append("\n");
        }
        if (merged) {
            sb.append("  merged: true (").append(mergedCount).append(" items)\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
