package com.projetdevis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Représente un brouillon de devis généré à partir de l'analyse.
 *
 * Cette classe transforme AnalyzedInfo en document commercial structuré avec :
 * - En-tête du devis (numéro, dates, validité)
 * - Lignes de devis organisées par catégorie
 * - Totaux et récapitulatif financier
 * - Conditions commerciales
 * - Recommandations et actions requises
 *
 * Pipeline BMAD - Étape 5 : Brouillon du devis
 *
 * @author BMAD Pipeline - Étape 5
 * @version 1.0
 */
public class DraftQuote {

    // === ÉNUMÉRATIONS ===

    /**
     * Statut global du brouillon de devis.
     */
    public enum DraftStatus {
        BROUILLON("Brouillon", "En cours de rédaction, non finalisé"),
        A_COMPLETER("À compléter", "Informations manquantes requises"),
        A_VALIDER("À valider", "Prêt pour validation interne"),
        PRET("Prêt", "Prêt à être envoyé au client"),
        REJETE("Rejeté", "Demande rejetée ou incohérente");

        private final String label;
        private final String description;

        DraftStatus(String label, String description) {
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
     * Priorité du devis.
     */
    public enum Priority {
        BASSE("Basse", 1),
        NORMALE("Normale", 2),
        HAUTE("Haute", 3),
        URGENTE("Urgente", 4),
        CRITIQUE("Critique", 5);

        private final String label;
        private final int level;

        Priority(String label, int level) {
            this.label = label;
            this.level = level;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }
    }

    // === ATTRIBUTS EN-TÊTE ===

    /** Numéro unique du devis */
    private String quoteNumber;

    /** Date de création du brouillon */
    private LocalDateTime createdAt;

    /** Date de dernière modification */
    private LocalDateTime modifiedAt;

    /** Date d'expiration/validité du devis */
    private LocalDate validUntil;

    /** Durée de validité en jours */
    private int validityDays;

    /** Objet/titre du devis */
    private String subject;

    /** Référence client (si connue) */
    private String clientReference;

    /** Statut du brouillon */
    private DraftStatus status;

    /** Priorité */
    private Priority priority;

    // === ATTRIBUTS CONTENU ===

    /** Lignes du devis */
    private List<QuoteItem> items;

    /** Sections regroupant les lignes par catégorie */
    private Map<AnalyzedItem.Category, List<QuoteItem>> sections;

    // === ATTRIBUTS FINANCIERS ===

    /** Total HT avant remise */
    private Double totalHTBrut;

    /** Total des remises */
    private Double totalDiscount;

    /** Total HT après remise */
    private Double totalHT;

    /** Montant TVA */
    private Double totalTVA;

    /** Taux TVA global */
    private double tvaRate;

    /** Total TTC */
    private Double totalTTC;

    /** Budget client (si indiqué) */
    private Double clientBudget;

    /** Écart budget vs total */
    private Double budgetDelta;

    /** Indicateur budget respecté */
    private Boolean budgetRespected;

    // === ATTRIBUTS LIVRAISON ===

    /** Date de livraison souhaitée */
    private LocalDate requestedDeliveryDate;

    /** Date de livraison estimée */
    private LocalDate estimatedDeliveryDate;

    /** Délai de livraison estimé (jours ouvrés) */
    private Integer deliveryDays;

    /** Frais de livraison */
    private Double deliveryFees;

    /** Livraison incluse */
    private boolean deliveryIncluded;

    /** Installation incluse */
    private boolean installationIncluded;

    // === ATTRIBUTS CONDITIONS ===

    /** Conditions de paiement */
    private String paymentTerms;

    /** Garantie */
    private String warranty;

    /** Conditions particulières */
    private List<String> specialConditions;

    // === ATTRIBUTS QUALITÉ ===

    /** Actions requises avant finalisation */
    private List<String> requiredActions;

    /** Recommandations pour le commercial */
    private List<String> recommendations;

    /** Avertissements */
    private List<String> warnings;

    /** Incohérences héritées de l'analyse */
    private List<String> inconsistencies;

    /** Confiance globale dans le brouillon */
    private double confidence;

    /** Source AnalyzedInfo */
    private AnalyzedInfo sourceAnalysis;

    /** Statistiques de génération */
    private DraftStats stats;

    // === CLASSE INTERNE : STATISTIQUES ===

    /**
     * Statistiques de génération du brouillon.
     */
    public static class DraftStats {
        public int totalItems;
        public int completeItems;
        public int incompleteItems;
        public int itemsWithPrice;
        public int itemsWithoutPrice;
        public int sectionsCount;
        public int warningsCount;
        public int actionsRequired;
        public long generationTimeMs;

        @Override
        public String toString() {
            return String.format(
                "DraftStats { articles=%d (complets=%d, incomplets=%d), prix=%d/%d, sections=%d, avertissements=%d, actions=%d, temps=%dms }",
                totalItems, completeItems, incompleteItems, itemsWithPrice, itemsWithoutPrice,
                sectionsCount, warningsCount, actionsRequired, generationTimeMs
            );
        }
    }

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public DraftQuote() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.validityDays = 30;
        this.validUntil = LocalDate.now().plusDays(validityDays);
        this.status = DraftStatus.BROUILLON;
        this.priority = Priority.NORMALE;
        this.tvaRate = 20.0;

        this.items = new ArrayList<>();
        this.sections = new HashMap<>();
        this.specialConditions = new ArrayList<>();
        this.requiredActions = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.inconsistencies = new ArrayList<>();

        this.deliveryIncluded = false;
        this.installationIncluded = false;
        this.confidence = 0.0;

        this.stats = new DraftStats();
    }

