package com.projetdevis.service;

import com.projetdevis.model.AnalyzedInfo;
import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service d'analyse des informations extraites.
 * Pipeline BMAD - Étape 4 : Analyse
 *
 * Ce service effectue :
 * 1. Filtrage des faux positifs
 * 2. Validation des articles
 * 3. Normalisation des données
 * 4. Regroupement des articles similaires
 * 5. Enrichissement (catégories, caractéristiques)
 * 6. Détection des incohérences
 *
 * @author BMAD Pipeline - Étape 4
 * @version 1.0
 */
public class AnalysisService {

    // === LISTES DE FILTRAGE ===

    /** Phrases décoratives à filtrer (faux positifs) */
    private static final List<String> DECORATIVE_PHRASES = Arrays.asList(
        "nous aurions besoin de",
        "nous avons besoin de",
        "nous recherchons",
        "nous souhaitons",
        "je souhaite",
        "je voudrais",
        "voici notre demande",
        "voici notre commande",
        "pour la salle de",
        "pour le bureau",
        "pour l'open space",
        "pour l'espace",
        "salle de",
        "open space",
        "espace",
        "coloris",
        "couleur",
        "dans le cadre de",
        "souhaitons obtenir",
        "obtenir un devis",
        "contraintes importantes",
        "informations complémentaires",
        "note",
        "attention",
        "important",
        "budget",
        "date souhaitée",
        "livraison",
        "délai",
        "garantie minimum",
        "paiement",
        "installation et montage",
        "nous restons disponibles",
        "pour vous recevoir",
        "salut",
        "bonjour",
        "merci"
    );

