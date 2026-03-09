package com.projetdevis.service;

import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'extraction des informations structurées depuis un email de devis.
 * Pipeline BMAD - Étape 3 : Extraction
 *
 * Ce service extrait :
 * - Les articles demandés (produits, quantités, dimensions, etc.)
 * - Le budget indicatif
 * - La date de livraison souhaitée
 * - Le niveau d'urgence
 * - Les notes additionnelles
 *
 * Et calcule un score de confiance global.
 *
 * @author BMAD Pipeline - Étape 3
 * @version 1.0
 */
public class ExtractionService {

    // === PATTERNS POUR LES ARTICLES ===

    // Pattern pour détecter les lignes d'articles avec quantité
    // Exemples : "- 50 chaises", "• 25 bureaux", "10 x tables", "15 unités de lampes"
    private static final Pattern ITEM_LINE_PATTERN = Pattern.compile(
        "(?im)^\\s*" +
        "(?:[-•*>]\\s*)?" +                           // Puce optionnelle
        "(?:(\\d+)\\s*(?:x|×|unités?\\s+de)?\\s+)?" + // Quantité optionnelle
        "([a-zàâäéèêëïîôùûüç][a-zàâäéèêëïîôùûüç\\s-]{2,50})" + // Produit (min 3 chars)
        "(?:\\s+(.+))?" +                             // Détails optionnels
        "$"
    );

    // Pattern alternatif : "des chaises (50)" ou "bureaux : 25"
    private static final Pattern ITEM_ALT_PATTERN = Pattern.compile(
        "(?im)(?:des?\\s+)?([a-zàâäéèêëïîôùûüç][a-zàâäéèêëïîôùûüç\\s-]{2,40})" +
        "\\s*[:\\(]\\s*(\\d+)\\s*\\)?"
    );

