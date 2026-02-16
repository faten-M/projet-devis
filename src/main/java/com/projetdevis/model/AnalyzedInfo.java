package com.projetdevis.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Conteneur principal des informations analysées, validées et enrichies.
 *
 * Cette classe représente le résultat final de l'étape 4 du pipeline BMAD.
 * Elle contient :
 * - Les articles validés et catégorisés
 * - Le budget normalisé
 * - La date de livraison validée
 * - Les incohérences détectées
 * - Les statistiques d'analyse
 *
 * Pipeline BMAD - Étape 4 : Analyse
 *
 * @author BMAD Pipeline - Étape 4
 * @version 1.0
 */
public class AnalyzedInfo {

    // === ATTRIBUTS ===

    /** Liste des articles analysés et validés */
    private List<AnalyzedItem> items;

    /** Budget normalisé (montant) */
    private Double budget;

    /** Unité du budget normalisée */
    private String budgetUnit;

    /** Budget brut original */
    private String budgetRaw;

    /** Indique si le budget est HT ou TTC */
    private boolean budgetHT;

    /** Date de livraison validée */
    private LocalDate deliveryDate;

    /** Date de livraison brute */
    private String deliveryDateRaw;

    /** Niveau d'urgence normalisé */
    private String urgency;

    /** Notes additionnelles validées */
    private List<String> notes;

    /** Liste des incohérences détectées */
    private List<String> inconsistencies;

    /** Liste des incohérences MAJEURES (bloquantes) */
    private List<String> majorInconsistencies;

    /** Liste des avertissements globaux */
    private List<String> warnings;

    /** Confiance globale de l'analyse (0.0 à 1.0) */
    private double confidence;

    /** Référence à l'objet ExtractedInfo source */
    private ExtractedInfo sourceInfo;

    /** Statistiques de l'analyse */
    private AnalysisStats stats;

    // === CLASSE INTERNE : STATISTIQUES ===

    /**
     * Statistiques de l'analyse.
     */
    public static class AnalysisStats {
        public int totalItemsExtracted;
        public int itemsFiltered;
        public int itemsValidated;
        public int itemsMerged;
        public int inconsistenciesFound;
        public long analysisTimeMs;

        @Override
        public String toString() {
            return String.format(
                "Stats { extraits=%d, filtrés=%d, validés=%d, fusionnés=%d, incohérences=%d, temps=%dms }",
                totalItemsExtracted, itemsFiltered, itemsValidated, itemsMerged, inconsistenciesFound, analysisTimeMs
            );
        }
    }

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public AnalyzedInfo() {
        this.items = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.inconsistencies = new ArrayList<>();
        this.majorInconsistencies = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.confidence = 0.0;
        this.budgetHT = true;
        this.stats = new AnalysisStats();
    }

    /**
     * Constructeur à partir d'un ExtractedInfo.
     *
     * @param source Informations extraites à analyser
     */
    public AnalyzedInfo(ExtractedInfo source) {
        this();
        this.sourceInfo = source;
    }

    // === GETTERS ET SETTERS ===

    public List<AnalyzedItem> getItems() {
        return items;
    }