    /** Mots-clés de produits valides */
    private static final Map<String, AnalyzedItem.Category> PRODUCT_CATEGORIES = new LinkedHashMap<>();
    static {
        // Bureaux
        PRODUCT_CATEGORIES.put("bureau", AnalyzedItem.Category.BUREAU);
        PRODUCT_CATEGORIES.put("bureaux", AnalyzedItem.Category.BUREAU);
        PRODUCT_CATEGORIES.put("poste de travail", AnalyzedItem.Category.BUREAU);
        PRODUCT_CATEGORIES.put("postes de travail", AnalyzedItem.Category.BUREAU);

        // Sièges
        PRODUCT_CATEGORIES.put("chaise", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("chaises", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("fauteuil", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("fauteuils", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("siège", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("sièges", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("tabouret", AnalyzedItem.Category.SIEGE);
        PRODUCT_CATEGORIES.put("tabourets", AnalyzedItem.Category.SIEGE);

        // Tables
        PRODUCT_CATEGORIES.put("table", AnalyzedItem.Category.TABLE);
        PRODUCT_CATEGORIES.put("tables", AnalyzedItem.Category.TABLE);
        PRODUCT_CATEGORIES.put("table de réunion", AnalyzedItem.Category.TABLE);
        PRODUCT_CATEGORIES.put("table basse", AnalyzedItem.Category.TABLE);
        PRODUCT_CATEGORIES.put("tables basses", AnalyzedItem.Category.TABLE);

        // Rangement
        PRODUCT_CATEGORIES.put("armoire", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("armoires", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("caisson", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("caissons", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("étagère", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("étagères", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("bibliothèque", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("rangement", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("tiroir", AnalyzedItem.Category.RANGEMENT);
        PRODUCT_CATEGORIES.put("tiroirs", AnalyzedItem.Category.RANGEMENT);

        // Éclairage
        PRODUCT_CATEGORIES.put("lampe", AnalyzedItem.Category.ECLAIRAGE);
        PRODUCT_CATEGORIES.put("lampes", AnalyzedItem.Category.ECLAIRAGE);
        PRODUCT_CATEGORIES.put("luminaire", AnalyzedItem.Category.ECLAIRAGE);
        PRODUCT_CATEGORIES.put("luminaires", AnalyzedItem.Category.ECLAIRAGE);

        // Espace détente
        PRODUCT_CATEGORIES.put("canapé", AnalyzedItem.Category.ESPACE_DETENTE);
        PRODUCT_CATEGORIES.put("canapés", AnalyzedItem.Category.ESPACE_DETENTE);
        PRODUCT_CATEGORIES.put("pouf", AnalyzedItem.Category.ESPACE_DETENTE);
        PRODUCT_CATEGORIES.put("poufs", AnalyzedItem.Category.ESPACE_DETENTE);
        PRODUCT_CATEGORIES.put("fauteuil lounge", AnalyzedItem.Category.ESPACE_DETENTE);

        // Cloisons
        PRODUCT_CATEGORIES.put("cloison", AnalyzedItem.Category.CLOISON);
        PRODUCT_CATEGORIES.put("cloisons", AnalyzedItem.Category.CLOISON);
        PRODUCT_CATEGORIES.put("panneau", AnalyzedItem.Category.CLOISON);
        PRODUCT_CATEGORIES.put("panneaux", AnalyzedItem.Category.CLOISON);
        PRODUCT_CATEGORIES.put("séparateur", AnalyzedItem.Category.CLOISON);
    }

    /** Synonymes de produits pour normalisation */
    private static final Map<String, String> PRODUCT_SYNONYMS = new HashMap<>();
    static {
        // Bureaux
        PRODUCT_SYNONYMS.put("bureaux opérationnels", "bureau opérationnel");
        PRODUCT_SYNONYMS.put("bureau executive", "bureau direction");
        PRODUCT_SYNONYMS.put("bureau executif", "bureau direction");
        PRODUCT_SYNONYMS.put("postes de travail complets", "poste de travail");

        // Sièges
        PRODUCT_SYNONYMS.put("chaises visiteurs", "chaise visiteur");
        PRODUCT_SYNONYMS.put("chaises de bureau", "chaise de bureau");
        PRODUCT_SYNONYMS.put("fauteuils de direction", "fauteuil direction");
        PRODUCT_SYNONYMS.put("fauteuils direction", "fauteuil direction");
        PRODUCT_SYNONYMS.put("fauteuils de conférence", "fauteuil conférence");
        PRODUCT_SYNONYMS.put("sièges ergonomiques", "siège ergonomique");

        // Rangement
        PRODUCT_SYNONYMS.put("caissons mobiles", "caisson mobile");
        PRODUCT_SYNONYMS.put("armoires de rangement", "armoire rangement");
        PRODUCT_SYNONYMS.put("armoire basse", "armoire basse");
        PRODUCT_SYNONYMS.put("étagères murales", "étagère murale");

        // Tables
        PRODUCT_SYNONYMS.put("tables de réunion", "table de réunion");
        PRODUCT_SYNONYMS.put("tables basses rondes", "table basse ronde");

        // Détente
        PRODUCT_SYNONYMS.put("poufs modulables", "pouf modulable");
    }

    /** Couleurs normalisées */
    private static final Map<String, String> COLOR_NORMALIZATION = new HashMap<>();
    static {
        COLOR_NORMALIZATION.put("noires", "noir");
        COLOR_NORMALIZATION.put("noirs", "noir");
        COLOR_NORMALIZATION.put("blanches", "blanc");
        COLOR_NORMALIZATION.put("blancs", "blanc");
        COLOR_NORMALIZATION.put("grises", "gris");
        COLOR_NORMALIZATION.put("bleues", "bleu");
        COLOR_NORMALIZATION.put("bleus", "bleu");
        COLOR_NORMALIZATION.put("rouges", "rouge");
        COLOR_NORMALIZATION.put("vertes", "vert");
        COLOR_NORMALIZATION.put("verts", "vert");
        COLOR_NORMALIZATION.put("chêne clair", "chêne clair");
        COLOR_NORMALIZATION.put("chêne foncé", "chêne foncé");
    }

    // === MÉTHODE PRINCIPALE ===

    /**
     * Analyse les informations extraites et produit un résultat validé.
     *
     * @param extracted Informations extraites (étape 3)
     * @return Informations analysées et validées
     */
    public AnalyzedInfo analyze(ExtractedInfo extracted) {
        long startTime = System.currentTimeMillis();

        AnalyzedInfo analyzed = new AnalyzedInfo(extracted);
        AnalyzedInfo.AnalysisStats stats = analyzed.getStats();

        if (extracted == null) {
            analyzed.addInconsistency("Données d'entrée nulles");
            analyzed.setConfidence(0.0);
            return analyzed;
        }

        // Statistiques initiales
        stats.totalItemsExtracted = extracted.getItemCount();

        // 1. Filtrage des faux positifs
        List<ItemRequest> filteredItems = filterFalsePositives(extracted.getItems());
        stats.itemsFiltered = stats.totalItemsExtracted - filteredItems.size();

        // 2. Conversion et validation des articles
        List<AnalyzedItem> validatedItems = new ArrayList<>();
        for (ItemRequest item : filteredItems) {
            AnalyzedItem analyzed_item = validateAndConvert(item);
            if (analyzed_item != null) {
                validatedItems.add(analyzed_item);
            }
        }

        // 3. Normalisation des articles
        for (AnalyzedItem item : validatedItems) {
            normalizeItem(item);
        }

        // 4. Enrichissement (catégories, caractéristiques)
        for (AnalyzedItem item : validatedItems) {
            enrichItem(item);
        }

        // 5. Regroupement des articles similaires
        List<AnalyzedItem> mergedItems = mergeItems(validatedItems);
        stats.itemsMerged = validatedItems.size() - mergedItems.size();
        stats.itemsValidated = mergedItems.size();

        // Ajouter les articles à AnalyzedInfo
        for (AnalyzedItem item : mergedItems) {
            analyzed.addItem(item);
        }

        // 6. Traitement du budget
        processBudget(extracted, analyzed);

        // 7. Traitement de la date de livraison
        processDeliveryDate(extracted, analyzed);

        // 8. Traitement de l'urgence
        analyzed.setUrgency(extracted.getUrgency());

        // 9. Traitement des notes
        processNotes(extracted, analyzed);

        // 10. Détection des incohérences
        detectInconsistencies(analyzed);
        stats.inconsistenciesFound = analyzed.getInconsistencies().size();

        // 11. Calcul de la confiance globale
        calculateConfidence(analyzed);

        // Temps d'analyse
        stats.analysisTimeMs = System.currentTimeMillis() - startTime;

        return analyzed;
    }

    // === 1. FILTRAGE DES FAUX POSITIFS ===

    /**
     * Filtre les faux positifs de la liste d'articles.
     *
     * @param items Liste d'articles extraits
     * @return Liste filtrée
     */
    private List<ItemRequest> filterFalsePositives(List<ItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        return items.stream()
            .filter(this::isValidProduct)
            .collect(Collectors.toList());
    }

    /**
     * Vérifie si un article est un produit valide (pas un faux positif).
     *
     * @param item Article à vérifier
     * @return true si c'est un produit valide
     */
    private boolean isValidProduct(ItemRequest item) {
        if (item == null || item.getProduct() == null) {
            return false;
        }

        String product = item.getProduct().toLowerCase().trim();

        // Vérifier si c'est une phrase décorative
        for (String phrase : DECORATIVE_PHRASES) {
            if (product.equals(phrase) || product.startsWith(phrase)) {
                return false;
            }
        }

        // Vérifier si le produit contient un mot-clé valide
        for (String keyword : PRODUCT_CATEGORIES.keySet()) {
            if (product.contains(keyword)) {
                return true;
            }
        }

        // Produit trop court (moins de 4 caractères)
        if (product.length() < 4) {
            return false;
        }

        // Produit sans quantité et sans mot-clé connu = suspect
        if (item.getQuantity() == null) {
            return false;
        }

        return true;
    }

    // === 2. VALIDATION ET CONVERSION ===

    /**
     * Valide et convertit un ItemRequest en AnalyzedItem.
     *
     * @param item Article source
     * @return Article analysé ou null si invalide
     */
    private AnalyzedItem validateAndConvert(ItemRequest item) {
        if (item == null) {
            return null;
        }

        AnalyzedItem analyzed = new AnalyzedItem(item);

        // Validation du produit
        if (analyzed.getProduct() == null || analyzed.getProduct().isBlank()) {
            return null;
        }

        // Validation de la quantité
        if (analyzed.getQuantity() == null) {
            analyzed.addWarning("Quantité non spécifiée");
            analyzed.setStatus(AnalyzedItem.ValidationStatus.WARNING);
        } else if (analyzed.getQuantity() <= 0) {
            analyzed.addWarning("Quantité invalide: " + analyzed.getQuantity());
            analyzed.setStatus(AnalyzedItem.ValidationStatus.WARNING);
            analyzed.setQuantity(1); // Correction par défaut
        } else if (analyzed.getQuantity() > 1000) {
            analyzed.addWarning("Quantité exceptionnellement élevée: " + analyzed.getQuantity());
        }

        // Validation des dimensions
        if (analyzed.getDimensions() != null) {
            if (!isValidDimensions(analyzed.getDimensions())) {
                analyzed.addWarning("Format de dimensions suspect: " + analyzed.getDimensions());
            }
        }

        return analyzed;
    }

    /**
     * Vérifie si les dimensions sont dans un format valide.
     *
     * @param dimensions Chaîne de dimensions
     * @return true si le format est valide
     */
    private boolean isValidDimensions(String dimensions) {
        if (dimensions == null || dimensions.isBlank()) {
            return false;
        }

        // Pattern: 160x80cm, 160x80x75, 2m x 1m, etc.
        Pattern pattern = Pattern.compile(
            "\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?\\s*[x×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?",
            Pattern.CASE_INSENSITIVE
        );

        return pattern.matcher(dimensions).find();
    }

    // === 3. NORMALISATION ===

    /**
     * Normalise les données d'un article.
     *
     * @param item Article à normaliser
     */
    private void normalizeItem(AnalyzedItem item) {
        if (item == null) return;

        // Normaliser le nom du produit
        item.setProduct(normalizeProductName(item.getProduct()));

        // Normaliser la couleur
        if (item.getColor() != null) {
            item.setColor(normalizeColor(item.getColor()));
        }

        // Normaliser le matériau
        if (item.getMaterial() != null) {
            item.setMaterial(normalizeMaterial(item.getMaterial()));
        }

        // Normaliser les dimensions
        if (item.getDimensions() != null) {
            item.setDimensions(normalizeDimensions(item.getDimensions()));
        }
    }

    /**
     * Normalise un nom de produit.
     *
     * @param product Nom brut
     * @return Nom normalisé
     */
    private String normalizeProductName(String product) {
        if (product == null) return null;

        String normalized = product.toLowerCase().trim();

        // Appliquer les synonymes
        for (Map.Entry<String, String> entry : PRODUCT_SYNONYMS.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                normalized = normalized.replace(entry.getKey(), entry.getValue());
            }
        }

        // Supprimer les articles et prépositions en début
        normalized = normalized.replaceFirst("^(les?|la|des?|un|une|du)\\s+", "");

        // Mettre en forme (première lettre majuscule)
        if (!normalized.isEmpty()) {
            normalized = normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Normalise une couleur.
     *
     * @param color Couleur brute
     * @return Couleur normalisée
     */
    private String normalizeColor(String color) {
        if (color == null) return null;

        String normalized = color.toLowerCase().trim();

        // Appliquer les normalisations
        if (COLOR_NORMALIZATION.containsKey(normalized)) {
            return COLOR_NORMALIZATION.get(normalized);
        }

        return normalized;
    }

    /**
     * Normalise un matériau.
     *
     * @param material Matériau brut
     * @return Matériau normalisé
     */
    private String normalizeMaterial(String material) {
        if (material == null) return null;

        return material.toLowerCase().trim();
    }

    /**
     * Normalise les dimensions.
     *
     * @param dimensions Dimensions brutes
     * @return Dimensions normalisées
     */
    private String normalizeDimensions(String dimensions) {
        if (dimensions == null) return null;

        // Uniformiser le séparateur 'x'
        String normalized = dimensions.replaceAll("[×X]", "x");

        // Supprimer les espaces superflus
        normalized = normalized.replaceAll("\\s*x\\s*", "x");

        return normalized;
    }

    // === 4. ENRICHISSEMENT ===

    /**
     * Enrichit un article avec catégorie et caractéristiques.
     *
     * @param item Article à enrichir
     */
    private void enrichItem(AnalyzedItem item) {
        if (item == null) return;

        // Déterminer la catégorie
        item.setCategory(determineCategory(item));

        // Ajouter des caractéristiques déduites
        deduceCharacteristics(item);

        // Calculer la confiance de l'article
        calculateItemConfidence(item);
    }

    /**
     * Détermine la catégorie d'un article.
     *
     * @param item Article
     * @return Catégorie
     */
    private AnalyzedItem.Category determineCategory(AnalyzedItem item) {
        if (item == null || item.getProduct() == null) {
            return AnalyzedItem.Category.AUTRE;
        }

        String product = item.getProduct().toLowerCase();

        // Chercher par mot-clé (ordre de priorité)
        for (Map.Entry<String, AnalyzedItem.Category> entry : PRODUCT_CATEGORIES.entrySet()) {
            if (product.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return AnalyzedItem.Category.AUTRE;
    }

    /**
     * Déduit des caractéristiques supplémentaires.
     *
     * @param item Article à enrichir
     */
    private void deduceCharacteristics(AnalyzedItem item) {
        if (item == null) return;

        String product = item.getProduct() != null ? item.getProduct().toLowerCase() : "";
        String material = item.getMaterial() != null ? item.getMaterial().toLowerCase() : "";

        // Ergonomique
        if (product.contains("ergonomique") || product.contains("ergonomic")) {
            item.addCharacteristic("ergonomique");
        }

        // Réglable en hauteur
        if (product.contains("assis-debout") || product.contains("réglable")) {
            item.addCharacteristic("réglable en hauteur");
        }

        // Direction / Conférence
        if (product.contains("direction") || product.contains("executive")) {
            item.addCharacteristic("gamme direction");
        }
        if (product.contains("conférence") || product.contains("réunion")) {
            item.addCharacteristic("gamme réunion");
        }

        // Matériaux spécifiques
        if (material.contains("cuir") || product.contains("cuir")) {
            item.addCharacteristic("cuir");
        }
        if (material.contains("mesh") || product.contains("mesh")) {
            item.addCharacteristic("dossier mesh");
        }

        // Avec roulettes (pour sièges)
        if (item.getCategory() == AnalyzedItem.Category.SIEGE && product.contains("mobile")) {
            item.addCharacteristic("sur roulettes");
        }

        // Empilable
        if (product.contains("empilable")) {
            item.addCharacteristic("empilable");
        }

        // Nombre de personnes (pour tables)
        Pattern persPattern = Pattern.compile("(\\d+)\\s*personnes?");
        Matcher persMatcher = persPattern.matcher(product);
        if (persMatcher.find()) {
            item.addCharacteristic(persMatcher.group(1) + " personnes");
        }

        // Nombre de tiroirs (pour caissons)
        Pattern tiroirPattern = Pattern.compile("(\\d+)\\s*tiroirs?");
        Matcher tiroirMatcher = tiroirPattern.matcher(product);
        if (tiroirMatcher.find()) {
            item.addCharacteristic(tiroirMatcher.group(1) + " tiroirs");
        }
    }

    /**
     * Calcule la confiance d'un article individuel.
     *
     * @param item Article
     */
    private void calculateItemConfidence(AnalyzedItem item) {
        if (item == null) return;

        double score = 0.0;

        // Produit défini (30%)
        if (item.getProduct() != null && !item.getProduct().isBlank()) {
            score += 0.3;
        }

        // Quantité définie (25%)
        if (item.getQuantity() != null && item.getQuantity() > 0) {
            score += 0.25;
        }

        // Catégorie identifiée (20%)
        if (item.getCategory() != AnalyzedItem.Category.AUTRE) {
            score += 0.2;
        }

        // Attributs supplémentaires (25%)
        double attrScore = 0.0;
        if (item.getColor() != null) attrScore += 0.08;
        if (item.getMaterial() != null) attrScore += 0.08;
        if (item.getDimensions() != null) attrScore += 0.09;
        score += attrScore;

        // Pénalité pour avertissements
        if (item.hasWarnings()) {
            score -= 0.1 * item.getWarnings().size();
        }

        item.setConfidence(Math.max(0.0, Math.min(1.0, score)));
    }

    // === 5. REGROUPEMENT ===

    /**
     * Fusionne les articles similaires.
     *
     * @param items Liste d'articles
     * @return Liste avec articles fusionnés
     */
    private List<AnalyzedItem> mergeItems(List<AnalyzedItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // Regrouper par clé
        Map<String, List<AnalyzedItem>> groups = new LinkedHashMap<>();
        for (AnalyzedItem item : items) {
            String key = item.getGroupingKey();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        // Fusionner chaque groupe
        List<AnalyzedItem> result = new ArrayList<>();
        for (List<AnalyzedItem> group : groups.values()) {
            if (group.size() == 1) {
                result.add(group.get(0));
            } else {
                result.add(mergeGroup(group));
            }
        }

        return result;
    }

    /**
     * Fusionne un groupe d'articles similaires.
     *
     * @param group Liste d'articles à fusionner
     * @return Article fusionné
     */
    private AnalyzedItem mergeGroup(List<AnalyzedItem> group) {
        if (group == null || group.isEmpty()) {
            return null;
        }

        // Prendre le premier comme base
        AnalyzedItem merged = group.get(0);

        if (group.size() > 1) {
            // Additionner les quantités
            int totalQuantity = 0;
            for (AnalyzedItem item : group) {
                if (item.getQuantity() != null) {
                    totalQuantity += item.getQuantity();
                }
            }

            // Prendre la quantité la plus cohérente (éviter les doublons de parsing)
            // Si un item a une quantité beaucoup plus grande, c'est probablement une erreur
            int maxSingleQty = group.stream()
                .filter(i -> i.getQuantity() != null)
                .mapToInt(AnalyzedItem::getQuantity)
                .max()
                .orElse(0);

            // Si le total est excessif par rapport au max, prendre le max
            if (totalQuantity > maxSingleQty * 2) {
                merged.setQuantity(maxSingleQty);
            } else {
                merged.setQuantity(totalQuantity);
            }

            merged.setMerged(true);
            merged.setMergedCount(group.size());

            // Fusionner les caractéristiques
            for (AnalyzedItem item : group) {
                for (String charac : item.getCharacteristics()) {
                    merged.addCharacteristic(charac);
                }
            }

            merged.addWarning("Article fusionné à partir de " + group.size() + " détections");
        }

        return merged;
    }

    // === 6. TRAITEMENT BUDGET ===

    /**
     * Traite et valide le budget.
     *
     * @param extracted Données extraites
     * @param analyzed Données analysées
     */
    private void processBudget(ExtractedInfo extracted, AnalyzedInfo analyzed) {
        analyzed.setBudgetRaw(extracted.getBudgetRaw());

        if (extracted.getBudget() != null) {
            Double budget = extracted.getBudget();

            // Validation du montant
            if (budget <= 0) {
                analyzed.addInconsistency("Budget invalide: " + budget);
            } else if (budget < 100) {
                analyzed.addWarning("Budget inhabituellement bas: " + budget + "€");
            } else if (budget > 10_000_000) {
                analyzed.addWarning("Budget inhabituellement élevé: " + budget + "€");
            }

            analyzed.setBudget(budget);
        }

        // Unité et HT/TTC
        String unit = extracted.getBudgetUnit();
        if (unit != null) {
            analyzed.setBudgetUnit("€");
            analyzed.setBudgetHT(unit.toUpperCase().contains("HT"));
        } else if (extracted.getBudgetRaw() != null) {
            // Essayer de déduire HT/TTC du texte brut
            String raw = extracted.getBudgetRaw().toUpperCase();
            analyzed.setBudgetHT(!raw.contains("TTC"));
            analyzed.setBudgetUnit("€");

            if (!raw.contains("HT") && !raw.contains("TTC")) {
                analyzed.addWarning("Budget sans précision HT/TTC (HT supposé)");
            }
        }
    }

    // === 7. TRAITEMENT DATE ===

    /**
     * Traite et valide la date de livraison.
     *
     * @param extracted Données extraites
     * @param analyzed Données analysées
     */
    private void processDeliveryDate(ExtractedInfo extracted, AnalyzedInfo analyzed) {
        analyzed.setDeliveryDateRaw(extracted.getDeliveryDateRaw());

        if (extracted.getDeliveryDate() != null) {
            LocalDate date = extracted.getDeliveryDate();
            LocalDate today = LocalDate.now();

            // Validation de la date
            if (date.isBefore(today)) {
                analyzed.addInconsistency("Date de livraison dans le passé: " + date);
                // Ne pas garder une date passée
            } else if (date.isBefore(today.plusDays(3))) {
                analyzed.addWarning("Délai de livraison très court: " + date);
                analyzed.setDeliveryDate(date);
            } else if (date.isAfter(today.plusYears(2))) {
                analyzed.addWarning("Date de livraison très éloignée: " + date);
                analyzed.setDeliveryDate(date);
            } else {
                analyzed.setDeliveryDate(date);
            }
        }
    }

    // === 8. TRAITEMENT NOTES ===

    /**
     * Traite et valide les notes additionnelles.
     *
     * @param extracted Données extraites
     * @param analyzed Données analysées
     */
    private void processNotes(ExtractedInfo extracted, AnalyzedInfo analyzed) {
        if (extracted.getAdditionalNotes() == null) return;

        for (String note : extracted.getAdditionalNotes()) {
            // Filtrer les notes mal extraites
            if (note.length() < 5 || note.length() > 200) {
                continue;
            }

            // Nettoyer la note
            String cleanedNote = note.trim();
            if (cleanedNote.startsWith("s importantes")) {
                continue; // Faux positif de "contraintes importantes"
            }

            analyzed.addNote(cleanedNote);
        }
    }

    // === 9. DÉTECTION INCOHÉRENCES ===

    /** Mots-clés indiquant des produits premium/haut de gamme */
    private static final List<String> PREMIUM_KEYWORDS = Arrays.asList(
        "direction", "haut de gamme", "premium", "luxe", "design",
        "cuir", "bois massif", "executive", "prestige"
    );

    /**
     * Détecte les incohérences dans les données analysées.
     * Les incohérences MAJEURES bloquent le pipeline.
     *
     * @param analyzed Données à vérifier
     */
    private void detectInconsistencies(AnalyzedInfo analyzed) {
        // ══════════════════════════════════════════════════════════════
        // INCOHÉRENCES MAJEURES (bloquantes)
        // ══════════════════════════════════════════════════════════════

        // 1. Aucun article valide
        if (!analyzed.hasItems()) {
            analyzed.addMajorInconsistency("Aucun article valide détecté");
        }

        // 2. Articles sans quantité exploitable (quantités floues)
        if (analyzed.hasItems()) {
            long itemsWithoutQty = analyzed.getItems().stream()
                .filter(i -> i.getQuantity() == null || i.getQuantity() <= 0)
                .count();
            long totalItems = analyzed.getItems().size();

            // Si plus de 50% des articles n'ont pas de quantité → MAJEURE
            if (totalItems > 0 && (double) itemsWithoutQty / totalItems > 0.5) {
                analyzed.addMajorInconsistency(String.format(
                    "Quantités non exploitables : %d/%d articles sans quantité valide",
                    itemsWithoutQty, totalItems
                ));
            } else if (itemsWithoutQty > 0) {
                analyzed.addWarning(itemsWithoutQty + " article(s) sans quantité spécifiée");
            }
        }

        // 3. Budget irréaliste par rapport à la quantité
        if (analyzed.hasBudget() && analyzed.hasItems()) {
            int totalQty = analyzed.getTotalQuantity();
            double budget = analyzed.getBudget();
            double pricePerUnit = budget / Math.max(1, totalQty);

            // Vérifier si des articles sont premium/haut de gamme
            boolean hasPremiumItems = analyzed.getItems().stream()
                .anyMatch(item -> {
                    String product = item.getProduct() != null ? item.getProduct().toLowerCase() : "";
                    String material = item.getMaterial() != null ? item.getMaterial().toLowerCase() : "";
                    return PREMIUM_KEYWORDS.stream()
                        .anyMatch(kw -> product.contains(kw) || material.contains(kw));
                });

            // Prix unitaire < 20€ pour du mobilier = irréaliste → MAJEURE
            if (pricePerUnit < 20) {
                analyzed.addMajorInconsistency(String.format(
                    "Budget irréaliste : %.2f€ par unité pour %d articles de mobilier",
                    pricePerUnit, totalQty
                ));
            }
            // Articles premium mais prix < 100€/unité → MAJEURE
            else if (pricePerUnit < 100 && hasPremiumItems) {
                analyzed.addMajorInconsistency(String.format(
                    "Contradiction : articles premium/haut de gamme avec budget de %.2f€ par unité",
                    pricePerUnit
                ));
            }
            // Prix très élevé = avertissement
            else if (pricePerUnit > 10000) {
                analyzed.addWarning("Prix unitaire moyen très élevé: " + String.format("%.2f€", pricePerUnit));
            }
        }

        // 4. Urgence contradictoire avec date lointaine (> 6 mois)
        if (analyzed.hasDeliveryDate() && analyzed.getDeliveryDate() != null) {
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), analyzed.getDeliveryDate());
            String urgency = analyzed.getUrgency();

            if (daysUntil > 180 && ("urgent".equals(urgency) || "très urgent".equals(urgency))) {
                analyzed.addMajorInconsistency(String.format(
                    "Contradiction : urgence '%s' déclarée mais livraison dans %d jours (%.1f mois)",
                    urgency, daysUntil, daysUntil / 30.0
                ));
            }
            // Date très proche mais urgence normale = avertissement
            else if (daysUntil <= 7 && "normal".equals(urgency)) {
                analyzed.addWarning("Délai court (" + daysUntil + " jours) mais urgence marquée 'normal'");
            }
        }

        // ══════════════════════════════════════════════════════════════
        // INCOHÉRENCES MINEURES (avertissements)
        // ══════════════════════════════════════════════════════════════

        // Articles en catégorie "Autre"
        if (analyzed.hasItems()) {
            long uncategorized = analyzed.getItems().stream()
                .filter(i -> i.getCategory() == AnalyzedItem.Category.AUTRE)
                .count();
            if (uncategorized > 0) {
                analyzed.addWarning(uncategorized + " article(s) non catégorisé(s)");
            }
        }

        // Budget sans articles
        if (analyzed.hasBudget() && !analyzed.hasItems()) {
            analyzed.addInconsistency("Budget spécifié mais aucun article détecté");
        }
    }

    // === 10. CALCUL CONFIANCE ===

    /**
     * Calcule la confiance globale de l'analyse.
     *
     * @param analyzed Données analysées
     */
    private void calculateConfidence(AnalyzedInfo analyzed) {
        double score = 0.0;

        // Articles valides (40%)
        if (analyzed.hasItems()) {
            int validItems = analyzed.getValidItems().size();
            int totalItems = analyzed.getItemCount();
            double itemScore = 0.4 * ((double) validItems / Math.max(1, totalItems));
            score += itemScore;
        }

        // Confiance moyenne des articles (20%)
        if (analyzed.hasItems()) {
            double avgConfidence = analyzed.getItems().stream()
                .mapToDouble(AnalyzedItem::getConfidence)
                .average()
                .orElse(0.0);
            score += 0.2 * avgConfidence;
        }

        // Budget détecté (15%)
        if (analyzed.hasBudget()) {
            score += 0.15;
        }

        // Date détectée (15%)
        if (analyzed.hasDeliveryDate()) {
            score += 0.15;
        }

        // Cohérence (10%)
        if (analyzed.hasItems() && (analyzed.hasBudget() || analyzed.hasDeliveryDate())) {
            score += 0.1;
        }

        // Pénalités
        int incCount = analyzed.getInconsistencies().size();
        if (incCount > 0) {
            score -= 0.1 * incCount;
        }

        analyzed.setConfidence(Math.max(0.0, Math.min(1.0, score)));
    }

    // === MÉTHODE UTILITAIRE ===

    /**
     * Génère un rapport d'analyse détaillé.
     *
     * @param analyzed Résultat de l'analyse
     * @return Rapport textuel
     */
    public String getAnalysisReport(AnalyzedInfo analyzed) {
        if (analyzed == null) {
            return "Analyse: null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== RAPPORT D'ANALYSE (Étape 4) ===\n\n");

        // Statistiques
        AnalyzedInfo.AnalysisStats stats = analyzed.getStats();
        sb.append("PIPELINE:\n");
        sb.append(String.format("  Entrée: %d articles extraits\n", stats.totalItemsExtracted));
        sb.append(String.format("  Filtrés: %d faux positifs supprimés\n", stats.itemsFiltered));
        sb.append(String.format("  Fusionnés: %d doublons regroupés\n", stats.itemsMerged));
        sb.append(String.format("  Sortie: %d articles validés\n", stats.itemsValidated));
        sb.append(String.format("  Temps: %d ms\n", stats.analysisTimeMs));
        sb.append("\n");

        // Qualité
        sb.append("QUALITÉ:\n");
        sb.append(String.format("  Confiance: %.0f%%\n", analyzed.getConfidence() * 100));
        sb.append(String.format("  Fiable: %s\n", analyzed.isReliable() ? "OUI" : "NON"));
        sb.append(String.format("  Incohérences: %d\n", analyzed.getInconsistencies().size()));
        sb.append(String.format("  Avertissements: %d\n", analyzed.getWarnings().size()));

        return sb.toString();
    }
}
