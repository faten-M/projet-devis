package com.projetdevis.service;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.QuoteItem;
import com.projetdevis.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Service de génération de brouillon de devis.
 *
 * Ce service transforme une AnalyzedInfo en DraftQuote commercial structuré.
 * Il applique les règles métier suivantes :
 * 1. Conversion des articles analysés en lignes de devis
 * 2. Estimation des prix par catégorie et gamme
 * 3. Calcul des totaux et vérification du budget
 * 4. Génération des recommandations et actions requises
 * 5. Évaluation de la confiance globale
 *
 * Pipeline BMAD - Étape 5 : Brouillon du devis
 *
 * @author BMAD Pipeline - Étape 5
 * @version 1.0
 */
@Service
public class DraftService {

    private final ProduitRepository produitRepository;

    /** Constructeur Spring : injecte le repository catalogue. */
    public DraftService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    /** Constructeur standalone pour démos / tests (utilise les prix codés en dur). */
    public DraftService() {
        this.produitRepository = null;
    }

    /**
     * Retourne les prix [économique, standard, premium] pour une catégorie.
     * Essaie d'abord le catalogue en base, puis se replie sur PRICE_GRID.
     */
    private double[] getPricesForCategory(AnalyzedItem.Category category) {
        if (produitRepository != null) {
            return produitRepository.findFirstByCategorie(category)
                .map(p -> new double[]{p.getPrixEconomique(), p.getPrixStandard(), p.getPrixPremium()})
                .orElseGet(() -> PRICE_GRID.getOrDefault(category, PRICE_GRID.get(AnalyzedItem.Category.AUTRE)));
        }
        return PRICE_GRID.getOrDefault(category, PRICE_GRID.get(AnalyzedItem.Category.AUTRE));
    }

    // === CONSTANTES ===

    /** Taux TVA par défaut */
    private static final double DEFAULT_TVA_RATE = 20.0;

    /** Validité par défaut du devis (jours) */
    private static final int DEFAULT_VALIDITY_DAYS = 30;

    /** Délai de livraison standard (jours ouvrés) */
    private static final int STANDARD_DELIVERY_DAYS = 15;

    /** Délai de livraison urgent (jours ouvrés) */
    private static final int URGENT_DELIVERY_DAYS = 7;

    /** Frais de livraison par défaut */
    private static final double DEFAULT_DELIVERY_FEES = 150.0;

    /** Seuil pour livraison gratuite */
    private static final double FREE_DELIVERY_THRESHOLD = 5000.0;

    // === GRILLES DE PRIX PAR CATÉGORIE ET GAMME ===

    /**
     * Prix estimés par catégorie (min, standard, premium).
     * Ces prix sont indicatifs et doivent être ajustés par le commercial.
     */
    private static final Map<AnalyzedItem.Category, double[]> PRICE_GRID = new HashMap<>();

    static {
        // [économique, standard, premium] — prix indicatifs par unité de vente courante
        PRICE_GRID.put(AnalyzedItem.Category.GROS_OEUVRE,   new double[]{ 6,   18,   45});
        PRICE_GRID.put(AnalyzedItem.Category.SECOND_OEUVRE, new double[]{ 8,   16,   28});
        PRICE_GRID.put(AnalyzedItem.Category.COUVERTURE,    new double[]{22,   38,   65});
        PRICE_GRID.put(AnalyzedItem.Category.CHARPENTE,     new double[]{ 5,   12,   28});
        PRICE_GRID.put(AnalyzedItem.Category.PLOMBERIE,     new double[]{ 8,   22,   55});
        PRICE_GRID.put(AnalyzedItem.Category.ELECTRICITE,   new double[]{ 5,   16,   42});
        PRICE_GRID.put(AnalyzedItem.Category.VRD,           new double[]{14,   32,   68});
        PRICE_GRID.put(AnalyzedItem.Category.ISOLATION,     new double[]{ 8,   18,   38});
        PRICE_GRID.put(AnalyzedItem.Category.FINITION,      new double[]{10,   26,   58});
        PRICE_GRID.put(AnalyzedItem.Category.AUTRE,         new double[]{10,   25,   50});
    }

