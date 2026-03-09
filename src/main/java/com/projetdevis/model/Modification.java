package com.projetdevis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Représente une modification apportée lors de la validation humaine.
 *
 * Cette classe trace toutes les modifications effectuées sur un brouillon de devis,
 * permettant un audit complet des changements avant envoi au client.
 *
 * Chaque modification enregistre :
 * - L'action effectuée
 * - Les valeurs avant/après
 * - L'horodatage
 * - L'opérateur (commercial)
 * - Un commentaire optionnel
 *
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * @author BMAD Pipeline - Étape 6
 * @version 1.0
 */
public class Modification {

    // === ATTRIBUTS ===

    /** Identifiant unique de la modification */
    private String id;

    /** Horodatage de la modification */
    private LocalDateTime timestamp;

    /** Type d'action effectuée */
    private ValidationAction action;

    /** Nom du champ modifié (pour les modifications de valeur) */
    private String fieldName;

    /** Valeur avant modification */
    private String oldValue;

    /** Valeur après modification */
    private String newValue;

    /** Numéro de ligne concernée (pour les modifications d'articles) */
    private Integer lineNumber;

    /** Référence de l'article concerné */
    private String itemReference;

    /** Commentaire/justification de la modification */
    private String comment;

    /** Nom de l'opérateur (commercial) ayant effectué la modification */
    private String operator;

    /** Indique si la modification a été appliquée avec succès */
    private boolean applied;

    /** Message d'erreur si la modification a échoué */
    private String errorMessage;

    // === COMPTEUR POUR ID UNIQUE ===

    private static int counter = 0;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public Modification() {
        this.id = generateId();
        this.timestamp = LocalDateTime.now();
        this.applied = false;
    }

    /**
     * Constructeur avec action.
     *
     * @param action Type d'action
     */
    public Modification(ValidationAction action) {
        this();
        this.action = action;
    }