    /**
     * Constructeur à partir d'une analyse.
     *
     * @param source Analyse source
     */
    public DraftQuote(AnalyzedInfo source) {
        this();
        this.sourceAnalysis = source;
        this.quoteNumber = generateQuoteNumber();
    }

    /**
     * Génère un numéro de devis unique.
     */
    private String generateQuoteNumber() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("DEV-%d%02d%02d-%04d",
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            (int) (Math.random() * 10000));
    }

    // === GETTERS ET SETTERS ===

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
        markModified();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
        markModified();
    }

    public int getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(int validityDays) {
        this.validityDays = validityDays;
        this.validUntil = LocalDate.now().plusDays(validityDays);
        markModified();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        markModified();
    }

    public String getClientReference() {
        return clientReference;
    }

    public void setClientReference(String clientReference) {
        this.clientReference = clientReference;
        markModified();
    }

    public DraftStatus getStatus() {
        return status;
    }

    public void setStatus(DraftStatus status) {
        this.status = status;
        markModified();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        markModified();
    }

    public List<QuoteItem> getItems() {
        return items;
    }

    public void setItems(List<QuoteItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        rebuildSections();
        markModified();
    }

    public void addItem(QuoteItem item) {
        if (item != null) {
            this.items.add(item);
            addToSection(item);
            markModified();
        }
    }

    public Map<AnalyzedItem.Category, List<QuoteItem>> getSections() {
        return sections;
    }

    private void addToSection(QuoteItem item) {
        AnalyzedItem.Category category = item.getCategory();
        if (category == null) {
            category = AnalyzedItem.Category.AUTRE;
        }
        sections.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
    }

    private void rebuildSections() {
        sections.clear();
        for (QuoteItem item : items) {
            addToSection(item);
        }
    }

    public Double getTotalHTBrut() {
        return totalHTBrut;
    }

    public void setTotalHTBrut(Double totalHTBrut) {
        this.totalHTBrut = totalHTBrut;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getTotalHT() {
        return totalHT;
    }

    public void setTotalHT(Double totalHT) {
        this.totalHT = totalHT;
    }

    public Double getTotalTVA() {
        return totalTVA;
    }

    public void setTotalTVA(Double totalTVA) {
        this.totalTVA = totalTVA;
    }

    public double getTvaRate() {
        return tvaRate;
    }

    public void setTvaRate(double tvaRate) {
        this.tvaRate = tvaRate;
    }

    public Double getTotalTTC() {
        return totalTTC;
    }

    public void setTotalTTC(Double totalTTC) {
        this.totalTTC = totalTTC;
    }

    public Double getClientBudget() {
        return clientBudget;
    }

    public void setClientBudget(Double clientBudget) {
        this.clientBudget = clientBudget;
        updateBudgetAnalysis();
    }

    public Double getBudgetDelta() {
        return budgetDelta;
    }

    public Boolean isBudgetRespected() {
        return budgetRespected;
    }

    public LocalDate getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public void setRequestedDeliveryDate(LocalDate requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
        markModified();
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        markModified();
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
        markModified();
    }

    public Double getDeliveryFees() {
        return deliveryFees;
    }

    public void setDeliveryFees(Double deliveryFees) {
        this.deliveryFees = deliveryFees;
        markModified();
    }

    public boolean isDeliveryIncluded() {
        return deliveryIncluded;
    }

    public void setDeliveryIncluded(boolean deliveryIncluded) {
        this.deliveryIncluded = deliveryIncluded;
        markModified();
    }

    public boolean isInstallationIncluded() {
        return installationIncluded;
    }

    public void setInstallationIncluded(boolean installationIncluded) {
        this.installationIncluded = installationIncluded;
        markModified();
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
        markModified();
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
        markModified();
    }

    public List<String> getSpecialConditions() {
        return specialConditions;
    }

    public void setSpecialConditions(List<String> specialConditions) {
        this.specialConditions = specialConditions != null ? specialConditions : new ArrayList<>();
        markModified();
    }

    public void addSpecialCondition(String condition) {
        if (condition != null && !condition.isBlank()) {
            this.specialConditions.add(condition);
            markModified();
        }
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions != null ? requiredActions : new ArrayList<>();
    }

    public void addRequiredAction(String action) {
        if (action != null && !action.isBlank() && !this.requiredActions.contains(action)) {
            this.requiredActions.add(action);
        }
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public void addRecommendation(String recommendation) {
        if (recommendation != null && !recommendation.isBlank()) {
            this.recommendations.add(recommendation);
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

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public AnalyzedInfo getSourceAnalysis() {
        return sourceAnalysis;
    }

    public void setSourceAnalysis(AnalyzedInfo sourceAnalysis) {
        this.sourceAnalysis = sourceAnalysis;
    }

    public DraftStats getStats() {
        return stats;
    }

    public void setStats(DraftStats stats) {
        this.stats = stats;
    }

    // === MÉTHODES DE CALCUL ===

    /**
     * Marque le document comme modifié.
     */
    private void markModified() {
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * Met à jour l'analyse du budget.
     */
    private void updateBudgetAnalysis() {
        if (clientBudget != null && totalHT != null) {
            this.budgetDelta = clientBudget - totalHT;
            this.budgetRespected = budgetDelta >= 0;
        }
    }

    /**
     * Recalcule tous les totaux.
     */
    public void recalculateTotals() {
        double brutHT = 0;
        double discounts = 0;

        for (QuoteItem item : items) {
            if (item.getTotalPriceHT() != null) {
                brutHT += item.getUnitPriceHT() * item.getQuantity();
                if (item.getDiscountAmount() != null) {
                    discounts += item.getDiscountAmount();
                }
            }
        }

        this.totalHTBrut = brutHT;
        this.totalDiscount = discounts;
        this.totalHT = brutHT - discounts;

        // Ajout des frais de livraison si non inclus
        if (!deliveryIncluded && deliveryFees != null) {
            this.totalHT += deliveryFees;
        }

        this.totalTVA = totalHT * (tvaRate / 100);
        this.totalTTC = totalHT + totalTVA;

        updateBudgetAnalysis();
        markModified();
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si le devis a des articles.
     *
     * @return true si au moins un article
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Retourne le nombre d'articles.
     *
     * @return Nombre d'articles
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Retourne la quantité totale.
     *
     * @return Somme des quantités
     */
    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(QuoteItem::getQuantity)
            .sum();
    }

    /**
     * Vérifie si le devis est complet.
     *
     * @return true si toutes les informations sont présentes
     */
    public boolean isComplete() {
        return status == DraftStatus.PRET &&
               requiredActions.isEmpty() &&
               items.stream().allMatch(QuoteItem::isComplete);
    }

    /**
     * Vérifie si le devis a des actions requises.
     *
     * @return true si des actions sont nécessaires
     */
    public boolean hasRequiredActions() {
        return requiredActions != null && !requiredActions.isEmpty();
    }

    /**
     * Vérifie si le devis a des avertissements.
     *
     * @return true si des avertissements sont présents
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Vérifie si le devis a des incohérences.
     *
     * @return true si des incohérences sont présentes
     */
    public boolean hasInconsistencies() {
        return inconsistencies != null && !inconsistencies.isEmpty();
    }

    /**
     * Vérifie si le devis est valide (non expiré).
     *
     * @return true si le devis est encore valide
     */
    public boolean isValid() {
        return validUntil != null && !validUntil.isBefore(LocalDate.now());
    }

    /**
     * Retourne le nombre de jours restants de validité.
     *
     * @return Jours restants (négatif si expiré)
     */
    public long getDaysUntilExpiry() {
        if (validUntil == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), validUntil);
    }

    /**
     * Retourne les articles d'une catégorie.
     *
     * @param category Catégorie
     * @return Liste des articles
     */
    public List<QuoteItem> getItemsByCategory(AnalyzedItem.Category category) {
        return sections.getOrDefault(category, new ArrayList<>());
    }

    /**
     * Retourne les articles incomplets.
     *
     * @return Liste des articles à compléter
     */
    public List<QuoteItem> getIncompleteItems() {
        return items.stream()
            .filter(item -> !item.isComplete())
            .collect(Collectors.toList());
    }

    /**
     * Retourne les articles sans prix.
     *
     * @return Liste des articles sans prix
     */
    public List<QuoteItem> getItemsWithoutPrice() {
        return items.stream()
            .filter(item -> !item.hasPrice())
            .collect(Collectors.toList());
    }

    // === MÉTHODES DE FORMATAGE ===

    /**
     * Retourne le total HT formaté.
     *
     * @return Total formaté
     */
    public String getFormattedTotalHT() {
        if (totalHT == null) return "À définir";
        return String.format("%,.2f € HT", totalHT);
    }

    /**
     * Retourne le total TTC formaté.
     *
     * @return Total formaté
     */
    public String getFormattedTotalTTC() {
        if (totalTTC == null) return "À définir";
        return String.format("%,.2f € TTC", totalTTC);
    }

    /**
     * Retourne la TVA formatée.
     *
     * @return TVA formatée
     */
    public String getFormattedTVA() {
        if (totalTVA == null) return "À définir";
        return String.format("%,.2f € (%.0f%%)", totalTVA, tvaRate);
    }

    /**
     * Retourne l'écart budget formaté.
     *
     * @return Écart formaté
     */
    public String getFormattedBudgetDelta() {
        if (budgetDelta == null) return "N/A";
        String sign = budgetDelta >= 0 ? "+" : "";
        return String.format("%s%,.2f €", sign, budgetDelta);
    }

    /**
     * Retourne la date de création formatée.
     *
     * @return Date formatée
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Retourne la date de validité formatée.
     *
     * @return Date formatée
     */
    public String getFormattedValidUntil() {
        if (validUntil == null) return "N/A";
        return validUntil.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Génère un résumé du brouillon.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== BROUILLON DE DEVIS ===\n");
        sb.append(String.format("N° %s | Créé le %s\n", quoteNumber, getFormattedCreatedAt()));
        sb.append(String.format("Statut: %s | Priorité: %s\n", status.getLabel(), priority.getLabel()));
        sb.append(String.format("Valide jusqu'au: %s (%d jours)\n", getFormattedValidUntil(), getDaysUntilExpiry()));
        sb.append("\n");

        if (subject != null) {
            sb.append("Objet: ").append(subject).append("\n\n");
        }

        // Articles
        sb.append(String.format("ARTICLES: %d lignes, %d unités\n", getItemCount(), getTotalQuantity()));
        for (QuoteItem item : items) {
            sb.append("  • ").append(item.getSummary()).append("\n");
        }

        // Totaux
        sb.append("\nTOTAUX:\n");
        sb.append("  Total HT:  ").append(getFormattedTotalHT()).append("\n");
        sb.append("  TVA:       ").append(getFormattedTVA()).append("\n");
        sb.append("  Total TTC: ").append(getFormattedTotalTTC()).append("\n");

        // Budget
        if (clientBudget != null) {
            sb.append("\nBUDGET CLIENT:\n");
            sb.append(String.format("  Budget: %,.2f € | Écart: %s\n", clientBudget, getFormattedBudgetDelta()));
            sb.append("  Respect du budget: ").append(budgetRespected ? "OUI" : "NON").append("\n");
        }

        // Actions requises
        if (hasRequiredActions()) {
            sb.append("\nACTIONS REQUISES:\n");
            for (String action : requiredActions) {
                sb.append("  □ ").append(action).append("\n");
            }
        }

        // Confiance
        sb.append(String.format("\nConfiance: %.0f%%\n", confidence * 100));

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DraftQuote that = (DraftQuote) o;
        return Objects.equals(quoteNumber, that.quoteNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteNumber);
    }

    @Override
    public String toString() {
        return "DraftQuote {\n" +
               "  quoteNumber: \"" + quoteNumber + "\"\n" +
               "  status: " + status.getLabel() + "\n" +
               "  items: " + getItemCount() + "\n" +
               "  totalHT: " + getFormattedTotalHT() + "\n" +
               "  totalTTC: " + getFormattedTotalTTC() + "\n" +
               "  validUntil: " + getFormattedValidUntil() + "\n" +
               "  confidence: " + String.format("%.0f%%", confidence * 100) + "\n" +
               "}";
    }
}