    // === MOTS-CLÉS POUR ESTIMATION DE GAMME ===

    private static final Set<String> PREMIUM_KEYWORDS = new HashSet<>(Arrays.asList(
        "haute résistance", "hr", "52,5", "premier choix", "certifié",
        "nf", "traité autoclave", "classe a", "haute performance",
        "technique", "structural", "armé", "précontraint"
    ));

    private static final Set<String> ECONOMIQUE_KEYWORDS = new HashSet<>(Arrays.asList(
        "standard", "courant", "second choix", "allégé", "basique",
        "classe b", "tout venant", "recyclé"
    ));

    // === OPTIONS PAR CATÉGORIE ===

    private static final Map<AnalyzedItem.Category, List<String>> CATEGORY_OPTIONS = new HashMap<>();

    static {
        CATEGORY_OPTIONS.put(AnalyzedItem.Category.GROS_OEUVRE, Arrays.asList(
            "Livraison sur chantier en vrac",
            "Livraison en big-bag (1 tonne)",
            "Certificat de conformité CE fourni",
            "Fiche technique et fiche sécurité"
        ));
        CATEGORY_OPTIONS.put(AnalyzedItem.Category.COUVERTURE, Arrays.asList(
            "Sous-toiture fournie",
            "Accessoires de rive et faîtière inclus",
            "Film d'étanchéité inclus",
            "Pose sur liteau"
        ));
        CATEGORY_OPTIONS.put(AnalyzedItem.Category.PLOMBERIE, Arrays.asList(
            "Joints et colliers de fixation inclus",
            "Coudes et manchons fournis",
            "Test d'étanchéité"
        ));
        CATEGORY_OPTIONS.put(AnalyzedItem.Category.ELECTRICITE, Arrays.asList(
            "Dominos de connexion fournis",
            "Gaines de protection incluses",
            "Certification NFC 15-100"
        ));
        CATEGORY_OPTIONS.put(AnalyzedItem.Category.ISOLATION, Arrays.asList(
            "Pare-vapeur inclus",
            "Fixations et vis spéciales fournies",
            "Bande de rive étanche"
        ));
    }

    // === MÉTHODE PRINCIPALE ===

    /**
     * Génère un brouillon de devis à partir d'une analyse.
     *
     * @param analysis Analyse source (AnalyzedInfo)
     * @return Brouillon de devis généré
     */
    public DraftQuote generateDraft(AnalyzedInfo analysis) {
        long startTime = System.currentTimeMillis();

        // Création du brouillon
        DraftQuote draft = new DraftQuote(analysis);

        // Vérification de l'analyse source
        if (analysis == null || !analysis.hasItems()) {
            draft.setStatus(DraftQuote.DraftStatus.REJETE);
            draft.addWarning("Aucun article à traiter dans l'analyse source");
            return draft;
        }

        // Configuration initiale
        configureDraft(draft, analysis);

        // Conversion des articles
        convertItems(draft, analysis);

        // Estimation des prix
        estimatePrices(draft, analysis);

        // Calcul des totaux
        draft.recalculateTotals();

        // Vérification du budget
        checkBudget(draft, analysis);

        // Configuration de la livraison
        configureDelivery(draft, analysis);

        // Génération des recommandations
        generateRecommendations(draft, analysis);

        // Détermination des actions requises
        determineRequiredActions(draft);

        // Évaluation du statut final
        evaluateStatus(draft);

        // Calcul de la confiance
        calculateConfidence(draft, analysis);

        // Statistiques
        finalizeStats(draft, startTime);

        return draft;
    }

    // === MÉTHODES DE CONFIGURATION ===

