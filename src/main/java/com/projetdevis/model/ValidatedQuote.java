package com.projetdevis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Représente un devis validé après révision humaine.
 *
 * Cette classe encapsule un DraftQuote et ajoute :
 * - L'historique complet des modifications
 * - Les informations de validation (validateur, date, signature)
 * - Le statut final de validation
 * - Les statistiques de validation
 *
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * @author BMAD Pipeline - Étape 6
 * @version 1.0
 */
public class ValidatedQuote {

    // === ÉNUMÉRATIONS ===

    /**
     * Statut de validation du devis.
     */
    public enum ValidationStatus {
        EN_COURS("En cours de validation", "Le devis est en cours de révision"),
        MODIFICATIONS_REQUISES("Modifications requises", "Des corrections sont nécessaires"),
        EN_ATTENTE("En attente", "Le devis est en attente d'informations"),
        APPROUVE("Approuvé", "Le devis est validé et prêt à être envoyé"),
        REJETE("Rejeté", "Le devis a été rejeté");

        private final String label;
        private final String description;

        ValidationStatus(String label, String description) {
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

    // === ATTRIBUTS ===

    /** Brouillon de devis source */
    private DraftQuote draft;

    /** Statut de validation */
    private ValidationStatus validationStatus;

    /** Historique des modifications */
    private List<Modification> modifications;

    /** Nom du validateur (commercial) */
    private String validatorName;

    /** Date de début de validation */
    private LocalDateTime validationStartedAt;

    /** Date de fin de validation (approbation/rejet) */
    private LocalDateTime validatedAt;

    /** Signature électronique ou référence */
    private String validatorSignature;

    /** Commentaire global de validation */
    private String validationComment;

    /** Commentaires de révision */
    private List<String> reviewComments;

    /** Actions en attente de résolution */
    private List<String> pendingActions;

    /** Statistiques de validation */
    private ValidationStats stats;

    // === CLASSE INTERNE : STATISTIQUES ===

    /**
     * Statistiques de la validation.
     */
    public static class ValidationStats {
        public int totalModifications;
        public int itemModifications;
        public int priceModifications;
        public int quantityModifications;
        public int itemsAdded;
        public int itemsRemoved;
        public int discountsApplied;
        public int conditionsModified;
        public long validationDurationMs;
        public double originalTotalHT;
        public double finalTotalHT;
        public double totalDelta;
        public double deltaPercent;

        @Override
        public String toString() {
            return String.format(
                "ValidationStats { modifications=%d (articles=%d, prix=%d, qté=%d), " +
                "ajouts=%d, suppressions=%d, remises=%d, delta=%.2f€ (%.1f%%) }",
                totalModifications, itemModifications, priceModifications, quantityModifications,
                itemsAdded, itemsRemoved, discountsApplied, totalDelta, deltaPercent
            );
        }
    }

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public ValidatedQuote() {
        this.validationStatus = ValidationStatus.EN_COURS;
        this.modifications = new ArrayList<>();
        this.reviewComments = new ArrayList<>();
        this.pendingActions = new ArrayList<>();
        this.validationStartedAt = LocalDateTime.now();
        this.stats = new ValidationStats();
    }

    /**
     * Constructeur à partir d'un brouillon de devis.
     *
     * @param draft Brouillon source
     */
    public ValidatedQuote(DraftQuote draft) {
        this();
        this.draft = draft;

        // Initialisation des actions en attente depuis le brouillon
        if (draft.hasRequiredActions()) {
            this.pendingActions.addAll(draft.getRequiredActions());
        }

        // Sauvegarde du total original pour les statistiques
        if (draft.getTotalHT() != null) {
            this.stats.originalTotalHT = draft.getTotalHT();
        }
    }

    /**
     * Constructeur avec nom du validateur.
     *
     * @param draft Brouillon source
     * @param validatorName Nom du validateur
     */
    public ValidatedQuote(DraftQuote draft, String validatorName) {
        this(draft);
        this.validatorName = validatorName;
    }

    // === GETTERS ET SETTERS ===

    public DraftQuote getDraft() {
        return draft;
    }

    public void setDraft(DraftQuote draft) {
        this.draft = draft;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public List<Modification> getModifications() {
        return modifications;
    }

    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications != null ? modifications : new ArrayList<>();
    }

    public String getValidatorName() {
        return validatorName;
    }

    public void setValidatorName(String validatorName) {
        this.validatorName = validatorName;
    }

    public LocalDateTime getValidationStartedAt() {
        return validationStartedAt;
    }

    public void setValidationStartedAt(LocalDateTime validationStartedAt) {
        this.validationStartedAt = validationStartedAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidatorSignature() {
        return validatorSignature;
    }

    public void setValidatorSignature(String validatorSignature) {
        this.validatorSignature = validatorSignature;
    }

    public String getValidationComment() {
        return validationComment;
    }

    public void setValidationComment(String validationComment) {
        this.validationComment = validationComment;
    }

    public List<String> getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(List<String> reviewComments) {
        this.reviewComments = reviewComments != null ? reviewComments : new ArrayList<>();
    }

    public List<String> getPendingActions() {
        return pendingActions;
    }

    public void setPendingActions(List<String> pendingActions) {
        this.pendingActions = pendingActions != null ? pendingActions : new ArrayList<>();
    }

    public ValidationStats getStats() {
        return stats;
    }

    public void setStats(ValidationStats stats) {
        this.stats = stats;
    }

    // === MÉTHODES D'AJOUT ===

    /**
     * Ajoute une modification à l'historique.
     *
     * @param modification Modification à ajouter
     */
    public void addModification(Modification modification) {
        if (modification != null) {
            if (validatorName != null && modification.getOperator() == null) {
                modification.setOperator(validatorName);
            }
            this.modifications.add(modification);
            updateStats(modification);
        }
    }

    /**
     * Ajoute un commentaire de révision.
     *
     * @param comment Commentaire à ajouter
     */
    public void addReviewComment(String comment) {
        if (comment != null && !comment.isBlank()) {
            String timestamped = String.format("[%s] %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                comment);
            this.reviewComments.add(timestamped);
        }
    }

    /**
     * Ajoute une action en attente.
     *
     * @param action Action à ajouter
     */
    public void addPendingAction(String action) {
        if (action != null && !action.isBlank() && !pendingActions.contains(action)) {
            this.pendingActions.add(action);
        }
    }

    /**
     * Résout (supprime) une action en attente.
     *
     * @param action Action à résoudre
     * @return true si l'action a été trouvée et supprimée
     */
    public boolean resolvePendingAction(String action) {
        return this.pendingActions.remove(action);
    }

    /**
     * Résout une action en attente par index.
     *
     * @param index Index de l'action
     * @return true si l'action a été supprimée
     */
    public boolean resolvePendingAction(int index) {
        if (index >= 0 && index < pendingActions.size()) {
            pendingActions.remove(index);
            return true;
        }
        return false;
    }

    // === MÉTHODES DE MISE À JOUR DES STATISTIQUES ===

    /**
     * Met à jour les statistiques après une modification.
     *
     * @param modification Modification appliquée
     */
    private void updateStats(Modification modification) {
        if (modification == null || !modification.isApplied()) return;

        stats.totalModifications++;

        switch (modification.getAction()) {
            case MODIFY_QUANTITY:
                stats.quantityModifications++;
                stats.itemModifications++;
                break;
            case MODIFY_PRICE:
                stats.priceModifications++;
                stats.itemModifications++;
                break;
            case MODIFY_DESIGNATION:
            case MODIFY_DESCRIPTION:
            case MODIFY_CATEGORY:
            case VALIDATE_ITEM:
                stats.itemModifications++;
                break;
            case ADD_ITEM:
                stats.itemsAdded++;
                break;
            case REMOVE_ITEM:
                stats.itemsRemoved++;
                break;
            case APPLY_ITEM_DISCOUNT:
            case APPLY_GLOBAL_DISCOUNT:
                stats.discountsApplied++;
                break;
            case MODIFY_DELIVERY:
            case MODIFY_DELIVERY_DATE:
            case MODIFY_DELIVERY_FEES:
            case MODIFY_PAYMENT_TERMS:
            case MODIFY_WARRANTY:
            case ADD_CONDITION:
            case REMOVE_CONDITION:
                stats.conditionsModified++;
                break;
            default:
                break;
        }
    }

    /**
     * Finalise les statistiques après validation.
     */
    public void finalizeStats() {
        if (draft != null && draft.getTotalHT() != null) {
            stats.finalTotalHT = draft.getTotalHT();
            stats.totalDelta = stats.finalTotalHT - stats.originalTotalHT;

            if (stats.originalTotalHT > 0) {
                stats.deltaPercent = (stats.totalDelta / stats.originalTotalHT) * 100;
            }
        }

        if (validationStartedAt != null && validatedAt != null) {
            stats.validationDurationMs = java.time.Duration.between(
                validationStartedAt, validatedAt).toMillis();
        }
    }

    // === MÉTHODES DE VÉRIFICATION ===

    /**
     * Vérifie si le devis a des modifications.
     *
     * @return true si des modifications ont été effectuées
     */
    public boolean hasModifications() {
        return modifications != null && !modifications.isEmpty();
    }

    /**
     * Vérifie si le devis a des actions en attente.
     *
     * @return true si des actions sont en attente
     */
    public boolean hasPendingActions() {
        return pendingActions != null && !pendingActions.isEmpty();
    }

    /**
     * Vérifie si le devis est prêt pour approbation.
     *
     * @return true si le devis peut être approuvé
     */
    public boolean isReadyForApproval() {
        if (draft == null) return false;

        // Pas d'actions en attente
        if (hasPendingActions()) return false;

        // Pas d'incohérences non résolues
        if (draft.hasInconsistencies()) return false;

        // Tous les articles doivent avoir un prix
        if (!draft.getItemsWithoutPrice().isEmpty()) return false;

        // Le statut ne doit pas être déjà rejeté
        if (draft.getStatus() == DraftQuote.DraftStatus.REJETE) return false;

        return true;
    }

    /**
     * Vérifie si le devis est validé (approuvé ou rejeté).
     *
     * @return true si la validation est terminée
     */
    public boolean isValidated() {
        return validationStatus == ValidationStatus.APPROUVE ||
               validationStatus == ValidationStatus.REJETE;
    }

    /**
     * Vérifie si le devis est approuvé.
     *
     * @return true si le devis est approuvé
     */
    public boolean isApproved() {
        return validationStatus == ValidationStatus.APPROUVE;
    }

    /**
     * Vérifie si le devis est rejeté.
     *
     * @return true si le devis est rejeté
     */
    public boolean isRejected() {
        return validationStatus == ValidationStatus.REJETE;
    }

    /**
     * Retourne le nombre de modifications.
     *
     * @return Nombre de modifications
     */
    public int getModificationCount() {
        return modifications != null ? modifications.size() : 0;
    }

    /**
     * Retourne les modifications appliquées avec succès.
     *
     * @return Liste des modifications réussies
     */
    public List<Modification> getAppliedModifications() {
        return modifications.stream()
            .filter(Modification::isApplied)
            .collect(Collectors.toList());
    }

    /**
     * Retourne les modifications échouées.
     *
     * @return Liste des modifications échouées
     */
    public List<Modification> getFailedModifications() {
        return modifications.stream()
            .filter(m -> !m.isApplied())
            .collect(Collectors.toList());
    }

    /**
     * Retourne les modifications d'articles.
     *
     * @return Liste des modifications d'articles
     */
    public List<Modification> getItemModifications() {
        return modifications.stream()
            .filter(Modification::isItemModification)
            .collect(Collectors.toList());
    }

    // === MÉTHODES DE FORMATAGE ===

    /**
     * Retourne la date de début de validation formatée.
     *
     * @return Date formatée
     */
    public String getFormattedValidationStartedAt() {
        if (validationStartedAt == null) return "N/A";
        return validationStartedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Retourne la date de validation formatée.
     *
     * @return Date formatée
     */
    public String getFormattedValidatedAt() {
        if (validatedAt == null) return "N/A";
        return validatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Retourne la durée de validation formatée.
     *
     * @return Durée formatée
     */
    public String getFormattedValidationDuration() {
        if (stats.validationDurationMs == 0) return "N/A";

        long seconds = stats.validationDurationMs / 1000;
        if (seconds < 60) {
            return seconds + " secondes";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else {
            return (seconds / 3600) + " heures " + ((seconds % 3600) / 60) + " minutes";
        }
    }

    /**
     * Génère un résumé de la validation.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== RÉSUMÉ DE VALIDATION ===\n");

        if (draft != null) {
            sb.append("Devis: ").append(draft.getQuoteNumber()).append("\n");
        }

        sb.append("Statut: ").append(validationStatus.getLabel()).append("\n");

        if (validatorName != null) {
            sb.append("Validateur: ").append(validatorName).append("\n");
        }

        sb.append("Débuté le: ").append(getFormattedValidationStartedAt()).append("\n");

        if (validatedAt != null) {
            sb.append("Finalisé le: ").append(getFormattedValidatedAt()).append("\n");
            sb.append("Durée: ").append(getFormattedValidationDuration()).append("\n");
        }

        sb.append("\n");

        // Modifications
        sb.append("MODIFICATIONS: ").append(getModificationCount()).append(" total\n");
        if (hasModifications()) {
            sb.append(String.format("  - Prix: %d, Quantités: %d, Ajouts: %d, Suppressions: %d\n",
                stats.priceModifications, stats.quantityModifications,
                stats.itemsAdded, stats.itemsRemoved));
        }

        // Delta
        if (stats.totalDelta != 0) {
            String sign = stats.totalDelta >= 0 ? "+" : "";
            sb.append(String.format("  - Écart total: %s%.2f € (%s%.1f%%)\n",
                sign, stats.totalDelta, sign, stats.deltaPercent));
        }

        // Actions en attente
        if (hasPendingActions()) {
            sb.append("\nACTIONS EN ATTENTE: ").append(pendingActions.size()).append("\n");
            for (String action : pendingActions) {
                sb.append("  □ ").append(action).append("\n");
            }
        }

        // Commentaire de validation
        if (validationComment != null && !validationComment.isBlank()) {
            sb.append("\nCOMMENTAIRE: ").append(validationComment).append("\n");
        }

        return sb.toString();
    }

    /**
     * Génère l'historique complet des modifications.
     *
     * @return Historique textuel
     */
    public String getModificationHistory() {
        if (!hasModifications()) {
            return "Aucune modification effectuée.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== HISTORIQUE DES MODIFICATIONS ===\n\n");

        int num = 1;
        for (Modification mod : modifications) {
            sb.append(String.format("%d. %s\n", num++, mod.getSummary()));
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidatedQuote that = (ValidatedQuote) o;
        return Objects.equals(draft, that.draft) &&
               Objects.equals(validationStartedAt, that.validationStartedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(draft, validationStartedAt);
    }

    @Override
    public String toString() {
        return "ValidatedQuote {\n" +
               "  quoteNumber: \"" + (draft != null ? draft.getQuoteNumber() : "null") + "\"\n" +
               "  validationStatus: " + validationStatus.getLabel() + "\n" +
               "  validatorName: \"" + validatorName + "\"\n" +
               "  modifications: " + getModificationCount() + "\n" +
               "  pendingActions: " + pendingActions.size() + "\n" +
               "  validatedAt: " + getFormattedValidatedAt() + "\n" +
               "  isApproved: " + isApproved() + "\n" +
               "}";
    }
}