    /**
     * Constructeur complet pour une modification d'article.
     *
     * @param action Type d'action
     * @param lineNumber Numéro de ligne
     * @param fieldName Nom du champ
     * @param oldValue Ancienne valeur
     * @param newValue Nouvelle valeur
     */
    public Modification(ValidationAction action, Integer lineNumber, String fieldName,
                        String oldValue, String newValue) {
        this(action);
        this.lineNumber = lineNumber;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Constructeur pour une modification globale.
     *
     * @param action Type d'action
     * @param fieldName Nom du champ
     * @param oldValue Ancienne valeur
     * @param newValue Nouvelle valeur
     */
    public Modification(ValidationAction action, String fieldName,
                        String oldValue, String newValue) {
        this(action);
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // === MÉTHODES FACTORY ===

    /**
     * Crée une modification pour un changement de quantité.
     *
     * @param lineNumber Numéro de ligne
     * @param oldQty Ancienne quantité
     * @param newQty Nouvelle quantité
     * @return Modification configurée
     */
    public static Modification quantityChange(int lineNumber, int oldQty, int newQty) {
        return new Modification(
            ValidationAction.MODIFY_QUANTITY,
            lineNumber,
            "quantity",
            String.valueOf(oldQty),
            String.valueOf(newQty)
        );
    }

    /**
     * Crée une modification pour un changement de prix.
     *
     * @param lineNumber Numéro de ligne
     * @param oldPrice Ancien prix
     * @param newPrice Nouveau prix
     * @return Modification configurée
     */
    public static Modification priceChange(int lineNumber, double oldPrice, double newPrice) {
        return new Modification(
            ValidationAction.MODIFY_PRICE,
            lineNumber,
            "unitPriceHT",
            String.format("%.2f", oldPrice),
            String.format("%.2f", newPrice)
        );
    }

    /**
     * Crée une modification pour l'ajout d'un article.
     *
     * @param lineNumber Numéro de ligne attribué
     * @param designation Désignation de l'article
     * @return Modification configurée
     */
    public static Modification itemAdded(int lineNumber, String designation) {
        Modification mod = new Modification(ValidationAction.ADD_ITEM);
        mod.setLineNumber(lineNumber);
        mod.setFieldName("article");
        mod.setOldValue(null);
        mod.setNewValue(designation);
        return mod;
    }

    /**
     * Crée une modification pour la suppression d'un article.
     *
     * @param lineNumber Numéro de ligne
     * @param designation Désignation de l'article supprimé
     * @return Modification configurée
     */
    public static Modification itemRemoved(int lineNumber, String designation) {
        Modification mod = new Modification(ValidationAction.REMOVE_ITEM);
        mod.setLineNumber(lineNumber);
        mod.setFieldName("article");
        mod.setOldValue(designation);
        mod.setNewValue(null);
        return mod;
    }

    /**
     * Crée une modification pour l'application d'une remise.
     *
     * @param lineNumber Numéro de ligne (null si remise globale)
     * @param discountPercent Pourcentage de remise
     * @return Modification configurée
     */
    public static Modification discountApplied(Integer lineNumber, double discountPercent) {
        ValidationAction action = lineNumber != null ?
            ValidationAction.APPLY_ITEM_DISCOUNT :
            ValidationAction.APPLY_GLOBAL_DISCOUNT;

        Modification mod = new Modification(action);
        mod.setLineNumber(lineNumber);
        mod.setFieldName("discountPercent");
        mod.setOldValue("0");
        mod.setNewValue(String.format("%.2f", discountPercent));
        return mod;
    }

    /**
     * Crée une modification pour l'approbation du devis.
     *
     * @param comment Commentaire d'approbation
     * @return Modification configurée
     */
    public static Modification approval(String comment) {
        Modification mod = new Modification(ValidationAction.APPROVE);
        mod.setFieldName("status");
        mod.setOldValue("A_VALIDER");
        mod.setNewValue("PRET");
        mod.setComment(comment);
        return mod;
    }

    /**
     * Crée une modification pour le rejet du devis.
     *
     * @param reason Raison du rejet
     * @return Modification configurée
     */
    public static Modification rejection(String reason) {
        Modification mod = new Modification(ValidationAction.REJECT);
        mod.setFieldName("status");
        mod.setOldValue("A_VALIDER");
        mod.setNewValue("REJETE");
        mod.setComment(reason);
        return mod;
    }

    // === GÉNÉRATION ID ===

    /**
     * Génère un identifiant unique pour la modification.
     *
     * @return Identifiant unique
     */
    private static synchronized String generateId() {
        counter++;
        return String.format("MOD-%d-%04d",
            System.currentTimeMillis() % 100000, counter);
    }

    // === GETTERS ET SETTERS ===

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ValidationAction getAction() {
        return action;
    }

    public void setAction(ValidationAction action) {
        this.action = action;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getItemReference() {
        return itemReference;
    }

    public void setItemReference(String itemReference) {
        this.itemReference = itemReference;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Marque la modification comme appliquée.
     */
    public void markAsApplied() {
        this.applied = true;
        this.errorMessage = null;
    }

    /**
     * Marque la modification comme échouée.
     *
     * @param error Message d'erreur
     */
    public void markAsFailed(String error) {
        this.applied = false;
        this.errorMessage = error;
    }

    /**
     * Vérifie si la modification porte sur un article.
     *
     * @return true si la modification porte sur un article
     */
    public boolean isItemModification() {
        return lineNumber != null && action != null && action.isItemAction();
    }

    /**
     * Vérifie si la modification est une décision finale.
     *
     * @return true si c'est une décision finale
     */
    public boolean isFinalDecision() {
        return action != null && action.isFinalDecision();
    }

    /**
     * Vérifie si la modification a changé une valeur.
     *
     * @return true si les valeurs sont différentes
     */
    public boolean hasValueChange() {
        if (oldValue == null && newValue == null) return false;
        if (oldValue == null || newValue == null) return true;
        return !oldValue.equals(newValue);
    }

    /**
     * Retourne l'horodatage formaté.
     *
     * @return Date/heure formatée
     */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "N/A";
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    /**
     * Génère un résumé de la modification.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(getFormattedTimestamp()).append("] ");
        sb.append(action != null ? action.getLabel() : "???");

        if (lineNumber != null) {
            sb.append(" (ligne ").append(lineNumber).append(")");
        }

        if (fieldName != null) {
            sb.append(" - ").append(fieldName).append(": ");
            sb.append(oldValue != null ? oldValue : "∅");
            sb.append(" → ");
            sb.append(newValue != null ? newValue : "∅");
        }

        if (operator != null) {
            sb.append(" [par ").append(operator).append("]");
        }

        if (!applied && errorMessage != null) {
            sb.append(" ⚠ ÉCHEC: ").append(errorMessage);
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Modification that = (Modification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Modification {\n" +
               "  id: \"" + id + "\"\n" +
               "  timestamp: " + getFormattedTimestamp() + "\n" +
               "  action: " + (action != null ? action.getLabel() : "null") + "\n" +
               "  fieldName: \"" + fieldName + "\"\n" +
               "  oldValue: \"" + oldValue + "\"\n" +
               "  newValue: \"" + newValue + "\"\n" +
               (lineNumber != null ? "  lineNumber: " + lineNumber + "\n" : "") +
               (operator != null ? "  operator: \"" + operator + "\"\n" : "") +
               (comment != null ? "  comment: \"" + comment + "\"\n" : "") +
               "  applied: " + applied + "\n" +
               (errorMessage != null ? "  errorMessage: \"" + errorMessage + "\"\n" : "") +
               "}";
    }
}