    /**
     * Configure les paramètres initiaux du brouillon.
     */
    private void configureDraft(DraftQuote draft, AnalyzedInfo analysis) {
        // Objet du devis
        String subject = generateSubject(analysis);
        draft.setSubject(subject);

        // TVA
        draft.setTvaRate(DEFAULT_TVA_RATE);

        // Validité
        draft.setValidityDays(DEFAULT_VALIDITY_DAYS);

        // Priorité basée sur l'urgence
        if (analysis.getUrgency() != null) {
            draft.setPriority(mapUrgencyToPriority(analysis.getUrgency()));
        }

        // Conditions de paiement par défaut
        draft.setPaymentTerms("30 jours date de facture");

        // Garantie par défaut
        draft.setWarranty("2 ans pièces et main d'œuvre");

        // Transfert des incohérences
        if (analysis.hasInconsistencies()) {
            draft.setInconsistencies(new ArrayList<>(analysis.getInconsistencies()));
        }
    }

    /**
     * Génère l'objet du devis.
     */
    private String generateSubject(AnalyzedInfo analysis) {
        int itemCount = analysis.getItemCount();
        int totalQty = analysis.getTotalQuantity();

        // Catégories présentes
        Set<String> categories = new HashSet<>();
        for (AnalyzedItem item : analysis.getItems()) {
            if (item.getCategory() != null) {
                categories.add(item.getCategory().getLabel().toLowerCase());
            }
        }

        String catText = categories.isEmpty() ? "matériaux" : String.join(", ", categories);

        return String.format("Devis matériaux de construction - %d référence(s) (%d unités) : %s",
            itemCount, totalQty, catText);
    }

    /**
     * Convertit l'urgence en priorité.
     */
    private DraftQuote.Priority mapUrgencyToPriority(String urgency) {
        if (urgency == null) return DraftQuote.Priority.NORMALE;

        String u = urgency.toLowerCase();
        if (u.contains("critique") || u.contains("immédiat")) {
            return DraftQuote.Priority.CRITIQUE;
        } else if (u.contains("urgent") || u.contains("asap")) {
            return DraftQuote.Priority.URGENTE;
        } else if (u.contains("important") || u.contains("priorit")) {
            return DraftQuote.Priority.HAUTE;
        }
        return DraftQuote.Priority.NORMALE;
    }

    // === CONVERSION DES ARTICLES ===

    /**
     * Convertit les articles analysés en lignes de devis.
     */
    private void convertItems(DraftQuote draft, AnalyzedInfo analysis) {
        int lineNumber = 1;

        for (AnalyzedItem analyzedItem : analysis.getValidItems()) {
            QuoteItem quoteItem = new QuoteItem(analyzedItem, lineNumber);

            // Transfert des avertissements
            if (analyzedItem.hasWarnings()) {
                for (String warning : analyzedItem.getWarnings()) {
                    quoteItem.addWarning(warning);
                }
            }

            // Ajout des options disponibles
            addAvailableOptions(quoteItem, analyzedItem);

            // Ajout des alternatives
            suggestAlternatives(quoteItem, analyzedItem);

            // Notes internes
            generateInternalNotes(quoteItem, analyzedItem);

            draft.addItem(quoteItem);
            lineNumber++;
        }

        // Traitement des articles avec avertissements (non valides mais récupérables)
        for (AnalyzedItem analyzedItem : analysis.getItems()) {
            if (analyzedItem.getStatus() == AnalyzedItem.ValidationStatus.WARNING) {
                QuoteItem quoteItem = new QuoteItem(analyzedItem, lineNumber);
                quoteItem.setStatus(QuoteItem.LineStatus.A_COMPLETER);
                quoteItem.addWarning("Article nécessitant des précisions");

                draft.addItem(quoteItem);
                lineNumber++;
            }
        }
    }

    /**
     * Ajoute les options disponibles selon la catégorie.
     */
    private void addAvailableOptions(QuoteItem quoteItem, AnalyzedItem analyzedItem) {
        List<String> options = CATEGORY_OPTIONS.get(analyzedItem.getCategory());
        if (options != null) {
            for (String option : options) {
                quoteItem.addOption(option);
            }
        }
    }

