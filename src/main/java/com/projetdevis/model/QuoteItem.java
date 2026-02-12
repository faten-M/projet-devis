package com.projetdevis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente une ligne du brouillon de devis.
 *
 * Cette classe transforme un AnalyzedItem en ligne de devis commerciale avec :
 * - Désignation formatée du produit
 * - Prix unitaire estimé
 * - Quantité et montant total
 * - Options et variantes possibles
 * - Notes internes pour le commercial
 *
 * Pipeline BMAD - Étape 5 : Brouillon du devis
 *
 * @author BMAD Pipeline - Étape 5
 * @version 1.0
 */
public class QuoteItem {

    // === ÉNUMÉRATIONS ===

    /**
     * Gamme de prix du produit.
     */
    public enum PriceRange {
        ECONOMIQUE("Économique", "Entrée de gamme, rapport qualité/prix optimal"),
        STANDARD("Standard", "Gamme intermédiaire, bon compromis"),
        PREMIUM("Premium", "Haut de gamme, qualité supérieure"),
        LUXE("Luxe", "Gamme prestige, matériaux nobles"),
        NON_DEFINI("Non défini", "Gamme à préciser avec le client");

        private final String label;
        private final String description;

        PriceRange(String label, String description) {
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
     * Statut de la ligne de devis.
     */
    public enum LineStatus {
        COMPLETE("Complète", "Toutes les informations sont disponibles"),
        A_COMPLETER("À compléter", "Informations manquantes à préciser"),
        A_VALIDER("À valider", "Nécessite validation client"),
        OPTIONNEL("Optionnel", "Article optionnel, non inclus par défaut");

        private final String label;
        private final String description;

        LineStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }
    }

    // === ATTRIBUTS ===

    /** Numéro de ligne (position dans le devis) */
    private int lineNumber;

    /** Référence interne du produit */
    private String reference;

    /** Désignation complète du produit */
    private String designation;

    /** Description détaillée */
    private String description;

    /** Catégorie du produit */
    private AnalyzedItem.Category category;

    /** Quantité */
    private int quantity;

    /** Unité de mesure */
    private String unit;

    /** Prix unitaire HT estimé */
    private Double unitPriceHT;

    /** Prix unitaire TTC estimé */
    private Double unitPriceTTC;

    /** Montant total HT de la ligne */
    private Double totalPriceHT;

    /** Montant total TTC de la ligne */
    private Double totalPriceTTC;

    /** Taux de TVA appliqué */
    private double tvaRate;

    /** Gamme de prix */
    private PriceRange priceRange;

    /** Statut de la ligne */
    private LineStatus status;

    /** Remise applicable (pourcentage) */
    private Double discountPercent;

    /** Délai de livraison estimé (jours) */
    private Integer deliveryDays;

    /** Options disponibles */
    private List<String> options;

    /** Alternatives suggérées */
    private List<String> alternatives;

    /** Notes internes pour le commercial */
    private List<String> internalNotes;

    /** Avertissements à afficher */
    private List<String> warnings;

    /** Article source (AnalyzedItem) */
    private AnalyzedItem sourceItem;

    /** Confiance dans l'estimation de prix */
    private double priceConfidence;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public QuoteItem() {
        this.unit = "unité";
        this.tvaRate = 20.0;
        this.priceRange = PriceRange.NON_DEFINI;
        this.status = LineStatus.A_COMPLETER;
        this.options = new ArrayList<>();
        this.alternatives = new ArrayList<>();
        this.internalNotes = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.priceConfidence = 0.0;
    }

    /**
     * Constructeur à partir d'un AnalyzedItem.
     *
     * @param source Article analysé source
     * @param lineNumber Numéro de ligne
     */
    public QuoteItem(AnalyzedItem source, int lineNumber) {
        this();
        this.lineNumber = lineNumber;
        this.sourceItem = source;

        if (source != null) {
            this.category = source.getCategory();
            this.quantity = source.getQuantity() != null ? source.getQuantity() : 1;
            this.designation = buildDesignation(source);
            this.description = buildDescription(source);
            this.reference = generateReference(source, lineNumber);
        }
    }

    // === MÉTHODES DE CONSTRUCTION ===