    // Pattern pour extraire les quantités dans le texte
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
        "(?i)(\\d+)\\s*(?:x|×|unités?|pièces?|postes?|lots?|sets?)?\\s*"
    );

    // === PATTERNS POUR LES DIMENSIONS ===

    private static final Pattern DIMENSION_PATTERN = Pattern.compile(
        "(?i)(\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?\\s*[x×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?)" +
        "(?:\\s*[x×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?)?"
    );

    // === PATTERNS POUR LES COULEURS ===

    private static final List<String> COLORS = Arrays.asList(
        "noir", "noire", "noirs", "noires",
        "blanc", "blanche", "blancs", "blanches",
        "gris", "grise",
        "rouge", "rouges",
        "bleu", "bleue", "bleus", "bleues",
        "vert", "verte", "verts", "vertes",
        "jaune", "jaunes",
        "orange",
        "marron",
        "beige",
        "chêne clair", "chêne foncé", "chêne naturel",
        "noyer", "wengé", "acajou", "merisier",
        "anthracite", "taupe", "bordeaux", "crème"
    );

    private static final Pattern COLOR_PATTERN = Pattern.compile(
        "(?i)\\b(" + String.join("|", COLORS) + ")\\b"
    );

    // === PATTERNS POUR LES MATÉRIAUX ===

    private static final List<String> MATERIALS = Arrays.asList(
        "bois", "métal", "acier", "aluminium", "inox",
        "verre", "verre trempé", "plastique", "pvc",
        "cuir", "simili cuir", "tissu", "velours", "mesh",
        "mélaminé", "stratifié", "laqué", "plaqué",
        "chêne", "hêtre", "pin", "noyer", "bambou"
    );

    private static final Pattern MATERIAL_PATTERN = Pattern.compile(
        "(?i)\\b(?:en\\s+)?(" + String.join("|", MATERIALS) + ")\\b"
    );

    // === PATTERNS POUR LE BUDGET ===

    private static final Pattern BUDGET_PATTERN = Pattern.compile(
        "(?i)(?:budget|enveloppe|montant|prix\\s*max(?:imum)?)" +
        "[^\\d]{0,30}" +
        "(\\d[\\d\\s.,]*)" +
        "\\s*(k?€|euros?|k\\s*euros?)?\\s*(ht|ttc)?"
    );

    private static final Pattern BUDGET_ALT_PATTERN = Pattern.compile(
        "(?i)(\\d[\\d\\s.,]*)\\s*(k?€|euros?)\\s*(ht|ttc)?\\s*(?:de\\s+budget|max(?:imum)?)"
    );

    // Pattern simple pour montant en euros
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?i)(\\d{1,3}(?:[\\s.,]?\\d{3})*(?:[.,]\\d{2})?)\\s*(k?€|euros?)\\s*(ht|ttc)?"
    );

    // === PATTERNS POUR LA DATE DE LIVRAISON ===

    private static final Pattern DELIVERY_DATE_PATTERN = Pattern.compile(
        "(?i)(?:livraison|livré|délai|avant|pour\\s+le|d'ici(?:\\s+le)?|fin)" +
        "[^\\d]{0,30}" +
        "(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}|" +         // 15/03/2024
        "\\d{1,2}\\s+(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?|" + // 15 mars 2024
        "fin\\s+(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?|" + // fin mars
        "(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?)" // mars 2024
    );

    private static final Pattern DATE_NUMERIC_PATTERN = Pattern.compile(
        "(\\d{1,2})[/.-](\\d{1,2})[/.-](\\d{2,4})"
    );

    private static final Map<String, Integer> MONTH_NAMES = new HashMap<>();
    static {
        MONTH_NAMES.put("janvier", 1);
        MONTH_NAMES.put("février", 2);
        MONTH_NAMES.put("mars", 3);
        MONTH_NAMES.put("avril", 4);
        MONTH_NAMES.put("mai", 5);
        MONTH_NAMES.put("juin", 6);
        MONTH_NAMES.put("juillet", 7);
        MONTH_NAMES.put("août", 8);
        MONTH_NAMES.put("septembre", 9);
        MONTH_NAMES.put("octobre", 10);
        MONTH_NAMES.put("novembre", 11);
        MONTH_NAMES.put("décembre", 12);
    }

    // === PATTERNS POUR L'URGENCE ===

    private static final Pattern URGENT_PATTERN = Pattern.compile(
        "(?i)(urgent|urgence|asap|au plus vite|dès que possible|rapidement|pressé|en urgence)"
    );

    private static final Pattern VERY_URGENT_PATTERN = Pattern.compile(
        "(?i)(très urgent|extrêmement urgent|urgentissime|immédiat|tout de suite|aujourd'hui)"
    );

    // === PATTERNS POUR LES NOTES ===

    private static final Pattern NOTE_PATTERNS = Pattern.compile(
        "(?im)^\\s*(?:note|nb|attention|important|contrainte|condition|remarque)\\s*[:\\-]?\\s*(.+)$"
    );

    // Types de produits connus (pour améliorer l'extraction)
    private static final List<String> PRODUCT_KEYWORDS = Arrays.asList(
        "bureau", "bureaux", "table", "tables", "chaise", "chaises",
        "fauteuil", "fauteuils", "caisson", "caissons", "armoire", "armoires",
        "étagère", "étagères", "lampe", "lampes", "écran", "écrans",
        "siège", "sièges", "poste", "postes", "rangement", "rangements",
        "canapé", "canapés", "pouf", "poufs", "tabouret", "tabourets",
        "meuble", "meubles", "mobilier", "cloison", "cloisons",
        "panneau", "panneaux", "tiroir", "tiroirs", "bibliothèque", "bibliothèques"
    );

    // === MÉTHODE PRINCIPALE ===

    /**
     * Extrait toutes les informations structurées d'un email nettoyé.
     *
     * @param cleanedEmail Email nettoyé par EmailCleanerService
     * @return ExtractedInfo contenant toutes les données extraites
     */
    public ExtractedInfo extract(String cleanedEmail) {
        ExtractedInfo info = new ExtractedInfo(cleanedEmail);

        if (cleanedEmail == null || cleanedEmail.isBlank()) {
            info.setConfidence(0.0);
            return info;
        }

        // 1. Extraction des articles
        extractItems(cleanedEmail, info);

        // 2. Extraction du budget
        extractBudget(cleanedEmail, info);

        // 3. Extraction de la date de livraison
        extractDeliveryDate(cleanedEmail, info);

        // 4. Extraction du niveau d'urgence
        extractUrgency(cleanedEmail, info);

        // 5. Extraction des notes additionnelles
        extractAdditionalNotes(cleanedEmail, info);

        // 6. Calcul de la confiance globale
        calculateConfidence(info);

        return info;
    }

    // === EXTRACTION DES ARTICLES ===

    /**
     * Extrait les articles demandés depuis le texte.
     *
     * @param text Texte source
     * @param info ExtractedInfo à remplir
     */
    private void extractItems(String text, ExtractedInfo info) {
        Set<String> processedLines = new HashSet<>();

        // Méthode 1 : Chercher les lignes avec format liste
        extractItemsFromListFormat(text, info, processedLines);

        // Méthode 2 : Chercher les patterns alternatifs
        extractItemsFromAltFormat(text, info, processedLines);

        // Méthode 3 : Chercher les produits connus dans le texte
        extractItemsFromProductKeywords(text, info, processedLines);
    }

    /**
     * Extrait les articles au format liste (- 50 chaises, • 25 bureaux).
     */
    private void extractItemsFromListFormat(String text, ExtractedInfo info, Set<String> processedLines) {
        String[] lines = text.split("\\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || processedLines.contains(trimmedLine.toLowerCase())) {
                continue;
            }

            Matcher matcher = ITEM_LINE_PATTERN.matcher(trimmedLine);
            if (matcher.find()) {
                String quantityStr = matcher.group(1);
                String product = matcher.group(2);
                String details = matcher.group(3);

                // Vérifier que le produit ressemble à un produit
                if (product != null && looksLikeProduct(product)) {
                    ItemRequest item = new ItemRequest();
                    item.setProduct(normalizeProduct(product));
                    item.setRawLine(trimmedLine);

                    // Quantité
                    if (quantityStr != null) {
                        try {
                            item.setQuantity(Integer.parseInt(quantityStr));
                        } catch (NumberFormatException ignored) {}
                    }

                    // Extraire les détails du produit
                    enrichItemDetails(item, trimmedLine);

                    if (item.isValid()) {
                        info.addItem(item);
                        processedLines.add(trimmedLine.toLowerCase());
                    }
                }
            }
        }
    }

    /**
     * Extrait les articles au format alternatif (des bureaux (25), chaises : 30).
     */
    private void extractItemsFromAltFormat(String text, ExtractedInfo info, Set<String> processedLines) {
        Matcher matcher = ITEM_ALT_PATTERN.matcher(text);

        while (matcher.find()) {
            String product = matcher.group(1);
            String quantityStr = matcher.group(2);

            if (product != null && looksLikeProduct(product)) {
                String key = product.toLowerCase().trim();
                if (!processedLines.contains(key)) {
                    ItemRequest item = new ItemRequest();
                    item.setProduct(normalizeProduct(product));

                    try {
                        item.setQuantity(Integer.parseInt(quantityStr));
                    } catch (NumberFormatException ignored) {}

                    // Enrichir avec le contexte environnant
                    int start = Math.max(0, matcher.start() - 50);
                    int end = Math.min(text.length(), matcher.end() + 50);
                    String context = text.substring(start, end);
                    enrichItemDetails(item, context);

                    if (item.isValid()) {
                        info.addItem(item);
                        processedLines.add(key);
                    }
                }
            }
        }
    }

    /**
     * Extrait les articles en cherchant les mots-clés de produits connus.
     */
    private void extractItemsFromProductKeywords(String text, ExtractedInfo info, Set<String> processedLines) {
        String lowerText = text.toLowerCase();

        for (String keyword : PRODUCT_KEYWORDS) {
            if (lowerText.contains(keyword) && !processedLines.contains(keyword)) {
                // Chercher le contexte autour du mot-clé
                int index = lowerText.indexOf(keyword);
                int lineStart = lowerText.lastIndexOf('\n', index) + 1;
                int lineEnd = lowerText.indexOf('\n', index);
                if (lineEnd == -1) lineEnd = text.length();

                String line = text.substring(lineStart, lineEnd).trim();

                // Essayer d'extraire la quantité
                Matcher qtyMatcher = QUANTITY_PATTERN.matcher(line);
                Integer quantity = null;
                if (qtyMatcher.find()) {
                    try {
                        quantity = Integer.parseInt(qtyMatcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }

                // Éviter les doublons
                boolean alreadyAdded = info.getItems().stream()
                    .anyMatch(i -> i.getProduct() != null &&
                                   i.getProduct().toLowerCase().contains(keyword));

                if (!alreadyAdded && quantity != null) {
                    ItemRequest item = new ItemRequest();
                    item.setProduct(normalizeProduct(keyword));
                    item.setQuantity(quantity);
                    item.setRawLine(line);

                    enrichItemDetails(item, line);

                    if (item.isValid()) {
                        info.addItem(item);
                        processedLines.add(keyword);
                    }
                }
            }
        }
    }

    /**
     * Enrichit un article avec les détails (couleur, matériau, dimensions).
     */
    private void enrichItemDetails(ItemRequest item, String context) {
        if (context == null) return;

        // Extraire la couleur
        Matcher colorMatcher = COLOR_PATTERN.matcher(context);
        if (colorMatcher.find()) {
            item.setColor(colorMatcher.group(1).toLowerCase());
        }

        // Extraire le matériau
        Matcher materialMatcher = MATERIAL_PATTERN.matcher(context);
        if (materialMatcher.find()) {
            item.setMaterial(materialMatcher.group(1).toLowerCase());
        }

        // Extraire les dimensions
        Matcher dimMatcher = DIMENSION_PATTERN.matcher(context);
        if (dimMatcher.find()) {
            item.setDimensions(dimMatcher.group(1));
        }

        // Extraire des caractéristiques additionnelles
        extractCharacteristics(item, context);
    }

    /**
     * Extrait des caractéristiques spécifiques d'un article.
     */
    private void extractCharacteristics(ItemRequest item, String context) {
        String lowerContext = context.toLowerCase();

        // Caractéristiques courantes
        if (lowerContext.contains("ergonomique")) {
            item.addCharacteristic("ergonomique");
        }
        if (lowerContext.contains("assis-debout") || lowerContext.contains("réglable en hauteur")) {
            item.addCharacteristic("réglable en hauteur");
        }
        if (lowerContext.contains("roulette")) {
            item.addCharacteristic("avec roulettes");
        }
        if (lowerContext.contains("accoudoir")) {
            item.addCharacteristic("avec accoudoirs");
        }
        if (lowerContext.contains("led")) {
            item.addCharacteristic("LED");
        }
        if (lowerContext.matches(".*\\d+\\s*personnes?.*")) {
            Matcher m = Pattern.compile("(\\d+)\\s*personnes?").matcher(lowerContext);
            if (m.find()) {
                item.addCharacteristic(m.group(1) + " personnes");
            }
        }
    }

    /**
     * Vérifie si un texte ressemble à un nom de produit.
     */
    private boolean looksLikeProduct(String text) {
        if (text == null || text.length() < 3) return false;

        String lower = text.toLowerCase().trim();

        // Vérifier si c'est un mot-clé de produit connu
        for (String keyword : PRODUCT_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }

        // Exclure les mots qui ne sont pas des produits
        List<String> excludedWords = Arrays.asList(
            "bonjour", "bonsoir", "merci", "cordialement", "urgent",
            "budget", "livraison", "délai", "prix", "devis", "besoin",
            "souhaite", "voudrais", "pourriez", "pouvez"
        );

        for (String excluded : excludedWords) {
            if (lower.equals(excluded) || lower.startsWith(excluded + " ")) {
                return false;
            }
        }

        return lower.length() >= 3 && lower.length() <= 50;
    }

    /**
     * Normalise le nom d'un produit.
     */
    private String normalizeProduct(String product) {
        if (product == null) return null;

        return product.trim()
            .toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("^(des?|les?|la|un|une)\\s+", ""); // Supprimer les articles
    }

    // === EXTRACTION DU BUDGET ===

    /**
     * Extrait le budget indicatif depuis le texte.
     *
     * @param text Texte source
     * @param info ExtractedInfo à remplir
     */
    private void extractBudget(String text, ExtractedInfo info) {
        // Méthode 1 : Pattern avec mot-clé budget
        Matcher matcher = BUDGET_PATTERN.matcher(text);
        if (matcher.find()) {
            parseBudget(matcher.group(1), matcher.group(2), matcher.group(3), info);
            info.setBudgetRaw(matcher.group(0).trim());
            return;
        }

        // Méthode 2 : Pattern alternatif
        matcher = BUDGET_ALT_PATTERN.matcher(text);
        if (matcher.find()) {
            parseBudget(matcher.group(1), matcher.group(2), matcher.group(3), info);
            info.setBudgetRaw(matcher.group(0).trim());
            return;
        }

        // Méthode 3 : Chercher un montant significatif (> 1000€)
        matcher = AMOUNT_PATTERN.matcher(text);
        while (matcher.find()) {
            String amountStr = matcher.group(1);
            String unit = matcher.group(2);
            String htTtc = matcher.group(3);

            Double amount = parseAmount(amountStr, unit);
            if (amount != null && amount >= 1000) {
                info.setBudget(amount);
                info.setBudgetUnit(formatBudgetUnit(unit, htTtc));
                info.setBudgetRaw(matcher.group(0).trim());
                return;
            }
        }
    }

    /**
     * Parse un montant budgétaire.
     */
    private void parseBudget(String amountStr, String unit, String htTtc, ExtractedInfo info) {
        Double amount = parseAmount(amountStr, unit);
        if (amount != null) {
            info.setBudget(amount);
            info.setBudgetUnit(formatBudgetUnit(unit, htTtc));
        }
    }

    /**
     * Parse une chaîne de montant en valeur numérique.
     */
    private Double parseAmount(String amountStr, String unit) {
        if (amountStr == null) return null;

        try {
            // Nettoyer le montant
            String cleaned = amountStr
                .replaceAll("\\s", "")
                .replace(",", ".");

            double amount = Double.parseDouble(cleaned);

            // Gérer les k€ (milliers)
            if (unit != null && unit.toLowerCase().startsWith("k")) {
                amount *= 1000;
            }

            return amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Formate l'unité du budget.
     */
    private String formatBudgetUnit(String unit, String htTtc) {
        StringBuilder sb = new StringBuilder("€");

        if (htTtc != null && !htTtc.isBlank()) {
            sb.append(" ").append(htTtc.toUpperCase());
        }

        return sb.toString();
    }

    // === EXTRACTION DE LA DATE DE LIVRAISON ===

    /**
     * Extrait la date de livraison souhaitée.
     *
     * @param text Texte source
     * @param info ExtractedInfo à remplir
     */
    private void extractDeliveryDate(String text, ExtractedInfo info) {
        Matcher matcher = DELIVERY_DATE_PATTERN.matcher(text);

        if (matcher.find()) {
            String dateStr = matcher.group(1);
            info.setDeliveryDateRaw(dateStr);

            // Essayer de parser la date
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                info.setDeliveryDate(date);
            }
        }
    }

    /**
     * Parse une chaîne de date en LocalDate.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        String lower = dateStr.toLowerCase().trim();
        int currentYear = LocalDate.now().getYear();

        // Format numérique (15/03/2024)
        Matcher numMatcher = DATE_NUMERIC_PATTERN.matcher(lower);
        if (numMatcher.find()) {
            try {
                int day = Integer.parseInt(numMatcher.group(1));
                int month = Integer.parseInt(numMatcher.group(2));
                int year = Integer.parseInt(numMatcher.group(3));

                if (year < 100) year += 2000;

                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {}
        }

        // Format "15 mars 2024" ou "15 mars"
        for (Map.Entry<String, Integer> entry : MONTH_NAMES.entrySet()) {
            String monthName = entry.getKey();
            if (lower.contains(monthName)) {
                Pattern p = Pattern.compile("(\\d{1,2})\\s+" + monthName + "(?:\\s+(\\d{4}))?");
                Matcher m = p.matcher(lower);
                if (m.find()) {
                    try {
                        int day = Integer.parseInt(m.group(1));
                        int month = entry.getValue();
                        int year = m.group(2) != null ? Integer.parseInt(m.group(2)) : currentYear;
                        return LocalDate.of(year, month, day);
                    } catch (Exception ignored) {}
                }

                // Format "fin mars" ou "mars 2024"
                if (lower.contains("fin " + monthName) || lower.matches(monthName + "(?:\\s+\\d{4})?")) {
                    int month = entry.getValue();
                    Pattern yearP = Pattern.compile(monthName + "\\s+(\\d{4})");
                    Matcher yearM = yearP.matcher(lower);
                    int year = yearM.find() ? Integer.parseInt(yearM.group(1)) : currentYear;

                    // Fin du mois = dernier jour
                    if (lower.contains("fin")) {
                        return LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
                    }
                    // Sinon, milieu du mois
                    return LocalDate.of(year, month, 15);
                }
            }
        }

        return null;
    }

    // === EXTRACTION DE L'URGENCE ===

    /**
     * Extrait le niveau d'urgence de la demande.
     *
     * @param text Texte source
     * @param info ExtractedInfo à remplir
     */
    private void extractUrgency(String text, ExtractedInfo info) {
        // Vérifier d'abord très urgent
        if (VERY_URGENT_PATTERN.matcher(text).find()) {
            info.setUrgency("très urgent");
            return;
        }

        // Ensuite urgent
        if (URGENT_PATTERN.matcher(text).find()) {
            info.setUrgency("urgent");
            return;
        }

        // Par défaut : normal
        info.setUrgency("normal");
    }

    // === EXTRACTION DES NOTES ADDITIONNELLES ===

    /**
     * Extrait les notes et contraintes additionnelles.
     *
     * @param text Texte source
     * @param info ExtractedInfo à remplir
     */
    private void extractAdditionalNotes(String text, ExtractedInfo info) {
        // Chercher les notes explicites
        Matcher matcher = NOTE_PATTERNS.matcher(text);
        while (matcher.find()) {
            String note = matcher.group(1);
            if (note != null && !note.isBlank()) {
                info.addNote(note.trim());
            }
        }

        // Chercher des contraintes implicites
        String lower = text.toLowerCase();

        if (lower.contains("livraison gratuite") || lower.contains("franco de port")) {
            info.addNote("Livraison gratuite souhaitée");
        }

        if (lower.contains("montage") || lower.contains("installation")) {
            if (lower.contains("avec montage") || lower.contains("montage inclus")) {
                info.addNote("Montage/installation inclus souhaité");
            }
        }

        if (lower.contains("garantie")) {
            Matcher gMatcher = Pattern.compile("garantie\\s+(\\d+)\\s*ans?").matcher(lower);
            if (gMatcher.find()) {
                info.addNote("Garantie " + gMatcher.group(1) + " ans demandée");
            }
        }

        if (lower.contains("paiement") || lower.contains("règlement")) {
            if (lower.contains("30 jours") || lower.contains("fin de mois")) {
                info.addNote("Paiement à 30 jours souhaité");
            }
        }

        if (lower.contains("échantillon") || lower.contains("showroom") || lower.contains("visite")) {
            info.addNote("Demande de voir les produits avant commande");
        }
    }

    // === CALCUL DE LA CONFIANCE ===

    /**
     * Calcule le score de confiance global de l'extraction.
     * Le score est basé sur :
     * - Présence d'articles (40%)
     * - Qualité des articles (20%)
     * - Présence de budget (15%)
     * - Présence de date (15%)
     * - Cohérence générale (10%)
     *
     * @param info ExtractedInfo à évaluer
     */
    private void calculateConfidence(ExtractedInfo info) {
        double score = 0.0;

        // 1. Présence d'articles (40%)
        if (info.hasItems()) {
            int itemCount = info.getItemCount();
            score += Math.min(0.4, 0.2 + (itemCount * 0.05)); // 0.2 base + 0.05 par article, max 0.4
        }

        // 2. Qualité des articles (20%)
        if (info.hasItems()) {
            double qualitySum = 0;
            for (ItemRequest item : info.getItems()) {
                double itemScore = 0;
                if (item.getProduct() != null) itemScore += 0.4;
                if (item.getQuantity() != null) itemScore += 0.3;
                if (item.getColor() != null || item.getMaterial() != null) itemScore += 0.15;
                if (item.getDimensions() != null) itemScore += 0.15;
                qualitySum += itemScore;
            }
            score += (qualitySum / info.getItemCount()) * 0.2;
        }

        // 3. Présence de budget (15%)
        if (info.hasBudget()) {
            score += 0.1;
            if (info.getBudget() != null) {
                score += 0.05; // Bonus si budget parsé numériquement
            }
        }

        // 4. Présence de date (15%)
        if (info.hasDeliveryDate()) {
            score += 0.1;
            if (info.getDeliveryDate() != null) {
                score += 0.05; // Bonus si date parsée
            }
        }

        // 5. Cohérence (10%)
        if (info.hasItems() && (info.hasBudget() || info.hasDeliveryDate())) {
            score += 0.1; // Bonus si on a articles + (budget ou date)
        }

        // Arrondir et limiter
        info.setConfidence(Math.round(score * 100.0) / 100.0);
    }

    // === MÉTHODE UTILITAIRE ===

    /**
     * Fournit des statistiques détaillées sur l'extraction.
     *
     * @param info Résultat de l'extraction
     * @return Statistiques sous forme de texte
     */
    public String getExtractionStats(ExtractedInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistiques d'extraction:\n");
        sb.append(String.format("- Articles extraits: %d\n", info.getItemCount()));
        sb.append(String.format("- Quantité totale: %d unités\n", info.getTotalQuantity()));
        sb.append(String.format("- Budget détecté: %s\n", info.hasBudget() ? "Oui" : "Non"));
        sb.append(String.format("- Date détectée: %s\n", info.hasDeliveryDate() ? "Oui" : "Non"));
        sb.append(String.format("- Niveau d'urgence: %s\n", info.getUrgency()));
        sb.append(String.format("- Notes additionnelles: %d\n", info.getAdditionalNotes().size()));
        sb.append(String.format("- Confiance globale: %.0f%%\n", info.getConfidence() * 100));
        return sb.toString();
    }
}
