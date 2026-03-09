package com.projetdevis.service;

import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractionService {

    private static final Pattern ITEM_LINE_PATTERN = Pattern.compile(
        "(?im)^\\s*"
        + "(?:[-•*>]\\s*)?"
        + "(?:(\\d+)\\s*(?:x|×|unités?\\s+de)?\\s+)?"
        + "([a-zàâäéèêëïîôùûüç][a-zàâäéèêëïîôùûüç\\s-]{2,50})"
        + "(?:\\s+(.+))?"
        + "$"
    );

    private static final Pattern ITEM_ALT_PATTERN = Pattern.compile(
        "(?im)(?:des?\\s+)?([a-zàâäéèêëïîôùûüç][a-zàâäéèêëïîôùûüç\\s-]{2,40})"
        + "\\s*[:\\(]\\s*(\\d+)\\s*\\)?"
    );

    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
        "(?i)(\\d+)\\s*(?:x|×|unités?|pièces?|postes?|lots?|sets?)?\\s*"
    );

    // expressions humaines utilisées pour la détection/stripping
    private static final List<String> HUMAN_QUANTITY_PHRASES = Arrays.asList(
        "une dizaine", "dizaine", "10aine",
        "une vingtaine", "vingtaine", "20aine",
        "une trentaine", "trentaine", "30aine",
        "une quarantaine", "quarantaine", "40aine",
        "une cinquantaine", "cinquantaine", "50aine",
        "une soixantaine", "soixantaine", "60aine",
        "une centaine", "centaine",
        "quelques", "plusieurs", "bcp", "beaucoup", "un peu", "à peine",
        "environ", "à peu près", "au moins", "minimum"
    );

    private static final Pattern DIMENSION_PATTERN = Pattern.compile(
        "(?i)(\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?\\s*[x×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?)"
        + "(?:\\s*[x×]\\s*\\d+(?:[.,]\\d+)?\\s*(?:cm|m|mm)?)?"
    );

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

    private static final Pattern BUDGET_PATTERN = Pattern.compile(
        "(?i)(?:budget|enveloppe|montant|prix\\s*max(?:imum)?)"
        + "[^\\d]{0,30}"
        + "(\\d[\\d\\s.,]*)"
        + "\\s*(k?€|euros?|k\\s*euros?)?\\s*(ht|ttc)?"
    );

    private static final Pattern BUDGET_ALT_PATTERN = Pattern.compile(
        "(?i)(\\d[\\d\\s.,]*)\\s*(k?€|euros?)\\s*(ht|ttc)?\\s*(?:de\\s+budget|max(?:imum)?)"
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?i)(\\d{1,3}(?:[\\s.,]?\\d{3})*(?:[.,]\\d{2})?)\\s*(k?€|euros?)\\s*(ht|ttc)?"
    );

    private static final Pattern DELIVERY_DATE_PATTERN = Pattern.compile(
        "(?i)(?:livraison|livré|délai|avant|pour\\s+le|d'ici(?:\\s+le)?|fin)"
        + "[^\\d]{0,30}"
        + "(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}|"
        + "\\d{1,2}\\s+(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?|"
        + "fin\\s+(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?|"
        + "(?:janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(?:\\s+\\d{4})?)"
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

    private static final Pattern URGENT_PATTERN = Pattern.compile(
        "(?i)(urgent|urgence|asap|au plus vite|dès que possible|rapidement|pressé|en urgence)"
    );

    private static final Pattern VERY_URGENT_PATTERN = Pattern.compile(
        "(?i)(très urgent|extrêmement urgent|urgentissime|immédiat|tout de suite|aujourd'hui)"
    );

    private static final Pattern NOTE_PATTERNS = Pattern.compile(
        "(?im)^\\s*(?:note|nb|attention|important|contrainte|condition|remarque)\\s*[:\\-]?\\s*(.+)$"
    );

    private static final List<String> PRODUCT_KEYWORDS = Arrays.asList(
        "bureau", "bureaux", "table", "tables", "chaise", "chaises",
        "fauteuil", "fauteuils", "caisson", "caissons", "armoire", "armoires",
        "étagère", "étagères", "lampe", "lampes", "écran", "écrans",
        "siège", "sièges", "poste", "postes", "rangement", "rangements",
        "canapé", "canapés", "pouf", "poufs", "tabouret", "tabourets",
        "meuble", "meubles", "mobilier", "cloison", "cloisons",
        "panneau", "panneaux", "tiroir", "tiroirs", "bibliothèque", "bibliothèques"
    );

    private final ExtractInfoIA quantityIA = new ExtractInfoIA();

    public ExtractedInfo extract(String cleanedEmail) {
        ExtractedInfo info = new ExtractedInfo(cleanedEmail);

        if (cleanedEmail == null || cleanedEmail.isBlank()) {
            info.setConfidence(0.0);
            return info;
        }

        extractItems(cleanedEmail, info);
        extractBudget(cleanedEmail, info);
        extractDeliveryDate(cleanedEmail, info);
        extractUrgency(cleanedEmail, info);
        extractAdditionalNotes(cleanedEmail, info);
        calculateConfidence(info);

        return info;
    }

    private void extractItems(String text, ExtractedInfo info) {
        Set<String> processedLines = new HashSet<>();
        extractItemsFromListFormat(text, info, processedLines);
        extractItemsFromAltFormat(text, info, processedLines);
        extractItemsFromProductKeywords(text, info, processedLines);
    }

    private void extractItemsFromListFormat(String text, ExtractedInfo info, Set<String> processedLines) {
        String[] lines = text.split("\n");

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

                if (product != null && looksLikeProduct(product)) {
                    // clean any human quantity words from the product text
                    String cleanedProduct = stripHumanQuantityPhrases(product);

                    ItemRequest item = new ItemRequest();
                    item.setProduct(normalizeProduct(cleanedProduct));
                    item.setRawLine(trimmedLine);

                    if (quantityStr != null) {
                        try {
                            item.setQuantity(Integer.parseInt(quantityStr));
                        } catch (NumberFormatException ignored) {}
                    } else {
                        // pas de nombre explicite, utiliser l'IA pour interpréter les quantités humaines
                        try {
                            Integer q = quantityIA.parseQuantity(trimmedLine);
                            if (q != null) {
                                item.setQuantity(q);
                            }
                        } catch (Exception ignored) {}
                    }

                    enrichItemDetails(item, trimmedLine);

                    if (item.isValid()) {
                        info.addItem(item);
                        // mark both full line and cleaned product to prevent duplicates
                        processedLines.add(trimmedLine.toLowerCase());
                        if (item.getProduct() != null) {
                            processedLines.add(item.getProduct().toLowerCase());
                        }
                    }
                }
            }
        }
    }

    private void extractItemsFromAltFormat(String text, ExtractedInfo info, Set<String> processedLines) {
        Matcher matcher = ITEM_ALT_PATTERN.matcher(text);

        while (matcher.find()) {
            String product = matcher.group(1);
            String quantityStr = matcher.group(2);

            if (product != null && looksLikeProduct(product)) {
                String key = product.toLowerCase().trim();
                if (!processedLines.contains(key)) {
                    String cleanedProduct = stripHumanQuantityPhrases(product);
                    ItemRequest item = new ItemRequest();
                    item.setProduct(normalizeProduct(cleanedProduct));

                    if (quantityStr != null) {
                        try {
                            item.setQuantity(quantityIA.parseQuantity(quantityStr));
                        } catch (Exception ignored) {}
                    } else {
                        // pas de quantité numérique, on interprète la ligne entière
                        int lineStart = text.lastIndexOf('\n', matcher.start()) + 1;
                        int lineEnd = text.indexOf('\n', matcher.end());
                        if (lineEnd == -1) lineEnd = text.length();
                        String line = text.substring(lineStart, lineEnd).trim();
                        try {
                            Integer q = quantityIA.parseQuantity(line);
                            if (q != null) {
                                item.setQuantity(q);
                            }
                        } catch (Exception ignored) {}
                    }

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

    private void extractItemsFromProductKeywords(String text, ExtractedInfo info, Set<String> processedLines) {
        String lowerText = text.toLowerCase();

        for (String keyword : PRODUCT_KEYWORDS) {
            if (lowerText.contains(keyword) && !processedLines.contains(keyword)) {
                int index = lowerText.indexOf(keyword);
                int lineStart = lowerText.lastIndexOf('\n', index) + 1;
                int lineEnd = lowerText.indexOf('\n', index);
                if (lineEnd == -1) lineEnd = text.length();

                String line = text.substring(lineStart, lineEnd).trim();

                Matcher qtyMatcher = QUANTITY_PATTERN.matcher(line);
                Integer quantity = null;
                if (qtyMatcher.find()) {
                    try {
                        quantity = quantityIA.parseQuantity(qtyMatcher.group(1));
                    } catch (Exception ignored) {}
                }

                if (quantity == null) {
                    quantity = extractQuantityFromText(line);
                }

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

    private Integer extractQuantityFromText(String text) {
        if (text == null || text.isBlank()) return null;

        try {
            for (String expr : HUMAN_QUANTITY_PHRASES) {
                if (text.toLowerCase().contains(expr)) {
                    return quantityIA.parseQuantity(expr);
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Remove common human quantity words/phrases from product descriptions.
     * This helps normalize items when the quantity was embedded as text.
     */
    private String stripHumanQuantityPhrases(String text) {
        if (text == null) return null;
        String result = text.toLowerCase();
        for (String expr : HUMAN_QUANTITY_PHRASES) {
            if (result.contains(expr)) {
                result = result.replace(expr, "");
            }
        }
        // clean up leftover prepositions like "de", "d'" or articles
        result = result.replaceAll("^\\s*(de|d'|des?)\\s*", "");
        return result.trim();
    }

    private void enrichItemDetails(ItemRequest item, String context) {
        if (context == null) return;

        Matcher colorMatcher = COLOR_PATTERN.matcher(context);
        if (colorMatcher.find()) {
            item.setColor(colorMatcher.group(1).toLowerCase());
        }

        Matcher materialMatcher = MATERIAL_PATTERN.matcher(context);
        if (materialMatcher.find()) {
            item.setMaterial(materialMatcher.group(1).toLowerCase());
        }

        Matcher dimMatcher = DIMENSION_PATTERN.matcher(context);
        if (dimMatcher.find()) {
            item.setDimensions(dimMatcher.group(1));
        }

        extractCharacteristics(item, context);
    }

    private void extractCharacteristics(ItemRequest item, String context) {
        String lowerContext = context.toLowerCase();

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

    private boolean looksLikeProduct(String text) {
        if (text == null || text.length() < 3) return false;

        String lower = text.toLowerCase().trim();

        for (String keyword : PRODUCT_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }

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

    private String normalizeProduct(String product) {
        if (product == null) return null;

        return product.trim()
            .toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("^(des?|les?|la|un|une)\\s+", "");
    }

    private void extractBudget(String text, ExtractedInfo info) {
        Matcher matcher = BUDGET_PATTERN.matcher(text);
        if (matcher.find()) {
            parseBudget(matcher.group(1), matcher.group(2), matcher.group(3), info);
            info.setBudgetRaw(matcher.group(0).trim());
            return;
        }

        matcher = BUDGET_ALT_PATTERN.matcher(text);
        if (matcher.find()) {
            parseBudget(matcher.group(1), matcher.group(2), matcher.group(3), info);
            info.setBudgetRaw(matcher.group(0).trim());
            return;
        }

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

    private void parseBudget(String amountStr, String unit, String htTtc, ExtractedInfo info) {
        Double amount = parseAmount(amountStr, unit);
        if (amount != null) {
            info.setBudget(amount);
            info.setBudgetUnit(formatBudgetUnit(unit, htTtc));
        }
    }

    private Double parseAmount(String amountStr, String unit) {
        if (amountStr == null) return null;

        try {
            String cleaned = amountStr
                .replaceAll("\\s", "")
                .replace(",", ".");

            double amount = Double.parseDouble(cleaned);

            if (unit != null && unit.toLowerCase().startsWith("k")) {
                amount *= 1000;
            }

            return amount;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatBudgetUnit(String unit, String htTtc) {
        StringBuilder sb = new StringBuilder("€");
        if (htTtc != null && !htTtc.isBlank()) {
            sb.append(" ").append(htTtc.toUpperCase());
        }
        return sb.toString();
    }

    private void extractDeliveryDate(String text, ExtractedInfo info) {
        Matcher matcher = DELIVERY_DATE_PATTERN.matcher(text);

        if (matcher.find()) {
            String dateStr = matcher.group(1);
            info.setDeliveryDateRaw(dateStr);
            LocalDate date = parseDate(dateStr);
            if (date != null) {
                info.setDeliveryDate(date);
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        String lower = dateStr.toLowerCase().trim();
        int currentYear = LocalDate.now().getYear();

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
                Pattern p2 = Pattern.compile("fin\\s+" + monthName + "(?:\\s+(\\d{4}))?");
                Matcher m2 = p2.matcher(lower);
                if (m2.find()) {
                    try {
                        int month = entry.getValue();
                        int year = m2.group(1) != null ? Integer.parseInt(m2.group(1)) : currentYear;
                        int day = 1;
                        return LocalDate.of(year, month, day);
                    } catch (Exception ignored) {}
                }
                break;
            }
        }

        return null;
    }

    private void extractUrgency(String text, ExtractedInfo info) {
        if (VERY_URGENT_PATTERN.matcher(text).find()) {
            info.setUrgency("VERY_URGENT");
        } else if (URGENT_PATTERN.matcher(text).find()) {
            info.setUrgency("URGENT");
        }
    }

    private void extractAdditionalNotes(String text, ExtractedInfo info) {
        Matcher matcher = NOTE_PATTERNS.matcher(text);
        while (matcher.find()) {
            info.addNote(matcher.group(1).trim());
        }
    }

    private void calculateConfidence(ExtractedInfo info) {
        double score = 0;
        if (!info.getItems().isEmpty()) score += 0.3;
        if (info.getBudget() != null) score += 0.2;
        if (info.getDeliveryDate() != null) score += 0.2;
        if (info.getUrgency() != null) score += 0.1;
        if (!info.getAdditionalNotes().isEmpty()) score += 0.1;
        info.setConfidence(Math.min(score, 1.0));
    }

    public String getExtractionStats(ExtractedInfo info) {
        if (info == null) return "(aucune info)";
        StringBuilder sb = new StringBuilder();
        sb.append("items=").append(info.getItemCount());
        if (info.getBudget() != null) {
            sb.append(" budget=").append(info.getFormattedBudget());
        }
        if (info.getDeliveryDate() != null) {
            sb.append(" delivery=").append(info.getDeliveryDate());
        }
        if (info.getUrgency() != null) {
            sb.append(" urgency=").append(info.getUrgency());
        }
        return sb.toString();
    }
}