    /**
     * Construit la désignation à partir de l'article analysé.
     */
    private String buildDesignation(AnalyzedItem item) {
        StringBuilder sb = new StringBuilder();

        // Nom du produit avec majuscule
        String product = item.getProduct();
        if (product != null && !product.isEmpty()) {
            sb.append(Character.toUpperCase(product.charAt(0)));
            sb.append(product.substring(1));
        }

        // Ajout de la couleur si présente
        if (item.getColor() != null && !item.getColor().isBlank()) {
            sb.append(" ").append(item.getColor());
        }

        // Ajout du matériau si présent
        if (item.getMaterial() != null && !item.getMaterial().isBlank()) {
            sb.append(" ").append(item.getMaterial());
        }

        return sb.toString().trim();
    }

    /**
     * Construit la description détaillée.
     */
    private String buildDescription(AnalyzedItem item) {
        List<String> details = new ArrayList<>();

        // Dimensions
        if (item.getDimensions() != null && !item.getDimensions().isBlank()) {
            details.add("Dimensions : " + item.getDimensions());
        }

        // Modèle
        if (item.getModel() != null && !item.getModel().isBlank()) {
            details.add("Modèle : " + item.getModel());
        }

        // Caractéristiques
        if (item.hasCharacteristics()) {
            details.add("Caractéristiques : " + String.join(", ", item.getCharacteristics()));
        }

        return details.isEmpty() ? null : String.join(" | ", details);
    }

    /**
     * Génère une référence unique pour la ligne.
     */
    private String generateReference(AnalyzedItem item, int lineNum) {
        String catCode = switch (item.getCategory()) {
            case BUREAU -> "BUR";
            case SIEGE -> "SIG";
            case RANGEMENT -> "RNG";
            case TABLE -> "TAB";
            case ECLAIRAGE -> "ECL";
            case ACCESSOIRE -> "ACC";
            case ESPACE_DETENTE -> "DET";
            case CLOISON -> "CLO";
            case AUTRE -> "DIV";
        };
        return String.format("%s-%03d", catCode, lineNum);
    }

    // === GETTERS ET SETTERS ===

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AnalyzedItem.Category getCategory() {
        return category;
    }

