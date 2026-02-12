package com.projetdevis.service;

import com.projetdevis.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de validation humaine des brouillons de devis.
 *
 * Ce service permet au commercial de :
 * 1. Modifier les articles (quantité, prix, désignation)
 * 2. Ajouter ou supprimer des articles
 * 3. Appliquer des remises
 * 4. Modifier les conditions (livraison, paiement, etc.)
 * 5. Résoudre les actions en attente
 * 6. Approuver ou rejeter le devis
 *
 * Toutes les modifications sont tracées dans l'historique.
 *
 * Pipeline BMAD - Étape 6 : Validation humaine
 *
 * @author BMAD Pipeline - Étape 6
 * @version 1.0
 */
public class ValidationService {

    // === CONSTANTES ===

    /** Remise maximale autorisée (%) */
    private static final double MAX_DISCOUNT_PERCENT = 30.0;

    /** Remise nécessitant approbation manager (%) */
    private static final double MANAGER_APPROVAL_THRESHOLD = 15.0;

    /** Quantité maximale par ligne */
    private static final int MAX_QUANTITY = 1000;

    /** Prix unitaire maximum (€) */
    private static final double MAX_UNIT_PRICE = 50000.0;

    // === ATTRIBUTS ===

    /** Devis en cours de validation */
    private ValidatedQuote currentValidation;

    /** Nom de l'opérateur (commercial) */
    private String operatorName;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public ValidationService() {
        this.operatorName = "Commercial";
    }

    /**
     * Constructeur avec nom d'opérateur.
     *
     * @param operatorName Nom du commercial
     */
    public ValidationService(String operatorName) {
        this.operatorName = operatorName;
    }

    // === MÉTHODE PRINCIPALE ===

    /**
     * Démarre la validation d'un brouillon de devis.
     *
     * @param draft Brouillon à valider
     * @return Devis en validation
     */
    public ValidatedQuote startValidation(DraftQuote draft) {
        if (draft == null) {
            throw new IllegalArgumentException("Le brouillon ne peut pas être null");
        }

        this.currentValidation = new ValidatedQuote(draft, operatorName);

        // Vérification initiale
        performInitialChecks();

        return currentValidation;
    }

    /**
     * Effectue les vérifications initiales.
     */
    private void performInitialChecks() {
        DraftQuote draft = currentValidation.getDraft();

        // Vérifier les articles sans prix
        List<QuoteItem> itemsWithoutPrice = draft.getItemsWithoutPrice();
        if (!itemsWithoutPrice.isEmpty()) {
            for (QuoteItem item : itemsWithoutPrice) {
                currentValidation.addPendingAction(
                    "Définir le prix de l'article ligne " + item.getLineNumber() +
                    " (" + item.getDesignation() + ")"
                );
            }
        }

        // Vérifier le budget
        if (draft.isBudgetRespected() != null && !draft.isBudgetRespected()) {
            currentValidation.addPendingAction("Ajuster le devis pour respecter le budget client");
        }

        // Vérifier les incohérences
        if (draft.hasInconsistencies()) {
            for (String inconsistency : draft.getInconsistencies()) {
                currentValidation.addPendingAction("Résoudre : " + inconsistency);
            }
        }
    }

    // === MODIFICATIONS DES ARTICLES ===

