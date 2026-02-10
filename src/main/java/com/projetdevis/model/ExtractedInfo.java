package com.projetdevis.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Conteneur principal des informations extraites d'un email de devis.
 *
 * Cette classe regroupe toutes les données structurées extraites :
 * - Liste des articles demandés (ItemRequest)
 * - Date de livraison souhaitée
 * - Budget indicatif
 * - Informations complémentaires
 *
 * Elle représente le résultat final de l'étape 3 du pipeline BMAD.
 *
 * Exemple d'utilisation :
 * <pre>
 * ExtractedInfo info = new ExtractedInfo();
 * info.addItem(new ItemRequest("bureau", 25));
 * info.setDeliveryDate(LocalDate.of(2024, 3, 15));
 * info.setBudget(80000.0);
 * info.setBudgetUnit("€ HT");
 * </pre>
 *
 * @author BMAD Pipeline - Étape 3
 * @version 1.0
 */
public class ExtractedInfo {

    // === ATTRIBUTS ===

    /** Liste des articles demandés */
    private List<ItemRequest> items;

    /** Date de livraison souhaitée (parsée) */
    private LocalDate deliveryDate;

    /** Date de livraison sous forme de texte brut (avant parsing) */
    private String deliveryDateRaw;

    /** Budget indicatif (montant numérique) */
    private Double budget;

    /** Unité du budget (€, € HT, € TTC, k€, etc.) */
    private String budgetUnit;

    /** Budget sous forme de texte brut (avant parsing) */
    private String budgetRaw;

    /** Niveau d'urgence détecté (normal, urgent, très urgent) */
    private String urgency;

    /** Notes ou contraintes additionnelles extraites */
    private List<String> additionalNotes;

    /** Texte source utilisé pour l'extraction */
    private String sourceText;

    /** Confiance globale de l'extraction (0.0 à 1.0) */
    private double confidence;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     * Initialise les listes et la confiance à 0.
     */
    public ExtractedInfo() {
        this.items = new ArrayList<>();
        this.additionalNotes = new ArrayList<>();
        this.confidence = 0.0;
    }

    /**
     * Constructeur avec texte source.
     *
     * @param sourceText Texte utilisé pour l'extraction
     */
    public ExtractedInfo(String sourceText) {
        this();
        this.sourceText = sourceText;
    }

    // === GETTERS ET SETTERS ===

    public List<ItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemRequest> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    /**
     * Ajoute un article à la liste.
     *
     * @param item Article à ajouter
     */
    public void addItem(ItemRequest item) {
        if (item != null && item.isValid()) {
            this.items.add(item);
        }
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryDateRaw() {
        return deliveryDateRaw;
    }

    public void setDeliveryDateRaw(String deliveryDateRaw) {
        this.deliveryDateRaw = deliveryDateRaw;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getBudgetUnit() {
        return budgetUnit;
    }

    public void setBudgetUnit(String budgetUnit) {
        this.budgetUnit = budgetUnit;
    }

    public String getBudgetRaw() {
        return budgetRaw;
    }

    public void setBudgetRaw(String budgetRaw) {
        this.budgetRaw = budgetRaw;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public List<String> getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(List<String> additionalNotes) {
        this.additionalNotes = additionalNotes != null ? additionalNotes : new ArrayList<>();
    }

    /**
     * Ajoute une note additionnelle.
     *
     * @param note Note à ajouter
     */
    public void addNote(String note) {
        if (note != null && !note.isBlank()) {
            this.additionalNotes.add(note.trim());
        }
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si au moins un article a été extrait.
     *
     * @return true si au moins un article est présent
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Retourne le nombre d'articles extraits.
     *
     * @return Nombre d'articles
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Calcule la quantité totale de tous les articles.
     *
     * @return Somme des quantités (ignore les null)
     */
    public int getTotalQuantity() {
        return items.stream()
            .filter(i -> i.getQuantity() != null)
            .mapToInt(ItemRequest::getQuantity)
            .sum();
    }

    /**
     * Vérifie si une date de livraison a été trouvée.
     *
     * @return true si une date est définie
     */
    public boolean hasDeliveryDate() {
        return deliveryDate != null || (deliveryDateRaw != null && !deliveryDateRaw.isBlank());
    }

    /**
     * Vérifie si un budget a été trouvé.
     *
     * @return true si un budget est défini
     */
    public boolean hasBudget() {
        return budget != null || (budgetRaw != null && !budgetRaw.isBlank());
    }

    /**
     * Vérifie si l'extraction semble fiable.
     * Une extraction est considérée fiable si :
     * - Au moins un article a été extrait
     * - La confiance est supérieure à 0.5
     *
     * @return true si l'extraction est fiable
     */
    public boolean isReliable() {
        return hasItems() && confidence >= 0.5;
    }

    /**
     * Retourne le budget formaté avec son unité.
     *
     * @return Budget formaté ou null
     */
    public String getFormattedBudget() {
        if (budget == null) {
            return budgetRaw;
        }

        String unit = budgetUnit != null ? " " + budgetUnit : "";

        // Formater sans décimales si c'est un nombre entier
        if (budget == Math.floor(budget)) {
            return String.format("%,.0f%s", budget, unit);
        }
        return String.format("%,.2f%s", budget, unit);
    }

    /**
     * Génère un résumé complet de l'extraction.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RÉSUMÉ DE L'EXTRACTION ===\n");
        sb.append(String.format("Confiance: %.0f%%\n", confidence * 100));
        sb.append("\n");

        // Articles
        sb.append("ARTICLES (").append(getItemCount()).append("):\n");
        if (hasItems()) {
            for (int i = 0; i < items.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(items.get(i).getSummary()).append("\n");
            }
            sb.append("  → Quantité totale: ").append(getTotalQuantity()).append(" unités\n");
        } else {
            sb.append("  (Aucun article extrait)\n");
        }

        // Livraison
        sb.append("\nLIVRAISON:\n");
        if (deliveryDate != null) {
            sb.append("  Date: ").append(deliveryDate).append("\n");
        } else if (deliveryDateRaw != null) {
            sb.append("  Date (brute): ").append(deliveryDateRaw).append("\n");
        } else {
            sb.append("  (Non spécifiée)\n");
        }

        // Budget
        sb.append("\nBUDGET:\n");
        if (budget != null) {
            sb.append("  Montant: ").append(getFormattedBudget()).append("\n");
        } else if (budgetRaw != null) {
            sb.append("  Brut: ").append(budgetRaw).append("\n");
        } else {
            sb.append("  (Non spécifié)\n");
        }

        // Urgence
        if (urgency != null) {
            sb.append("\nURGENCE: ").append(urgency).append("\n");
        }

        // Notes
        if (!additionalNotes.isEmpty()) {
            sb.append("\nNOTES ADDITIONNELLES:\n");
            for (String note : additionalNotes) {
                sb.append("  • ").append(note).append("\n");
            }
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtractedInfo that = (ExtractedInfo) o;
        return Objects.equals(items, that.items) &&
               Objects.equals(deliveryDate, that.deliveryDate) &&
               Objects.equals(budget, that.budget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, deliveryDate, budget);
    }

    @Override
    public String toString() {
        return "ExtractedInfo {\n" +
               "  itemCount: " + getItemCount() + "\n" +
               "  deliveryDate: " + (deliveryDate != null ? deliveryDate : deliveryDateRaw) + "\n" +
               "  budget: " + getFormattedBudget() + "\n" +
               "  urgency: " + urgency + "\n" +
               "  confidence: " + String.format("%.0f%%", confidence * 100) + "\n" +
               "}";
    }
}