    public void setItems(List<AnalyzedItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(AnalyzedItem item) {
        if (item != null) {
            this.items.add(item);
        }
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

    public boolean isBudgetHT() {
        return budgetHT;
    }

    public void setBudgetHT(boolean budgetHT) {
        this.budgetHT = budgetHT;
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

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
    }

    public void addNote(String note) {
        if (note != null && !note.isBlank()) {
            this.notes.add(note.trim());
        }
    }

    public List<String> getInconsistencies() {
        return inconsistencies;
    }

    public void setInconsistencies(List<String> inconsistencies) {
        this.inconsistencies = inconsistencies != null ? inconsistencies : new ArrayList<>();
    }

    public void addInconsistency(String inconsistency) {
        if (inconsistency != null && !inconsistency.isBlank()) {
            this.inconsistencies.add(inconsistency);
        }
    }

    /**
     * Retourne la liste des incohérences majeures (bloquantes).
     *
     * @return Liste des incohérences majeures
     */
    public List<String> getMajorInconsistencies() {
        return majorInconsistencies;
    }

    /**
     * Ajoute une incohérence MAJEURE (bloquante).
     * Les incohérences majeures empêchent le pipeline de continuer.
     *
     * @param inconsistency Description de l'incohérence majeure
     */
    public void addMajorInconsistency(String inconsistency) {
        if (inconsistency != null && !inconsistency.isBlank()) {
            this.majorInconsistencies.add("[MAJEURE] " + inconsistency);
            // Ajouter aussi aux incohérences normales pour le rapport
            this.inconsistencies.add("[MAJEURE] " + inconsistency);
        }
    }

    /**
     * Vérifie s'il y a des incohérences majeures.
     *
     * @return true s'il y a au moins une incohérence majeure
     */
    public boolean hasMajorInconsistency() {
        return !majorInconsistencies.isEmpty();
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

    public ExtractedInfo getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(ExtractedInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public AnalysisStats getStats() {
        return stats;
    }

    public void setStats(AnalysisStats stats) {
        this.stats = stats;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si des articles ont été validés.
     *
     * @return true si au moins un article est présent
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Retourne le nombre d'articles validés.
     *
     * @return Nombre d'articles
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Retourne uniquement les articles valides.
     *
     * @return Liste des articles valides
     */
    public List<AnalyzedItem> getValidItems() {
        return items.stream()
            .filter(AnalyzedItem::isValid)
            .collect(Collectors.toList());
    }

    /**
     * Calcule la quantité totale des articles validés.
     *
     * @return Somme des quantités
     */
    public int getTotalQuantity() {
        return items.stream()
            .filter(i -> i.getQuantity() != null)
            .mapToInt(AnalyzedItem::getQuantity)
            .sum();
    }

    /**
     * Vérifie si un budget a été détecté.
     *
     * @return true si un budget est défini
     */
    public boolean hasBudget() {
        return budget != null;
    }

    /**
     * Vérifie si une date de livraison a été détectée.
     *
     * @return true si une date est définie
     */
    public boolean hasDeliveryDate() {
        return deliveryDate != null;
    }

    /**
     * Vérifie si des incohérences ont été détectées.
     *
     * @return true si au moins une incohérence est présente
     */
    public boolean hasInconsistencies() {
        return inconsistencies != null && !inconsistencies.isEmpty();
    }

    /**
     * Vérifie si l'analyse est considérée comme fiable.
     * Une analyse est fiable si :
     * - Au moins un article valide
     * - Confiance >= 0.5
     * - Pas d'incohérences majeures
     * - Pas plus de 2 incohérences mineures
     *
     * @return true si l'analyse est fiable
     */
    public boolean isReliable() {
        return hasItems() &&
               getValidItems().size() > 0 &&
               confidence >= 0.5 &&
               !hasMajorInconsistency() &&
               inconsistencies.size() <= 2;
    }

    /**
     * Retourne le budget formaté.
     *
     * @return Budget formaté avec unité
     */
    public String getFormattedBudget() {
        if (budget == null) {
            return budgetRaw;
        }

        String unit = budgetUnit != null ? " " + budgetUnit : " €";
        String htTtc = budgetHT ? " HT" : " TTC";

        if (budget == Math.floor(budget)) {
            return String.format("%,.0f%s%s", budget, unit, htTtc);
        }
        return String.format("%,.2f%s%s", budget, unit, htTtc);
    }

    /**
     * Regroupe les articles par catégorie.
     *
     * @return Map catégorie → liste d'articles
     */
    public Map<AnalyzedItem.Category, List<AnalyzedItem>> getItemsByCategory() {
        Map<AnalyzedItem.Category, List<AnalyzedItem>> result = new HashMap<>();

        for (AnalyzedItem item : items) {
            result.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        }

        return result;
    }

    /**
     * Génère un résumé complet de l'analyse.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RÉSUMÉ DE L'ANALYSE (Étape 4) ===\n");
        sb.append(String.format("Confiance: %.0f%%\n", confidence * 100));
        sb.append(String.format("Fiabilité: %s\n", isReliable() ? "OUI" : "NON"));
        sb.append("\n");

        // Statistiques
        if (stats != null) {
            sb.append("STATISTIQUES:\n");
            sb.append(String.format("  Articles extraits: %d\n", stats.totalItemsExtracted));
            sb.append(String.format("  Articles filtrés: %d\n", stats.itemsFiltered));
            sb.append(String.format("  Articles validés: %d\n", stats.itemsValidated));
            sb.append(String.format("  Articles fusionnés: %d\n", stats.itemsMerged));
            sb.append("\n");
        }

        // Articles par catégorie
        sb.append("ARTICLES PAR CATÉGORIE:\n");
        Map<AnalyzedItem.Category, List<AnalyzedItem>> byCategory = getItemsByCategory();
        for (AnalyzedItem.Category cat : AnalyzedItem.Category.values()) {
            List<AnalyzedItem> catItems = byCategory.get(cat);
            if (catItems != null && !catItems.isEmpty()) {
                sb.append("  ").append(cat.getLabel()).append(":\n");
                for (AnalyzedItem item : catItems) {
                    String statusIcon = item.isValid() ? "✓" : "⚠";
                    sb.append("    ").append(statusIcon).append(" ").append(item.getSummary()).append("\n");
                }
            }
        }

        // Totaux
        sb.append(String.format("\n  → Total: %d articles, %d unités\n", getItemCount(), getTotalQuantity()));

        // Budget
        sb.append("\nBUDGET:\n");
        if (hasBudget()) {
            sb.append("  ").append(getFormattedBudget()).append("\n");
        } else {
            sb.append("  (Non spécifié)\n");
        }

        // Livraison
        sb.append("\nLIVRAISON:\n");
        if (hasDeliveryDate()) {
            sb.append("  Date: ").append(deliveryDate).append("\n");
        } else if (deliveryDateRaw != null) {
            sb.append("  Brut: ").append(deliveryDateRaw).append("\n");
        } else {
            sb.append("  (Non spécifiée)\n");
        }

        // Urgence
        if (urgency != null) {
            sb.append("\nURGENCE: ").append(urgency.toUpperCase()).append("\n");
        }

        // Notes
        if (!notes.isEmpty()) {
            sb.append("\nNOTES:\n");
            for (String note : notes) {
                sb.append("  • ").append(note).append("\n");
            }
        }

        // Incohérences
        if (hasInconsistencies()) {
            sb.append("\n⚠ INCOHÉRENCES DÉTECTÉES:\n");
            for (String inc : inconsistencies) {
                sb.append("  ⚠ ").append(inc).append("\n");
            }
        }

        // Avertissements
        if (!warnings.isEmpty()) {
            sb.append("\nAVERTISSEMENTS:\n");
            for (String warn : warnings) {
                sb.append("  ! ").append(warn).append("\n");
            }
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedInfo that = (AnalyzedInfo) o;
        return Objects.equals(items, that.items) &&
               Objects.equals(budget, that.budget) &&
               Objects.equals(deliveryDate, that.deliveryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, budget, deliveryDate);
    }

    @Override
    public String toString() {
        return "AnalyzedInfo {\n" +
               "  itemCount: " + getItemCount() + "\n" +
               "  validItems: " + getValidItems().size() + "\n" +
               "  totalQuantity: " + getTotalQuantity() + "\n" +
               "  budget: " + getFormattedBudget() + "\n" +
               "  deliveryDate: " + deliveryDate + "\n" +
               "  urgency: " + urgency + "\n" +
               "  inconsistencies: " + inconsistencies.size() + "\n" +
               "  confidence: " + String.format("%.0f%%", confidence * 100) + "\n" +
               "  reliable: " + isReliable() + "\n" +
               "}";
    }
}