    /**
     * Modifie la quantité d'un article.
     *
     * @param lineNumber Numéro de ligne
     * @param newQuantity Nouvelle quantité
     * @return true si la modification a réussi
     */
    public boolean modifyQuantity(int lineNumber, int newQuantity) {
        checkValidationInProgress();

        // Validation
        if (newQuantity <= 0) {
            return recordFailedModification(ValidationAction.MODIFY_QUANTITY, lineNumber,
                "La quantité doit être positive");
        }
        if (newQuantity > MAX_QUANTITY) {
            return recordFailedModification(ValidationAction.MODIFY_QUANTITY, lineNumber,
                "Quantité maximale dépassée (" + MAX_QUANTITY + ")");
        }

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.MODIFY_QUANTITY, lineNumber,
                "Article non trouvé");
        }

        // Enregistrement de la modification
        int oldQuantity = item.getQuantity();
        Modification mod = Modification.quantityChange(lineNumber, oldQuantity, newQuantity);
        mod.setItemReference(item.getReference());

        // Application
        item.setQuantity(newQuantity);
        recalculateTotals();

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie le prix unitaire d'un article.
     *
     * @param lineNumber Numéro de ligne
     * @param newPrice Nouveau prix unitaire HT
     * @return true si la modification a réussi
     */
    public boolean modifyPrice(int lineNumber, double newPrice) {
        checkValidationInProgress();

        // Validation
        if (newPrice < 0) {
            return recordFailedModification(ValidationAction.MODIFY_PRICE, lineNumber,
                "Le prix ne peut pas être négatif");
        }
        if (newPrice > MAX_UNIT_PRICE) {
            return recordFailedModification(ValidationAction.MODIFY_PRICE, lineNumber,
                "Prix unitaire maximum dépassé (" + MAX_UNIT_PRICE + " €)");
        }

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.MODIFY_PRICE, lineNumber,
                "Article non trouvé");
        }

        // Enregistrement
        double oldPrice = item.getUnitPriceHT() != null ? item.getUnitPriceHT() : 0;
        Modification mod = Modification.priceChange(lineNumber, oldPrice, newPrice);
        mod.setItemReference(item.getReference());

        // Application
        item.setUnitPriceHT(newPrice);
        item.setPriceConfidence(1.0); // Prix validé manuellement
        item.setStatus(QuoteItem.LineStatus.A_VALIDER);
        recalculateTotals();

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie la désignation d'un article.
     *
     * @param lineNumber Numéro de ligne
     * @param newDesignation Nouvelle désignation
     * @return true si la modification a réussi
     */
    public boolean modifyDesignation(int lineNumber, String newDesignation) {
        checkValidationInProgress();

        if (newDesignation == null || newDesignation.isBlank()) {
            return recordFailedModification(ValidationAction.MODIFY_DESIGNATION, lineNumber,
                "La désignation ne peut pas être vide");
        }

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.MODIFY_DESIGNATION, lineNumber,
                "Article non trouvé");
        }

        String oldDesignation = item.getDesignation();
        Modification mod = new Modification(
            ValidationAction.MODIFY_DESIGNATION, lineNumber,
            "designation", oldDesignation, newDesignation
        );
        mod.setItemReference(item.getReference());

        item.setDesignation(newDesignation);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie la description d'un article.
     *
     * @param lineNumber Numéro de ligne
     * @param newDescription Nouvelle description
     * @return true si la modification a réussi
     */
    public boolean modifyDescription(int lineNumber, String newDescription) {
        checkValidationInProgress();

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.MODIFY_DESCRIPTION, lineNumber,
                "Article non trouvé");
        }

        String oldDescription = item.getDescription();
        Modification mod = new Modification(
            ValidationAction.MODIFY_DESCRIPTION, lineNumber,
            "description", oldDescription, newDescription
        );
        mod.setItemReference(item.getReference());

        item.setDescription(newDescription);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Valide un article (marque comme COMPLETE).
     *
     * @param lineNumber Numéro de ligne
     * @return true si la validation a réussi
     */
    public boolean validateItem(int lineNumber) {
        checkValidationInProgress();

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.VALIDATE_ITEM, lineNumber,
                "Article non trouvé");
        }

        // Vérifier que l'article est complet
        if (!item.hasPrice()) {
            return recordFailedModification(ValidationAction.VALIDATE_ITEM, lineNumber,
                "L'article n'a pas de prix défini");
        }
        if (item.getDesignation() == null || item.getDesignation().isBlank()) {
            return recordFailedModification(ValidationAction.VALIDATE_ITEM, lineNumber,
                "L'article n'a pas de désignation");
        }

        QuoteItem.LineStatus oldStatus = item.getStatus();
        Modification mod = new Modification(
            ValidationAction.VALIDATE_ITEM, lineNumber,
            "status", oldStatus.getLabel(), QuoteItem.LineStatus.COMPLETE.getLabel()
        );
        mod.setItemReference(item.getReference());

        item.setStatus(QuoteItem.LineStatus.COMPLETE);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    // === AJOUT ET SUPPRESSION D'ARTICLES ===

    /**
     * Ajoute un nouvel article au devis.
     *
     * @param designation Désignation de l'article
     * @param quantity Quantité
     * @param unitPrice Prix unitaire HT
     * @param category Catégorie
     * @return Numéro de ligne attribué, ou -1 si échec
     */
    public int addItem(String designation, int quantity, double unitPrice,
                       AnalyzedItem.Category category) {
        checkValidationInProgress();

        // Validations
        if (designation == null || designation.isBlank()) {
            recordFailedModification(ValidationAction.ADD_ITEM, -1,
                "La désignation est requise");
            return -1;
        }
        if (quantity <= 0) {
            recordFailedModification(ValidationAction.ADD_ITEM, -1,
                "La quantité doit être positive");
            return -1;
        }
        if (unitPrice < 0) {
            recordFailedModification(ValidationAction.ADD_ITEM, -1,
                "Le prix ne peut pas être négatif");
            return -1;
        }

        DraftQuote draft = currentValidation.getDraft();

        // Création du nouvel article
        int newLineNumber = draft.getItemCount() + 1;

        QuoteItem newItem = new QuoteItem();
        newItem.setLineNumber(newLineNumber);
        newItem.setDesignation(designation);
        newItem.setQuantity(quantity);
        newItem.setUnitPriceHT(unitPrice);
        newItem.setCategory(category != null ? category : AnalyzedItem.Category.AUTRE);
        newItem.setReference(generateReference(newItem.getCategory(), newLineNumber));
        newItem.setStatus(QuoteItem.LineStatus.COMPLETE);
        newItem.setPriceConfidence(1.0);

        // Ajout au devis
        draft.addItem(newItem);
        recalculateTotals();

        // Enregistrement de la modification
        Modification mod = Modification.itemAdded(newLineNumber, designation);
        mod.setItemReference(newItem.getReference());
        mod.markAsApplied();
        currentValidation.addModification(mod);

        currentValidation.addReviewComment("Article ajouté : " + designation);

        return newLineNumber;
    }

    /**
     * Supprime un article du devis.
     *
     * @param lineNumber Numéro de ligne
     * @return true si la suppression a réussi
     */
    public boolean removeItem(int lineNumber) {
        checkValidationInProgress();

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.REMOVE_ITEM, lineNumber,
                "Article non trouvé");
        }

        DraftQuote draft = currentValidation.getDraft();
        String designation = item.getDesignation();
        String reference = item.getReference();

        // Suppression
        draft.getItems().remove(item);

        // Renumérotation des lignes
        renumberLines();
        recalculateTotals();

        // Enregistrement
        Modification mod = Modification.itemRemoved(lineNumber, designation);
        mod.setItemReference(reference);
        mod.markAsApplied();
        currentValidation.addModification(mod);

        currentValidation.addReviewComment("Article supprimé : " + designation);

        return true;
    }

    // === GESTION DES REMISES ===

    /**
     * Applique une remise sur un article.
     *
     * @param lineNumber Numéro de ligne
     * @param discountPercent Pourcentage de remise
     * @return true si l'application a réussi
     */
    public boolean applyItemDiscount(int lineNumber, double discountPercent) {
        checkValidationInProgress();

        // Validations
        if (discountPercent < 0) {
            return recordFailedModification(ValidationAction.APPLY_ITEM_DISCOUNT, lineNumber,
                "La remise ne peut pas être négative");
        }
        if (discountPercent > MAX_DISCOUNT_PERCENT) {
            return recordFailedModification(ValidationAction.APPLY_ITEM_DISCOUNT, lineNumber,
                "Remise maximale dépassée (" + MAX_DISCOUNT_PERCENT + "%)");
        }

        QuoteItem item = findItem(lineNumber);
        if (item == null) {
            return recordFailedModification(ValidationAction.APPLY_ITEM_DISCOUNT, lineNumber,
                "Article non trouvé");
        }

        Double oldDiscount = item.getDiscountPercent();
        Modification mod = Modification.discountApplied(lineNumber, discountPercent);
        if (oldDiscount != null) {
            mod.setOldValue(String.format("%.2f", oldDiscount));
        }
        mod.setItemReference(item.getReference());

        // Application
        item.setDiscountPercent(discountPercent);
        recalculateTotals();

        mod.markAsApplied();
        currentValidation.addModification(mod);

        // Avertissement si remise importante
        if (discountPercent > MANAGER_APPROVAL_THRESHOLD) {
            currentValidation.addReviewComment(
                "Attention : remise de " + discountPercent + "% appliquée (seuil manager dépassé)");
        }

        return true;
    }

    /**
     * Applique une remise globale sur tous les articles.
     *
     * @param discountPercent Pourcentage de remise
     * @return true si l'application a réussi
     */
    public boolean applyGlobalDiscount(double discountPercent) {
        checkValidationInProgress();

        if (discountPercent < 0 || discountPercent > MAX_DISCOUNT_PERCENT) {
            Modification mod = new Modification(ValidationAction.APPLY_GLOBAL_DISCOUNT);
            mod.setFieldName("globalDiscount");
            mod.setNewValue(String.format("%.2f", discountPercent));
            mod.markAsFailed("Remise invalide (0-" + MAX_DISCOUNT_PERCENT + "%)");
            currentValidation.addModification(mod);
            return false;
        }

        DraftQuote draft = currentValidation.getDraft();
        int applied = 0;

        for (QuoteItem item : draft.getItems()) {
            if (item.hasPrice()) {
                item.setDiscountPercent(discountPercent);
                applied++;
            }
        }

        recalculateTotals();

        Modification mod = Modification.discountApplied(null, discountPercent);
        mod.setComment("Appliquée sur " + applied + " articles");
        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    // === MODIFICATION DES CONDITIONS ===

    /**
     * Modifie les frais de livraison.
     *
     * @param deliveryFees Nouveaux frais de livraison
     * @return true si la modification a réussi
     */
    public boolean modifyDeliveryFees(double deliveryFees) {
        checkValidationInProgress();

        DraftQuote draft = currentValidation.getDraft();
        Double oldFees = draft.getDeliveryFees();

        Modification mod = new Modification(
            ValidationAction.MODIFY_DELIVERY_FEES,
            "deliveryFees",
            oldFees != null ? String.format("%.2f", oldFees) : "0",
            String.format("%.2f", deliveryFees)
        );

        draft.setDeliveryFees(deliveryFees);
        draft.setDeliveryIncluded(deliveryFees == 0);
        recalculateTotals();

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie la date de livraison estimée.
     *
     * @param newDate Nouvelle date de livraison
     * @return true si la modification a réussi
     */
    public boolean modifyDeliveryDate(LocalDate newDate) {
        checkValidationInProgress();

        if (newDate == null) {
            return false;
        }

        if (newDate.isBefore(LocalDate.now())) {
            Modification mod = new Modification(ValidationAction.MODIFY_DELIVERY_DATE);
            mod.markAsFailed("La date ne peut pas être dans le passé");
            currentValidation.addModification(mod);
            return false;
        }

        DraftQuote draft = currentValidation.getDraft();
        LocalDate oldDate = draft.getEstimatedDeliveryDate();

        Modification mod = new Modification(
            ValidationAction.MODIFY_DELIVERY_DATE,
            "estimatedDeliveryDate",
            oldDate != null ? oldDate.toString() : "null",
            newDate.toString()
        );

        draft.setEstimatedDeliveryDate(newDate);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie les conditions de paiement.
     *
     * @param paymentTerms Nouvelles conditions
     * @return true si la modification a réussi
     */
    public boolean modifyPaymentTerms(String paymentTerms) {
        checkValidationInProgress();

        DraftQuote draft = currentValidation.getDraft();
        String oldTerms = draft.getPaymentTerms();

        Modification mod = new Modification(
            ValidationAction.MODIFY_PAYMENT_TERMS,
            "paymentTerms",
            oldTerms,
            paymentTerms
        );

        draft.setPaymentTerms(paymentTerms);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie la garantie.
     *
     * @param warranty Nouvelle garantie
     * @return true si la modification a réussi
     */
    public boolean modifyWarranty(String warranty) {
        checkValidationInProgress();

        DraftQuote draft = currentValidation.getDraft();
        String oldWarranty = draft.getWarranty();

        Modification mod = new Modification(
            ValidationAction.MODIFY_WARRANTY,
            "warranty",
            oldWarranty,
            warranty
        );

        draft.setWarranty(warranty);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Ajoute une condition spéciale.
     *
     * @param condition Condition à ajouter
     * @return true si l'ajout a réussi
     */
    public boolean addSpecialCondition(String condition) {
        checkValidationInProgress();

        if (condition == null || condition.isBlank()) {
            return false;
        }

        DraftQuote draft = currentValidation.getDraft();

        Modification mod = new Modification(
            ValidationAction.ADD_CONDITION,
            "specialConditions",
            null,
            condition
        );

        draft.addSpecialCondition(condition);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Modifie la validité du devis.
     *
     * @param validityDays Nombre de jours de validité
     * @return true si la modification a réussi
     */
    public boolean modifyValidity(int validityDays) {
        checkValidationInProgress();

        if (validityDays <= 0 || validityDays > 90) {
            Modification mod = new Modification(ValidationAction.MODIFY_VALIDITY);
            mod.markAsFailed("La validité doit être entre 1 et 90 jours");
            currentValidation.addModification(mod);
            return false;
        }

        DraftQuote draft = currentValidation.getDraft();
        int oldValidity = draft.getValidityDays();

        Modification mod = new Modification(
            ValidationAction.MODIFY_VALIDITY,
            "validityDays",
            String.valueOf(oldValidity),
            String.valueOf(validityDays)
        );

        draft.setValidityDays(validityDays);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    // === RÉSOLUTION DES ACTIONS ===

    /**
     * Résout une action en attente par son index.
     *
     * @param actionIndex Index de l'action (0-based)
     * @param resolution Description de la résolution
     * @return true si la résolution a réussi
     */
    public boolean resolveAction(int actionIndex, String resolution) {
        checkValidationInProgress();

        List<String> actions = currentValidation.getPendingActions();
        if (actionIndex < 0 || actionIndex >= actions.size()) {
            return false;
        }

        String action = actions.get(actionIndex);

        Modification mod = new Modification(ValidationAction.RESOLVE_ACTION);
        mod.setFieldName("pendingAction");
        mod.setOldValue(action);
        mod.setNewValue("RÉSOLU");
        mod.setComment(resolution);

        currentValidation.resolvePendingAction(actionIndex);

        mod.markAsApplied();
        currentValidation.addModification(mod);

        return true;
    }

    /**
     * Ajoute un commentaire de révision.
     *
     * @param comment Commentaire à ajouter
     */
    public void addComment(String comment) {
        checkValidationInProgress();

        Modification mod = new Modification(ValidationAction.ADD_COMMENT);
        mod.setFieldName("reviewComment");
        mod.setNewValue(comment);

        currentValidation.addReviewComment(comment);

        mod.markAsApplied();
        currentValidation.addModification(mod);
    }

    // === DÉCISIONS FINALES ===

    /**
     * Approuve le devis (prêt pour envoi au client).
     *
     * @param comment Commentaire d'approbation
     * @return true si l'approbation a réussi
     */
    public boolean approve(String comment) {
        checkValidationInProgress();

        // Vérifications préalables
        if (!currentValidation.isReadyForApproval()) {
            Modification mod = Modification.approval(comment);
            mod.markAsFailed("Le devis n'est pas prêt pour approbation");
            currentValidation.addModification(mod);
            return false;
        }

        // Validation de tous les articles en A_VALIDER
        DraftQuote draft = currentValidation.getDraft();
        for (QuoteItem item : draft.getItems()) {
            if (item.getStatus() == QuoteItem.LineStatus.A_VALIDER) {
                item.setStatus(QuoteItem.LineStatus.COMPLETE);
            }
        }

        // Mise à jour du statut du devis
        draft.setStatus(DraftQuote.DraftStatus.PRET);

        // Mise à jour de la validation
        currentValidation.setValidationStatus(ValidatedQuote.ValidationStatus.APPROUVE);
        currentValidation.setValidatedAt(LocalDateTime.now());
        currentValidation.setValidationComment(comment);
        currentValidation.setValidatorSignature(generateSignature());

        // Enregistrement
        Modification mod = Modification.approval(comment);
        mod.markAsApplied();
        currentValidation.addModification(mod);

        // Finalisation des statistiques
        currentValidation.finalizeStats();

        return true;
    }

    /**
     * Rejette le devis.
     *
     * @param reason Raison du rejet
     * @return true si le rejet a réussi
     */
    public boolean reject(String reason) {
        checkValidationInProgress();

        if (reason == null || reason.isBlank()) {
            Modification mod = Modification.rejection(reason);
            mod.markAsFailed("Une raison de rejet est requise");
            currentValidation.addModification(mod);
            return false;
        }

        DraftQuote draft = currentValidation.getDraft();
        draft.setStatus(DraftQuote.DraftStatus.REJETE);

        currentValidation.setValidationStatus(ValidatedQuote.ValidationStatus.REJETE);
        currentValidation.setValidatedAt(LocalDateTime.now());
        currentValidation.setValidationComment(reason);

        Modification mod = Modification.rejection(reason);
        mod.markAsApplied();
        currentValidation.addModification(mod);

        currentValidation.finalizeStats();

        return true;
    }

    /**
     * Efface les incohérences du devis après vérification manuelle.
     *
     * @param comment Commentaire justifiant la suppression
     * @return true si l'opération a réussi
     */
    public boolean clearInconsistencies(String comment) {
        checkValidationInProgress();

        DraftQuote draft = currentValidation.getDraft();
        if (!draft.hasInconsistencies()) {
            return true;
        }

        int count = draft.getInconsistencies().size();

        Modification mod = new Modification(ValidationAction.RESOLVE_ACTION);
        mod.setFieldName("inconsistencies");
        mod.setOldValue(count + " incohérence(s)");
        mod.setNewValue("0");
        mod.setComment(comment != null ? comment : "Incohérences vérifiées et traitées");

        draft.getInconsistencies().clear();

        mod.markAsApplied();
        currentValidation.addModification(mod);

        currentValidation.addReviewComment("Incohérences effacées : " + comment);

        return true;
    }

    /**
     * Met le devis en attente.
     *
     * @param reason Raison de la mise en attente
     * @return true si la mise en attente a réussi
     */
    public boolean putOnHold(String reason) {
        checkValidationInProgress();

        DraftQuote draft = currentValidation.getDraft();
        draft.setStatus(DraftQuote.DraftStatus.A_COMPLETER);

        currentValidation.setValidationStatus(ValidatedQuote.ValidationStatus.EN_ATTENTE);

        Modification mod = new Modification(ValidationAction.PUT_ON_HOLD);
        mod.setFieldName("status");
        mod.setNewValue("EN_ATTENTE");
        mod.setComment(reason);
        mod.markAsApplied();
        currentValidation.addModification(mod);

        if (reason != null && !reason.isBlank()) {
            currentValidation.addPendingAction(reason);
        }

        return true;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie qu'une validation est en cours.
     */
    private void checkValidationInProgress() {
        if (currentValidation == null) {
            throw new IllegalStateException("Aucune validation en cours. Appelez startValidation() d'abord.");
        }
        if (currentValidation.isValidated()) {
            throw new IllegalStateException("La validation est déjà terminée.");
        }
    }

    /**
     * Trouve un article par son numéro de ligne.
     *
     * @param lineNumber Numéro de ligne
     * @return Article trouvé ou null
     */
    private QuoteItem findItem(int lineNumber) {
        DraftQuote draft = currentValidation.getDraft();
        return draft.getItems().stream()
            .filter(item -> item.getLineNumber() == lineNumber)
            .findFirst()
            .orElse(null);
    }

    /**
     * Enregistre une modification échouée.
     *
     * @param action Type d'action
     * @param lineNumber Numéro de ligne
     * @param error Message d'erreur
     * @return false (toujours)
     */
    private boolean recordFailedModification(ValidationAction action, int lineNumber, String error) {
        Modification mod = new Modification(action);
        mod.setLineNumber(lineNumber);
        mod.markAsFailed(error);
        currentValidation.addModification(mod);
        return false;
    }

    /**
     * Renuméroter les lignes après suppression.
     */
    private void renumberLines() {
        DraftQuote draft = currentValidation.getDraft();
        int lineNum = 1;
        for (QuoteItem item : draft.getItems()) {
            item.setLineNumber(lineNum++);
        }
    }

    /**
     * Recalcule les totaux du devis.
     */
    private void recalculateTotals() {
        currentValidation.getDraft().recalculateTotals();
    }

    /**
     * Génère une référence pour un nouvel article.
     *
     * @param category Catégorie
     * @param lineNum Numéro de ligne
     * @return Référence générée
     */
    private String generateReference(AnalyzedItem.Category category, int lineNum) {
        String catCode = switch (category) {
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

    /**
     * Génère une signature pour l'approbation.
     *
     * @return Signature générée
     */
    private String generateSignature() {
        return String.format("SIG-%s-%d",
            operatorName.substring(0, Math.min(3, operatorName.length())).toUpperCase(),
            System.currentTimeMillis() % 10000);
    }

    // === MÉTHODES D'ACCÈS ===

    /**
     * Retourne la validation en cours.
     *
     * @return Validation courante
     */
    public ValidatedQuote getCurrentValidation() {
        return currentValidation;
    }

    /**
     * Retourne le nom de l'opérateur.
     *
     * @return Nom de l'opérateur
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * Définit le nom de l'opérateur.
     *
     * @param operatorName Nom de l'opérateur
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    // === RAPPORT DE VALIDATION ===

    /**
     * Génère un rapport complet de validation.
     *
     * @return Rapport textuel
     */
    public String getValidationReport() {
        if (currentValidation == null) {
            return "Aucune validation en cours.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              RAPPORT DE VALIDATION - ÉTAPE 6                     ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");

        // Informations générales
        DraftQuote draft = currentValidation.getDraft();
        sb.append("DEVIS: ").append(draft.getQuoteNumber()).append("\n");
        sb.append("Validateur: ").append(operatorName).append("\n");
        sb.append("Statut validation: ").append(currentValidation.getValidationStatus().getLabel()).append("\n");
        sb.append("Débuté le: ").append(currentValidation.getFormattedValidationStartedAt()).append("\n");

        if (currentValidation.isValidated()) {
            sb.append("Finalisé le: ").append(currentValidation.getFormattedValidatedAt()).append("\n");
        }
        sb.append("\n");

        // Statistiques
        ValidatedQuote.ValidationStats stats = currentValidation.getStats();
        sb.append("STATISTIQUES:\n");
        sb.append(String.format("  Modifications totales: %d\n", stats.totalModifications));
        sb.append(String.format("  - Sur articles: %d (prix: %d, qté: %d)\n",
            stats.itemModifications, stats.priceModifications, stats.quantityModifications));
        sb.append(String.format("  - Ajouts: %d, Suppressions: %d\n",
            stats.itemsAdded, stats.itemsRemoved));
        sb.append(String.format("  - Remises appliquées: %d\n", stats.discountsApplied));
        sb.append("\n");

        // Totaux
        sb.append("TOTAUX:\n");
        sb.append(String.format("  Total HT original: %.2f €\n", stats.originalTotalHT));
        if (draft.getTotalHT() != null) {
            sb.append(String.format("  Total HT final: %.2f €\n", draft.getTotalHT()));
            double delta = draft.getTotalHT() - stats.originalTotalHT;
            String sign = delta >= 0 ? "+" : "";
            sb.append(String.format("  Écart: %s%.2f €\n", sign, delta));
        }
        sb.append(String.format("  Total TTC: %s\n", draft.getFormattedTotalTTC()));
        sb.append("\n");

        // Articles
        sb.append("ARTICLES: ").append(draft.getItemCount()).append(" lignes\n");
        for (QuoteItem item : draft.getItems()) {
            sb.append(String.format("  %d. %s | %d x %.2f € = %.2f € [%s]\n",
                item.getLineNumber(),
                item.getDesignation(),
                item.getQuantity(),
                item.getUnitPriceHT() != null ? item.getUnitPriceHT() : 0,
                item.getTotalPriceHT() != null ? item.getTotalPriceHT() : 0,
                item.getStatus().getLabel()
            ));
        }
        sb.append("\n");

        // Actions en attente
        if (currentValidation.hasPendingActions()) {
            sb.append("ACTIONS EN ATTENTE: ").append(currentValidation.getPendingActions().size()).append("\n");
            int i = 1;
            for (String action : currentValidation.getPendingActions()) {
                sb.append(String.format("  %d. □ %s\n", i++, action));
            }
            sb.append("\n");
        }

        // Prêt pour approbation ?
        sb.append("PRÊT POUR APPROBATION: ");
        sb.append(currentValidation.isReadyForApproval() ? "OUI" : "NON").append("\n");

        if (!currentValidation.isReadyForApproval()) {
            sb.append("  Raisons:\n");
            if (currentValidation.hasPendingActions()) {
                sb.append("    - Actions en attente non résolues\n");
            }
            if (draft.hasInconsistencies()) {
                sb.append("    - Incohérences non résolues\n");
            }
            if (!draft.getItemsWithoutPrice().isEmpty()) {
                sb.append("    - Articles sans prix\n");
            }
        }

        return sb.toString();
    }
}
