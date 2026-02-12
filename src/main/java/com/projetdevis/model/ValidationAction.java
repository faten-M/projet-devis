package com.projetdevis.model;

/**
 * Énumération des actions de validation possibles sur un brouillon de devis.
 *
 * Chaque action représente une modification ou décision que le commercial
 * peut effectuer lors de la validation humaine du devis.
 *
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * @author BMAD Pipeline - Étape 6
 * @version 1.0
 */
public enum ValidationAction {

    // === ACTIONS SUR LES ARTICLES ===

    /**
     * Modification de la quantité d'un article.
     */
    MODIFY_QUANTITY("Modification quantité", "Modifier la quantité d'un article", true),

    /**
     * Modification du prix unitaire d'un article.
     */
    MODIFY_PRICE("Modification prix", "Modifier le prix unitaire HT d'un article", true),

    /**
     * Modification de la désignation d'un article.
     */
    MODIFY_DESIGNATION("Modification désignation", "Modifier le nom/désignation d'un article", true),

    /**
     * Modification de la description d'un article.
     */
    MODIFY_DESCRIPTION("Modification description", "Modifier la description détaillée d'un article", true),

    /**
     * Modification de la catégorie d'un article.
     */
    MODIFY_CATEGORY("Modification catégorie", "Changer la catégorie d'un article", true),

    /**
     * Ajout d'un nouvel article au devis.
     */
    ADD_ITEM("Ajout article", "Ajouter un nouvel article au devis", true),

    /**
     * Suppression d'un article du devis.
     */
    REMOVE_ITEM("Suppression article", "Retirer un article du devis", true),

    /**
     * Validation du statut d'un article (passage en COMPLETE).
     */
    VALIDATE_ITEM("Validation article", "Valider les informations d'un article", true),

    // === ACTIONS SUR LES REMISES ===

    /**
     * Application d'une remise sur un article.
     */
    APPLY_ITEM_DISCOUNT("Remise article", "Appliquer une remise sur un article", true),

    /**
     * Application d'une remise globale sur le devis.
     */
    APPLY_GLOBAL_DISCOUNT("Remise globale", "Appliquer une remise sur le total du devis", false),

    /**
     * Suppression d'une remise.
     */
    REMOVE_DISCOUNT("Suppression remise", "Retirer une remise appliquée", true),

    // === ACTIONS SUR LES CONDITIONS ===

    /**
     * Modification des conditions de livraison.
     */
    MODIFY_DELIVERY("Modification livraison", "Modifier les conditions de livraison", false),

    /**
     * Modification de la date de livraison.
     */
    MODIFY_DELIVERY_DATE("Modification date livraison", "Modifier la date de livraison estimée", false),

    /**
     * Modification des frais de livraison.
     */
    MODIFY_DELIVERY_FEES("Modification frais livraison", "Modifier les frais de livraison", false),

    /**
     * Modification des conditions de paiement.
     */
    MODIFY_PAYMENT_TERMS("Modification paiement", "Modifier les conditions de paiement", false),

    /**
     * Modification de la garantie.
     */
    MODIFY_WARRANTY("Modification garantie", "Modifier les conditions de garantie", false),

    /**
     * Ajout d'une condition spéciale.
     */
    ADD_CONDITION("Ajout condition", "Ajouter une condition spéciale au devis", false),

    /**
     * Suppression d'une condition spéciale.
     */
    REMOVE_CONDITION("Suppression condition", "Retirer une condition spéciale", false),

    // === ACTIONS SUR LE DEVIS ===

    /**
     * Modification de l'objet du devis.
     */
    MODIFY_SUBJECT("Modification objet", "Modifier l'objet/titre du devis", false),

    /**
     * Modification de la validité du devis.
     */
    MODIFY_VALIDITY("Modification validité", "Modifier la durée de validité du devis", false),

    /**
     * Modification de la priorité.
     */
    MODIFY_PRIORITY("Modification priorité", "Modifier le niveau de priorité", false),

    /**
     * Ajout d'une recommandation.
     */
    ADD_RECOMMENDATION("Ajout recommandation", "Ajouter une recommandation interne", false),

    /**
     * Résolution d'une action requise.
     */
    RESOLVE_ACTION("Résolution action", "Marquer une action requise comme résolue", false),

    /**
     * Ajout d'un commentaire.
     */
    ADD_COMMENT("Ajout commentaire", "Ajouter un commentaire de validation", false),

    // === DÉCISIONS FINALES ===

    /**
     * Approbation du devis (prêt à être envoyé).
     */
    APPROVE("Approbation", "Valider et approuver le devis pour envoi", false),

    /**
     * Rejet du devis.
     */
    REJECT("Rejet", "Rejeter le devis (non valide ou non pertinent)", false),

    /**
     * Demande de modification supplémentaire.
     */
    REQUEST_CHANGES("Demande modifications", "Demander des modifications avant approbation", false),

    /**
     * Mise en attente.
     */
    PUT_ON_HOLD("Mise en attente", "Mettre le devis en attente (info manquante)", false);

    // === ATTRIBUTS ===

    private final String label;
    private final String description;
    private final boolean requiresLineNumber;

    // === CONSTRUCTEUR ===

    ValidationAction(String label, String description, boolean requiresLineNumber) {
        this.label = label;
        this.description = description;
        this.requiresLineNumber = requiresLineNumber;
    }

    // === GETTERS ===

    /**
     * Retourne le libellé de l'action.
     *
     * @return Libellé court
     */
    public String getLabel() {
        return label;
    }

    /**
     * Retourne la description de l'action.
     *
     * @return Description complète
     */
    public String getDescription() {
        return description;
    }

    /**
     * Indique si l'action nécessite un numéro de ligne.
     *
     * @return true si un numéro de ligne est requis
     */
    public boolean requiresLineNumber() {
        return requiresLineNumber;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si l'action est une décision finale.
     *
     * @return true si c'est une décision finale
     */
    public boolean isFinalDecision() {
        return this == APPROVE || this == REJECT;
    }

    /**
     * Vérifie si l'action modifie un article.
     *
     * @return true si l'action porte sur un article
     */
    public boolean isItemAction() {
        return this == MODIFY_QUANTITY ||
               this == MODIFY_PRICE ||
               this == MODIFY_DESIGNATION ||
               this == MODIFY_DESCRIPTION ||
               this == MODIFY_CATEGORY ||
               this == ADD_ITEM ||
               this == REMOVE_ITEM ||
               this == VALIDATE_ITEM ||
               this == APPLY_ITEM_DISCOUNT ||
               this == REMOVE_DISCOUNT;
    }

    /**
     * Vérifie si l'action modifie les conditions.
     *
     * @return true si l'action porte sur les conditions
     */
    public boolean isConditionAction() {
        return this == MODIFY_DELIVERY ||
               this == MODIFY_DELIVERY_DATE ||
               this == MODIFY_DELIVERY_FEES ||
               this == MODIFY_PAYMENT_TERMS ||
               this == MODIFY_WARRANTY ||
               this == ADD_CONDITION ||
               this == REMOVE_CONDITION;
    }

    /**
     * Vérifie si l'action est critique (nécessite attention).
     *
     * @return true si l'action est critique
     */
    public boolean isCritical() {
        return this == REMOVE_ITEM ||
               this == REJECT ||
               this == APPLY_GLOBAL_DISCOUNT;
    }

    @Override
    public String toString() {
        return label;
    }
}