    public void setCategory(AnalyzedItem.Category category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getUnitPriceHT() {
        return unitPriceHT;
    }

    public void setUnitPriceHT(Double unitPriceHT) {
        this.unitPriceHT = unitPriceHT;
        updateCalculatedPrices();
    }

    public Double getUnitPriceTTC() {
        return unitPriceTTC;
    }

    public void setUnitPriceTTC(Double unitPriceTTC) {
        this.unitPriceTTC = unitPriceTTC;
    }

    public Double getTotalPriceHT() {
        return totalPriceHT;
    }

    public void setTotalPriceHT(Double totalPriceHT) {
        this.totalPriceHT = totalPriceHT;
    }

    public Double getTotalPriceTTC() {
        return totalPriceTTC;
    }

    public void setTotalPriceTTC(Double totalPriceTTC) {
        this.totalPriceTTC = totalPriceTTC;
    }

    public double getTvaRate() {
        return tvaRate;
    }

    public void setTvaRate(double tvaRate) {
        this.tvaRate = tvaRate;
        updateCalculatedPrices();
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }

    public LineStatus getStatus() {
        return status;
    }

    public void setStatus(LineStatus status) {
        this.status = status;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
        updateCalculatedPrices();
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public void addOption(String option) {
        if (option != null && !option.isBlank()) {
            this.options.add(option);
        }
    }

    public List<String> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<String> alternatives) {
        this.alternatives = alternatives != null ? alternatives : new ArrayList<>();
    }

    public void addAlternative(String alternative) {
        if (alternative != null && !alternative.isBlank()) {
            this.alternatives.add(alternative);
        }
    }

    public List<String> getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(List<String> internalNotes) {
        this.internalNotes = internalNotes != null ? internalNotes : new ArrayList<>();
    }

    public void addInternalNote(String note) {
        if (note != null && !note.isBlank()) {
            this.internalNotes.add(note);
        }
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

    public AnalyzedItem getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(AnalyzedItem sourceItem) {
        this.sourceItem = sourceItem;
    }

    public double getPriceConfidence() {
        return priceConfidence;
    }

    public void setPriceConfidence(double priceConfidence) {
        this.priceConfidence = Math.max(0.0, Math.min(1.0, priceConfidence));
    }

    // === MÉTHODES DE CALCUL ===

    /**
     * Met à jour les prix calculés (TTC et totaux).
     */
    private void updateCalculatedPrices() {
        if (unitPriceHT != null) {
            // Calcul du prix TTC
            this.unitPriceTTC = unitPriceHT * (1 + tvaRate / 100);

            // Calcul des totaux
            double baseTotal = unitPriceHT * quantity;

            // Application de la remise si présente
            if (discountPercent != null && discountPercent > 0) {
                baseTotal = baseTotal * (1 - discountPercent / 100);
            }

            this.totalPriceHT = baseTotal;
            this.totalPriceTTC = baseTotal * (1 + tvaRate / 100);
        }
    }

    /**
     * Calcule le montant de la TVA.
     *
     * @return Montant TVA ou null si pas de prix
     */
    public Double getTvaAmount() {
        if (totalPriceHT == null || totalPriceTTC == null) {
            return null;
        }
        return totalPriceTTC - totalPriceHT;
    }

    /**
     * Calcule le montant de la remise.
     *
     * @return Montant de la remise ou null
     */
    public Double getDiscountAmount() {
        if (unitPriceHT == null || discountPercent == null) {
            return null;
        }
        return unitPriceHT * quantity * discountPercent / 100;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si la ligne est complète.
     *
     * @return true si toutes les informations essentielles sont présentes
     */
    public boolean isComplete() {
        return designation != null && !designation.isBlank() &&
               quantity > 0 &&
               unitPriceHT != null &&
               status == LineStatus.COMPLETE;
    }

    /**
     * Vérifie si la ligne a un prix défini.
     *
     * @return true si un prix est défini
     */
    public boolean hasPrice() {
        return unitPriceHT != null;
    }

    /**
     * Vérifie si la ligne a des avertissements.
     *
     * @return true si des avertissements sont présents
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Vérifie si la ligne a des options.
     *
     * @return true si des options sont disponibles
     */
    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    /**
     * Vérifie si la ligne a des alternatives.
     *
     * @return true si des alternatives sont suggérées
     */
    public boolean hasAlternatives() {
        return alternatives != null && !alternatives.isEmpty();
    }

    /**
     * Retourne le prix unitaire HT formaté.
     *
     * @return Prix formaté
     */
    public String getFormattedUnitPriceHT() {
        if (unitPriceHT == null) {
            return "À définir";
        }
        return String.format("%,.2f € HT", unitPriceHT);
    }

    /**
     * Retourne le prix total HT formaté.
     *
     * @return Prix formaté
     */
    public String getFormattedTotalPriceHT() {
        if (totalPriceHT == null) {
            return "À définir";
        }
        return String.format("%,.2f € HT", totalPriceHT);
    }

    /**
     * Retourne le prix total TTC formaté.
     *
     * @return Prix formaté
     */
    public String getFormattedTotalPriceTTC() {
        if (totalPriceTTC == null) {
            return "À définir";
        }
        return String.format("%,.2f € TTC", totalPriceTTC);
    }

    /**
     * Génère un résumé de la ligne.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append(reference != null ? reference : "---");
        sb.append(" | ");
        sb.append(quantity).append(" x ");
        sb.append(designation != null ? designation : "???");
        sb.append(" | ");
        sb.append(getFormattedTotalPriceHT());

        if (status != LineStatus.COMPLETE) {
            sb.append(" [").append(status.getLabel()).append("]");
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteItem that = (QuoteItem) o;
        return lineNumber == that.lineNumber &&
               Objects.equals(reference, that.reference) &&
               Objects.equals(designation, that.designation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, reference, designation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("QuoteItem {\n");
        sb.append("  lineNumber: ").append(lineNumber).append("\n");
        sb.append("  reference: \"").append(reference).append("\"\n");
        sb.append("  designation: \"").append(designation).append("\"\n");
        sb.append("  category: ").append(category != null ? category.getLabel() : "null").append("\n");
        sb.append("  quantity: ").append(quantity).append(" ").append(unit).append("\n");

        if (unitPriceHT != null) {
            sb.append("  unitPriceHT: ").append(getFormattedUnitPriceHT()).append("\n");
            sb.append("  totalPriceHT: ").append(getFormattedTotalPriceHT()).append("\n");
        }

        sb.append("  priceRange: ").append(priceRange.getLabel()).append("\n");
        sb.append("  status: ").append(status.getLabel()).append("\n");

        if (discountPercent != null) {
            sb.append("  discount: ").append(discountPercent).append("%\n");
        }
        if (deliveryDays != null) {
            sb.append("  deliveryDays: ").append(deliveryDays).append("\n");
        }
        if (!options.isEmpty()) {
            sb.append("  options: ").append(options).append("\n");
        }
        if (!warnings.isEmpty()) {
            sb.append("  warnings: ").append(warnings).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