    /**
     * Suggère des alternatives pour les matériaux de construction.
     */
    private void suggestAlternatives(QuoteItem quoteItem, AnalyzedItem analyzedItem) {
        AnalyzedItem.Category category = analyzedItem.getCategory();
        switch (category) {
            case GROS_OEUVRE:
                quoteItem.addAlternative("Béton prêt à l'emploi livré toupie (gain de temps)");
                quoteItem.addAlternative("Parpaing allégé (-20% poids, même résistance)");
                break;
            case COUVERTURE:
                quoteItem.addAlternative("Tuile béton (économie ~20% vs tuile terre cuite)");
                quoteItem.addAlternative("Bac acier isolant (pose rapide, garantie 30 ans)");
                break;
            case CHARPENTE:
                quoteItem.addAlternative("Lamellé-collé (portée plus grande)");
                quoteItem.addAlternative("Charpente métallique (délai raccourci)");
                break;
            case PLOMBERIE:
                quoteItem.addAlternative("PVC pression (économie ~30%)");
                quoteItem.addAlternative("Multicouche gainé (facilité de pose)");
                break;
            case ELECTRICITE:
                quoteItem.addAlternative("Câble rigide U-1000 R2V (plus économique)");
                quoteItem.addAlternative("Câble souple H07RN-F (environnement humide)");
                break;
            case ISOLATION:
                quoteItem.addAlternative("Laine de roche (meilleure résistance au feu)");
                quoteItem.addAlternative("Polystyrène expansé (léger, économique)");
                break;
            case FINITION:
                quoteItem.addAlternative("Carrelage grès cérame (durabilité supérieure)");
                quoteItem.addAlternative("Revêtement vinyle (pose rapide, moins cher)");
                break;
            default:
                break;
        }
    }

    // === ESTIMATION DES PRIX ===

    /**
     * Estime les prix pour tous les articles du brouillon.
     */
    private void estimatePrices(DraftQuote draft, AnalyzedInfo analysis) {
        for (QuoteItem item : draft.getItems()) {
            estimateItemPrice(item);
        }
    }

    /**
     * Génère des notes internes pour le commercial.
     */
    private void generateInternalNotes(QuoteItem quoteItem, AnalyzedItem analyzedItem) {
        if (analyzedItem.getQuantity() != null && analyzedItem.getQuantity() >= 10) {
            quoteItem.addInternalNote("Volume important — remise négociable (5-15%)");
        }
        if (analyzedItem.isMerged()) {
            quoteItem.addInternalNote(String.format(
                "Article fusionné (%d références similaires regroupées)",
                analyzedItem.getMergedCount()
            ));
        }
        if (analyzedItem.getConfidence() < 0.6) {
            quoteItem.addInternalNote("Confiance faible — vérifier la désignation avec le client");
        }
    }

    /**
     * Estime le prix d'un article.
     */
    private void estimateItemPrice(QuoteItem item) {
        AnalyzedItem source = item.getSourceItem();
        if (source == null) {
            item.setStatus(QuoteItem.LineStatus.A_COMPLETER);
            item.addWarning("Impossible d'estimer le prix - article source manquant");
            return;
        }

        // Détermination de la gamme
        QuoteItem.PriceRange priceRange = determinePriceRange(source);
        item.setPriceRange(priceRange);

        // Récupération du prix de base depuis le catalogue (base de données)
        double[] prices = getPricesForCategory(source.getCategory());

        double basePrice;
        double confidence;

        switch (priceRange) {
            case ECONOMIQUE:
                basePrice = prices[0];
                confidence = 0.7;
                break;
            case PREMIUM:
            case LUXE:
                basePrice = prices[2];
                confidence = 0.6;
                break;
            default:
                basePrice = prices[1];
                confidence = 0.75;
        }

        // Ajustements selon les caractéristiques
        basePrice = adjustPriceForFeatures(basePrice, source);

        // Définition du prix
        item.setUnitPriceHT(basePrice);
        item.setPriceConfidence(confidence);

        // Estimation du délai de livraison
        item.setDeliveryDays(STANDARD_DELIVERY_DAYS);

        // Statut
        if (confidence >= 0.6) {
            item.setStatus(QuoteItem.LineStatus.A_VALIDER);
        } else {
            item.setStatus(QuoteItem.LineStatus.A_COMPLETER);
            item.addWarning("Prix estimé avec faible confiance - à vérifier");
        }
    }

    /**
     * Détermine la gamme de prix selon les caractéristiques.
     */
    private QuoteItem.PriceRange determinePriceRange(AnalyzedItem item) {
        String searchText = buildSearchText(item);

        // Recherche de mots-clés premium
        for (String keyword : PREMIUM_KEYWORDS) {
            if (searchText.contains(keyword)) {
                return QuoteItem.PriceRange.PREMIUM;
            }
        }

        // Recherche de mots-clés économiques
        for (String keyword : ECONOMIQUE_KEYWORDS) {
            if (searchText.contains(keyword)) {
                return QuoteItem.PriceRange.ECONOMIQUE;
            }
        }

        return QuoteItem.PriceRange.STANDARD;
    }

    /**
     * Construit le texte de recherche pour l'analyse.
     */
    private String buildSearchText(AnalyzedItem item) {
        StringBuilder sb = new StringBuilder();

        if (item.getProduct() != null) {
            sb.append(item.getProduct().toLowerCase()).append(" ");
        }
        if (item.getMaterial() != null) {
            sb.append(item.getMaterial().toLowerCase()).append(" ");
        }
        if (item.getColor() != null) {
            sb.append(item.getColor().toLowerCase()).append(" ");
        }
        if (item.hasCharacteristics()) {
            for (String c : item.getCharacteristics()) {
                sb.append(c.toLowerCase()).append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Ajuste le prix selon les caractéristiques spéciales.
     */
    private double adjustPriceForFeatures(double basePrice, AnalyzedItem item) {
        double adjustedPrice = basePrice;

        String searchText = buildSearchText(item);

        // Haute résistance / classe supérieure : +30 %
        if (searchText.contains("haute résistance") || searchText.contains("hr")
                || searchText.contains("52,5")) {
            adjustedPrice *= 1.30;
        }

        // Produit traité (autoclave, hydrofuge…) : +20 %
        if (searchText.contains("traité") || searchText.contains("hydrofuge")) {
            adjustedPrice *= 1.20;
        }

        // Certifié / norme NF : +10 %
        if (searchText.contains("certifié") || searchText.contains("nf")) {
            adjustedPrice *= 1.10;
        }

        // Produit allégé / recyclé : -10 %
        if (searchText.contains("allégé") || searchText.contains("recyclé")) {
            adjustedPrice *= 0.90;
        }

        return Math.round(adjustedPrice * 100.0) / 100.0;
    }

    // === VÉRIFICATION DU BUDGET ===

    /**
     * Vérifie le budget et génère les alertes appropriées.
     */
    /** Seuil de dépassement de budget pour rejet automatique (%) */
    private static final double BUDGET_OVERRUN_REJECTION_THRESHOLD = 30.0;

    private void checkBudget(DraftQuote draft, AnalyzedInfo analysis) {
        if (!analysis.hasBudget()) {
            draft.addRecommendation("Budget client non spécifié - Proposer plusieurs options de gamme");
            return;
        }

        double budget = analysis.getBudget();
        draft.setClientBudget(budget);

        // Le calcul du delta est fait automatiquement dans setClientBudget
        Double totalHT = draft.getTotalHT();

        if (totalHT != null) {
            double delta = budget - totalHT;
            double deltaPercent = (delta / budget) * 100;
            double overrunPercent = Math.abs(deltaPercent);

            if (delta < 0) {
                // Dépassement du budget
                if (overrunPercent > BUDGET_OVERRUN_REJECTION_THRESHOLD) {
                    // Dépassement MAJEUR (> 30%) → alerte pour le commercial (pas de rejet automatique)
                    draft.addInconsistency(String.format(
                        "Budget dépassé de %.0f%% : total %.0f € pour un budget de %.0f € — à revoir avec le client",
                        overrunPercent, totalHT, budget
                    ));
                    draft.addWarning(String.format(
                        "Dépassement important : +%.2f € (%.0f%% au-dessus du budget client)",
                        Math.abs(delta), overrunPercent
                    ));
                    draft.addRequiredAction("Ajuster le devis pour respecter le budget client");
                } else {
                    // Dépassement modéré → avertissement
                    draft.addWarning(String.format(
                        "Dépassement du budget de %.2f € (%.0f%% au-dessus)",
                        Math.abs(delta), overrunPercent
                    ));
                    draft.addRequiredAction("Proposer des alternatives moins coûteuses");
                    draft.addRecommendation("Envisager la gamme économique pour certains articles");
                }
            } else if (deltaPercent < 10) {
                // Proche du budget
                draft.addRecommendation("Budget serré - Peu de marge pour les options");
            } else if (deltaPercent > 30) {
                // Large marge
                draft.addRecommendation("Marge importante - Possibilité d'upgrade vers gamme supérieure");
            }
        }
    }

    // === CONFIGURATION DE LA LIVRAISON ===

    /**
     * Configure les paramètres de livraison.
     */
    private void configureDelivery(DraftQuote draft, AnalyzedInfo analysis) {
        // Date de livraison demandée
        if (analysis.hasDeliveryDate()) {
            draft.setRequestedDeliveryDate(analysis.getDeliveryDate());
        }

        // Délai selon l'urgence
        int deliveryDays = STANDARD_DELIVERY_DAYS;
        if (analysis.getUrgency() != null) {
            String urgency = analysis.getUrgency().toLowerCase();
            if (urgency.contains("urgent") || urgency.contains("asap")) {
                deliveryDays = URGENT_DELIVERY_DAYS;
                draft.addSpecialCondition("Livraison express demandée");
            }
        }
        draft.setDeliveryDays(deliveryDays);

        // Date estimée
        LocalDate estimatedDate = LocalDate.now().plusDays(deliveryDays);
        draft.setEstimatedDeliveryDate(estimatedDate);

        // Vérification de faisabilité
        if (analysis.hasDeliveryDate()) {
            LocalDate requested = analysis.getDeliveryDate();
            if (requested.isBefore(estimatedDate)) {
                long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(estimatedDate, requested);
                draft.addWarning(String.format(
                    "Date de livraison demandée (%s) difficile à respecter (écart: %d jours)",
                    requested, Math.abs(daysDiff)
                ));
                draft.addRequiredAction("Confirmer la faisabilité du délai avec le fournisseur");
            }
        }

        // Frais de livraison
        Double totalHT = draft.getTotalHT();
        if (totalHT != null && totalHT >= FREE_DELIVERY_THRESHOLD) {
            draft.setDeliveryIncluded(true);
            draft.setDeliveryFees(0.0);
            draft.addSpecialCondition("Livraison offerte (montant > " +
                String.format("%,.0f €", FREE_DELIVERY_THRESHOLD) + ")");
        } else {
            draft.setDeliveryFees(DEFAULT_DELIVERY_FEES);
        }

        // Installation
        if (draft.getTotalQuantity() >= 20) {
            draft.addRecommendation("Volume important - Inclure l'installation dans le devis");
        }
    }

    // === GÉNÉRATION DES RECOMMANDATIONS ===

    /**
     * Génère les recommandations pour le commercial.
     */
    private void generateRecommendations(DraftQuote draft, AnalyzedInfo analysis) {
        // Recommandations selon le volume
        int totalQty = draft.getTotalQuantity();
        if (totalQty >= 50) {
            draft.addRecommendation("Commande volumineuse - Négocier une remise fournisseur (10-15%)");
        } else if (totalQty >= 20) {
            draft.addRecommendation("Volume moyen - Remise possible (5-8%)");
        }

        // Recommandations selon les catégories
        Map<AnalyzedItem.Category, List<QuoteItem>> sections = draft.getSections();

        if (sections.containsKey(AnalyzedItem.Category.GROS_OEUVRE) &&
            sections.containsKey(AnalyzedItem.Category.SECOND_OEUVRE)) {
            draft.addRecommendation("Gros œuvre + Second œuvre : proposer une offre groupée chantier complet");
        }

        // Recommandations selon la confiance
        if (analysis.getConfidence() < 0.6) {
            draft.addRecommendation("Confiance analyse faible - Prévoir un échange téléphonique avec le client");
        }

        // Notes additionnelles
        if (!analysis.getNotes().isEmpty()) {
            for (String note : analysis.getNotes()) {
                draft.addSpecialCondition("Note client : " + note);
            }
        }
    }

    // === DÉTERMINATION DES ACTIONS ===

    /**
     * Détermine les actions requises avant finalisation.
     */
    private void determineRequiredActions(DraftQuote draft) {
        // Articles avec statut A_COMPLETER (A_VALIDER = déjà prêt pour validation, pas une action requise)
        List<QuoteItem> incompleteItems = draft.getItems().stream()
                .filter(item -> item.getStatus() == QuoteItem.LineStatus.A_COMPLETER)
                .collect(java.util.stream.Collectors.toList());
        if (!incompleteItems.isEmpty()) {
            draft.addRequiredAction(String.format(
                "Compléter les informations de %d article(s)",
                incompleteItems.size()
            ));
        }

        // Articles sans prix
        List<QuoteItem> itemsWithoutPrice = draft.getItemsWithoutPrice();
        if (!itemsWithoutPrice.isEmpty()) {
            draft.addRequiredAction(String.format(
                "Définir le prix de %d article(s)",
                itemsWithoutPrice.size()
            ));
        }

        // Incohérences
        if (draft.hasInconsistencies()) {
            draft.addRequiredAction("Résoudre les incohérences détectées dans la demande");
        }

        // Budget non respecté
        if (draft.isBudgetRespected() != null && !draft.isBudgetRespected()) {
            draft.addRequiredAction("Ajuster le devis pour respecter le budget client");
        }
    }

    // === ÉVALUATION DU STATUT ===

    /**
     * Évalue et définit le statut final du brouillon.
     */
    private void evaluateStatus(DraftQuote draft) {
        // Rejeté si trop d'incohérences
        if (draft.getInconsistencies().size() >= 3) {
            draft.setStatus(DraftQuote.DraftStatus.REJETE);
            draft.addWarning("Trop d'incohérences - Demande de clarification nécessaire");
            return;
        }

        // À compléter si des actions sont requises
        if (!draft.getRequiredActions().isEmpty()) {
            draft.setStatus(DraftQuote.DraftStatus.A_COMPLETER);
            return;
        }

        // Prêt si tous les articles sont complets
        boolean allComplete = draft.getItems().stream()
            .allMatch(item -> item.getStatus() == QuoteItem.LineStatus.COMPLETE ||
                              item.getStatus() == QuoteItem.LineStatus.A_VALIDER);

        if (allComplete && draft.hasItems()) {
            draft.setStatus(DraftQuote.DraftStatus.A_VALIDER);
        } else {
            draft.setStatus(DraftQuote.DraftStatus.BROUILLON);
        }
    }

    // === CALCUL DE LA CONFIANCE ===

    /**
     * Calcule le score de confiance global du brouillon.
     */
    private void calculateConfidence(DraftQuote draft, AnalyzedInfo analysis) {
        double confidence = 0.0;

        // Base : confiance de l'analyse source (30%)
        confidence += analysis.getConfidence() * 0.30;

        // Articles avec prix (25%)
        if (draft.hasItems()) {
            long itemsWithPrice = draft.getItems().stream()
                .filter(QuoteItem::hasPrice)
                .count();
            double priceRatio = (double) itemsWithPrice / draft.getItemCount();
            confidence += priceRatio * 0.25;
        }

        // Complétude des articles (20%)
        if (draft.hasItems()) {
            long completeItems = draft.getItems().stream()
                .filter(i -> i.getStatus() == QuoteItem.LineStatus.COMPLETE ||
                            i.getStatus() == QuoteItem.LineStatus.A_VALIDER)
                .count();
            double completeRatio = (double) completeItems / draft.getItemCount();
            confidence += completeRatio * 0.20;
        }

        // Budget vérifié (10%)
        if (draft.getClientBudget() != null && draft.isBudgetRespected() != null) {
            confidence += draft.isBudgetRespected() ? 0.10 : 0.05;
        }

        // Absence d'incohérences (10%)
        if (!draft.hasInconsistencies()) {
            confidence += 0.10;
        } else if (draft.getInconsistencies().size() <= 1) {
            confidence += 0.05;
        }

        // Absence d'actions requises (5%)
        if (!draft.hasRequiredActions()) {
            confidence += 0.05;
        }

        draft.setConfidence(Math.min(1.0, confidence));
    }

    // === FINALISATION DES STATISTIQUES ===

    /**
     * Finalise les statistiques de génération.
     */
    private void finalizeStats(DraftQuote draft, long startTime) {
        DraftQuote.DraftStats stats = new DraftQuote.DraftStats();

        stats.totalItems = draft.getItemCount();
        stats.completeItems = (int) draft.getItems().stream()
            .filter(i -> i.getStatus() == QuoteItem.LineStatus.COMPLETE)
            .count();
        stats.incompleteItems = stats.totalItems - stats.completeItems;
        stats.itemsWithPrice = (int) draft.getItems().stream()
            .filter(QuoteItem::hasPrice)
            .count();
        stats.itemsWithoutPrice = stats.totalItems - stats.itemsWithPrice;
        stats.sectionsCount = draft.getSections().size();
        stats.warningsCount = draft.getWarnings().size() +
            draft.getItems().stream().mapToInt(i -> i.getWarnings().size()).sum();
        stats.actionsRequired = draft.getRequiredActions().size();
        stats.generationTimeMs = System.currentTimeMillis() - startTime;

        draft.setStats(stats);
    }

    // === MÉTHODE DE RAPPORT ===

    /**
     * Génère un rapport détaillé du brouillon.
     *
     * @param draft Brouillon à analyser
     * @return Rapport textuel
     */
    public String getDraftReport(DraftQuote draft) {
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              RAPPORT DE GÉNÉRATION - ÉTAPE 5                     ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");

        // En-tête
        sb.append("DEVIS: ").append(draft.getQuoteNumber()).append("\n");
        sb.append("Créé le: ").append(draft.getFormattedCreatedAt()).append("\n");
        sb.append("Valide jusqu'au: ").append(draft.getFormattedValidUntil()).append("\n");
        sb.append("Statut: ").append(draft.getStatus().getLabel()).append("\n");
        sb.append("Priorité: ").append(draft.getPriority().getLabel()).append("\n");
        sb.append("\n");

        // Objet
        if (draft.getSubject() != null) {
            sb.append("Objet: ").append(draft.getSubject()).append("\n\n");
        }

        // Statistiques
        DraftQuote.DraftStats stats = draft.getStats();
        if (stats != null) {
            sb.append("STATISTIQUES:\n");
            sb.append(String.format("  Articles: %d (complets: %d, à compléter: %d)\n",
                stats.totalItems, stats.completeItems, stats.incompleteItems));
            sb.append(String.format("  Prix définis: %d/%d\n",
                stats.itemsWithPrice, stats.totalItems));
            sb.append(String.format("  Sections: %d catégories\n", stats.sectionsCount));
            sb.append(String.format("  Temps de génération: %d ms\n", stats.generationTimeMs));
            sb.append("\n");
        }

        return sb.toString();
    }
}
