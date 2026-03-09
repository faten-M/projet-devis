package com.projetdevis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Représente les données formatées pour export vers un CRM externe.
 *
 * Cette classe prépare les données dans différents formats :
 * - JSON (pour API REST)
 * - CSV (pour import fichier)
 * - XML (pour intégration legacy)
 * - Map clé-valeur (pour intégration générique)
 *
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * @author BMAD Pipeline - Étape 7
 * @version 1.0
 */
public class CrmExport {

    // === ÉNUMÉRATIONS ===

    /**
     * Format d'export supporté.
     */
    public enum ExportFormat {
        JSON("JSON", "application/json"),
        CSV("CSV", "text/csv"),
        XML("XML", "application/xml"),
        KEY_VALUE("Key-Value", "text/plain");

        private final String label;
        private final String mimeType;

        ExportFormat(String label, String mimeType) {
            this.label = label;
            this.mimeType = mimeType;
        }

        public String getLabel() {
            return label;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

    /**
     * Type de CRM cible.
     */
    public enum CrmTarget {
        SALESFORCE("Salesforce", "SF"),
        HUBSPOT("HubSpot", "HS"),
        PIPEDRIVE("Pipedrive", "PD"),
        DYNAMICS("Microsoft Dynamics", "DYN"),
        ZOHO("Zoho CRM", "ZH"),
        GENERIC("Générique", "GEN");

        private final String label;
        private final String code;

        CrmTarget(String label, String code) {
            this.label = label;
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public String getCode() {
            return code;
        }
    }

    // === ATTRIBUTS ===

    /** Identifiant unique de l'export */
    private String exportId;

    /** Date et heure de génération */
    private LocalDateTime generatedAt;

    /** Format d'export */
    private ExportFormat format;

    /** CRM cible */
    private CrmTarget target;

    /** Enregistrement CRM source */
    private CrmRecord sourceRecord;

    /** Client source */
    private Client sourceClient;

    /** Données exportées (clé-valeur) */
    private Map<String, Object> data;

    /** Données du client */
    private Map<String, Object> clientData;

    /** Données du devis */
    private Map<String, Object> quoteData;

    /** Lignes du devis */
    private List<Map<String, Object>> quoteLines;

    /** Contenu exporté (format final) */
    private String exportedContent;

    /** Métadonnées de l'export */
    private Map<String, String> metadata;

    // === COMPTEUR POUR ID UNIQUE ===

    private static int counter = 0;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public CrmExport() {
        this.exportId = generateExportId();
        this.generatedAt = LocalDateTime.now();
        this.format = ExportFormat.JSON;
        this.target = CrmTarget.GENERIC;
        this.data = new LinkedHashMap<>();
        this.clientData = new LinkedHashMap<>();
        this.quoteData = new LinkedHashMap<>();
        this.quoteLines = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();

        // Métadonnées par défaut
        metadata.put("generator", "BMAD Pipeline v1.0");
        metadata.put("stage", "7 - Intégration CRM");
        metadata.put("timestamp", generatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Constructeur avec enregistrement CRM.
     *
     * @param record Enregistrement CRM source
     */
    public CrmExport(CrmRecord record) {
        this();
        this.sourceRecord = record;
        if (record != null) {
            this.sourceClient = record.getClient();
            buildFromRecord(record);
        }
    }

    /**
     * Constructeur avec enregistrement et format.
     *
     * @param record Enregistrement CRM source
     * @param format Format d'export
     * @param target CRM cible
     */
    public CrmExport(CrmRecord record, ExportFormat format, CrmTarget target) {
        this(record);
        this.format = format;
        this.target = target;
    }

    // === GÉNÉRATION D'IDENTIFIANTS ===

    /**
     * Génère un identifiant d'export unique.
     */
    private static synchronized String generateExportId() {
        counter++;
        return String.format("EXP-%d-%04d",
            System.currentTimeMillis() % 100000, counter);
    }

    // === CONSTRUCTION DES DONNÉES ===

    /**
     * Construit les données d'export à partir d'un enregistrement CRM.
     *
     * @param record Enregistrement source
     */
    private void buildFromRecord(CrmRecord record) {
        // Données principales de l'opportunité
        data.put("recordId", record.getRecordId());
        data.put("opportunityId", record.getOpportunityId());
        data.put("quoteNumber", record.getQuoteNumber());
        data.put("opportunityName", record.getOpportunityName());
        data.put("status", record.getStatus().name());
        data.put("statusLabel", record.getStatus().getLabel());
        data.put("probability", record.getProbabiliteConversion());
        data.put("amount", record.getMontantHT());
        data.put("amountTTC", record.getMontantTTC());
        data.put("weightedAmount", record.getMontantPondere());
        data.put("source", record.getSource());
        data.put("campaign", record.getCampagne());
        data.put("owner", record.getCommercialResponsable());
        data.put("createdAt", formatDateTime(record.getDateCreation()));
        data.put("modifiedAt", formatDateTime(record.getDateModification()));
        data.put("quoteSentAt", formatDateTime(record.getDateEnvoiDevis()));
        data.put("followUpCount", record.getNombreRelances());
        data.put("tags", record.getTags());

        // Données client
        buildClientData(record.getClient());

        // Données devis
        buildQuoteData(record.getValidatedQuote());
    }

    /**
     * Construit les données client.
     *
     * @param client Client source
     */
    private void buildClientData(Client client) {
        if (client == null) {
            clientData.put("status", "NOUVEAU");
            return;
        }

        clientData.put("clientId", client.getClientId());
        clientData.put("accountNumber", client.getNumeroCompte());
        clientData.put("companyName", client.getRaisonSociale());
        clientData.put("tradeName", client.getNomCommercial());
        clientData.put("siret", client.getSiret());
        clientData.put("vatNumber", client.getNumeroTva());
        clientData.put("segment", client.getSegment().name());
        clientData.put("segmentLabel", client.getSegment().getLabel());
        clientData.put("status", client.getStatus().name());
        clientData.put("statusLabel", client.getStatus().getLabel());
        clientData.put("assignedSales", client.getCommercialAttitre());
        clientData.put("paymentTerms", client.getConditionsPaiement());
        clientData.put("standardDiscount", client.getRemiseStandard());
        clientData.put("creditLimit", client.getPlafondCredit());
        clientData.put("currentOutstanding", client.getEncoursActuel());
        clientData.put("orderCount", client.getNombreCommandes());
        clientData.put("totalRevenue", client.getChiffreAffairesCumule());

        // Contact principal
        Client.Contact contact = client.getContactPrincipal();
        if (contact != null) {
            clientData.put("contactName", contact.getNomComplet());
            clientData.put("contactEmail", contact.getEmail());
            clientData.put("contactPhone", contact.getTelephone());
            clientData.put("contactMobile", contact.getMobile());
            clientData.put("contactRole", contact.getFonction());
        }

        // Adresse
        Client.Adresse adresse = client.getAdresseSiege();
        if (adresse != null) {
            clientData.put("addressLine1", adresse.getLigne1());
            clientData.put("addressLine2", adresse.getLigne2());
            clientData.put("postalCode", adresse.getCodePostal());
            clientData.put("city", adresse.getVille());
            clientData.put("country", adresse.getPays());
        }
    }

    /**
     * Construit les données du devis.
     *
     * @param validated Devis validé source
     */
    private void buildQuoteData(ValidatedQuote validated) {
        if (validated == null || validated.getDraft() == null) {
            return;
        }

        DraftQuote draft = validated.getDraft();

        quoteData.put("quoteNumber", draft.getQuoteNumber());
        quoteData.put("subject", draft.getSubject());
        quoteData.put("status", draft.getStatus().name());
        quoteData.put("statusLabel", draft.getStatus().getLabel());
        quoteData.put("priority", draft.getPriority().name());
        quoteData.put("priorityLabel", draft.getPriority().getLabel());
        quoteData.put("createdAt", formatDateTime(draft.getCreatedAt()));
        quoteData.put("validUntil", draft.getValidUntil() != null ?
            draft.getValidUntil().toString() : null);
        quoteData.put("validityDays", draft.getValidityDays());

        // Montants
        quoteData.put("totalHTBrut", draft.getTotalHTBrut());
        quoteData.put("totalDiscount", draft.getTotalDiscount());
        quoteData.put("totalHT", draft.getTotalHT());
        quoteData.put("totalTVA", draft.getTotalTVA());
        quoteData.put("tvaRate", draft.getTvaRate());
        quoteData.put("totalTTC", draft.getTotalTTC());

        // Budget client
        quoteData.put("clientBudget", draft.getClientBudget());
        quoteData.put("budgetDelta", draft.getBudgetDelta());
        quoteData.put("budgetRespected", draft.isBudgetRespected());

        // Livraison
        quoteData.put("requestedDeliveryDate", draft.getRequestedDeliveryDate() != null ?
            draft.getRequestedDeliveryDate().toString() : null);
        quoteData.put("estimatedDeliveryDate", draft.getEstimatedDeliveryDate() != null ?
            draft.getEstimatedDeliveryDate().toString() : null);
        quoteData.put("deliveryDays", draft.getDeliveryDays());
        quoteData.put("deliveryFees", draft.getDeliveryFees());
        quoteData.put("deliveryIncluded", draft.isDeliveryIncluded());
        quoteData.put("installationIncluded", draft.isInstallationIncluded());

        // Conditions
        quoteData.put("paymentTerms", draft.getPaymentTerms());
        quoteData.put("warranty", draft.getWarranty());
        quoteData.put("specialConditions", draft.getSpecialConditions());

        // Qualité
        quoteData.put("confidence", draft.getConfidence());
        quoteData.put("itemCount", draft.getItemCount());
        quoteData.put("totalQuantity", draft.getTotalQuantity());

        // Validation
        quoteData.put("validatorName", validated.getValidatorName());
        quoteData.put("validatedAt", formatDateTime(validated.getValidatedAt()));
        quoteData.put("validatorSignature", validated.getValidatorSignature());
        quoteData.put("validationComment", validated.getValidationComment());
        quoteData.put("modificationCount", validated.getModificationCount());

        // Lignes du devis
        buildQuoteLines(draft);
    }

    /**
     * Construit les lignes du devis.
     *
     * @param draft Brouillon source
     */
    private void buildQuoteLines(DraftQuote draft) {
        for (QuoteItem item : draft.getItems()) {
            Map<String, Object> line = new LinkedHashMap<>();

            line.put("lineNumber", item.getLineNumber());
            line.put("reference", item.getReference());
            line.put("designation", item.getDesignation());
            line.put("description", item.getDescription());
            line.put("category", item.getCategory() != null ? item.getCategory().name() : null);
            line.put("categoryLabel", item.getCategory() != null ? item.getCategory().getLabel() : null);
            line.put("quantity", item.getQuantity());
            line.put("unit", item.getUnit());
            line.put("unitPriceHT", item.getUnitPriceHT());
            line.put("unitPriceTTC", item.getUnitPriceTTC());
            line.put("totalPriceHT", item.getTotalPriceHT());
            line.put("totalPriceTTC", item.getTotalPriceTTC());
            line.put("tvaRate", item.getTvaRate());
            line.put("discountPercent", item.getDiscountPercent());
            line.put("discountAmount", item.getDiscountAmount());
            line.put("priceRange", item.getPriceRange() != null ? item.getPriceRange().name() : null);
            line.put("status", item.getStatus() != null ? item.getStatus().name() : null);
            line.put("deliveryDays", item.getDeliveryDays());
            line.put("priceConfidence", item.getPriceConfidence());
            line.put("options", item.getOptions());
            line.put("warnings", item.getWarnings());

            quoteLines.add(line);
        }
    }

    // === GÉNÉRATION DES EXPORTS ===

    /**
     * Génère le contenu exporté dans le format spécifié.
     *
     * @return Contenu exporté
     */
    public String generate() {
        switch (format) {
            case JSON:
                exportedContent = generateJson();
                break;
            case CSV:
                exportedContent = generateCsv();
                break;
            case XML:
                exportedContent = generateXml();
                break;
            case KEY_VALUE:
                exportedContent = generateKeyValue();
                break;
        }
        return exportedContent;
    }

    /**
     * Génère le contenu au format JSON.
     *
     * @return JSON string
     */
    private String generateJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // Metadata
        sb.append("  \"metadata\": ").append(mapToJson(metadata, 2)).append(",\n");

        // Opportunity
        sb.append("  \"opportunity\": ").append(mapToJson(data, 2)).append(",\n");

        // Client
        sb.append("  \"client\": ").append(mapToJson(clientData, 2)).append(",\n");

        // Quote
        sb.append("  \"quote\": ").append(mapToJson(quoteData, 2)).append(",\n");

        // Quote lines
        sb.append("  \"quoteLines\": [\n");
        for (int i = 0; i < quoteLines.size(); i++) {
            sb.append("    ").append(mapToJson(quoteLines.get(i), 4));
            if (i < quoteLines.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Génère le contenu au format CSV.
     *
     * @return CSV string
     */
    private String generateCsv() {
        StringBuilder sb = new StringBuilder();

        // En-têtes pour les lignes de devis
        sb.append("# BMAD Export - ").append(exportId).append("\n");
        sb.append("# Generated: ").append(formatDateTime(generatedAt)).append("\n");
        sb.append("# Quote: ").append(data.get("quoteNumber")).append("\n");
        sb.append("# Client: ").append(clientData.get("companyName")).append("\n\n");

        // Résumé opportunité
        sb.append("## OPPORTUNITY\n");
        sb.append("Field,Value\n");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append(entry.getKey()).append(",").append(escapeCsv(entry.getValue())).append("\n");
        }
        sb.append("\n");

        // Lignes du devis
        if (!quoteLines.isEmpty()) {
            sb.append("## QUOTE LINES\n");

            // En-têtes
            Map<String, Object> firstLine = quoteLines.get(0);
            sb.append(String.join(",", firstLine.keySet())).append("\n");

            // Données
            for (Map<String, Object> line : quoteLines) {
                sb.append(line.values().stream()
                    .map(this::escapeCsv)
                    .collect(Collectors.joining(","))).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Génère le contenu au format XML.
     *
     * @return XML string
     */
    private String generateXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<CrmExport>\n");

        // Metadata
        sb.append("  <Metadata>\n");
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            sb.append("    <").append(entry.getKey()).append(">")
              .append(escapeXml(entry.getValue()))
              .append("</").append(entry.getKey()).append(">\n");
        }
        sb.append("  </Metadata>\n");

        // Opportunity
        sb.append("  <Opportunity>\n");
        appendXmlMap(sb, data, 4);
        sb.append("  </Opportunity>\n");

        // Client
        sb.append("  <Client>\n");
        appendXmlMap(sb, clientData, 4);
        sb.append("  </Client>\n");

        // Quote
        sb.append("  <Quote>\n");
        appendXmlMap(sb, quoteData, 4);
        sb.append("    <Lines>\n");
        for (Map<String, Object> line : quoteLines) {
            sb.append("      <Line>\n");
            appendXmlMap(sb, line, 8);
            sb.append("      </Line>\n");
        }
        sb.append("    </Lines>\n");
        sb.append("  </Quote>\n");

        sb.append("</CrmExport>\n");
        return sb.toString();
    }

    /**
     * Génère le contenu au format clé-valeur.
     *
     * @return Key-value string
     */
    private String generateKeyValue() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== BMAD CRM EXPORT ===\n");
        sb.append("Export ID: ").append(exportId).append("\n");
        sb.append("Generated: ").append(formatDateTime(generatedAt)).append("\n");
        sb.append("Format: ").append(format.getLabel()).append("\n");
        sb.append("Target: ").append(target.getLabel()).append("\n\n");

        sb.append("--- OPPORTUNITY ---\n");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }

        sb.append("\n--- CLIENT ---\n");
        for (Map.Entry<String, Object> entry : clientData.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }

        sb.append("\n--- QUOTE ---\n");
        for (Map.Entry<String, Object> entry : quoteData.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }

        sb.append("\n--- QUOTE LINES ---\n");
        for (int i = 0; i < quoteLines.size(); i++) {
            sb.append("\n[Line ").append(i + 1).append("]\n");
            for (Map.Entry<String, Object> entry : quoteLines.get(i).entrySet()) {
                sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }

        return sb.toString();
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Formate une date/heure.
     */
    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Convertit une Map en JSON.
     */
    private String mapToJson(Map<String, ?> map, int indent) {
        if (map == null || map.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder();
        String indentStr = " ".repeat(indent);
        sb.append("{\n");

        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String value = valueToJson(entry.getValue());
            entries.add(indentStr + "  \"" + entry.getKey() + "\": " + value);
        }
        sb.append(String.join(",\n", entries));
        sb.append("\n").append(indentStr).append("}");

        return sb.toString();
    }

    /**
     * Convertit une valeur en JSON.
     */
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Number) return value.toString();
        if (value instanceof Boolean) return value.toString();
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return "[]";
            return "[" + list.stream()
                .map(this::valueToJson)
                .collect(Collectors.joining(", ")) + "]";
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    /**
     * Échappe une chaîne pour JSON.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Échappe une valeur pour CSV.
     */
    private String escapeCsv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /**
     * Échappe une chaîne pour XML.
     */
    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Ajoute une Map au XML.
     */
    private void appendXmlMap(StringBuilder sb, Map<String, Object> map, int indent) {
        String indentStr = " ".repeat(indent);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                sb.append(indentStr).append("<").append(entry.getKey()).append(">\n");
                for (Object item : (List<?>) value) {
                    sb.append(indentStr).append("  <item>").append(escapeXml(String.valueOf(item))).append("</item>\n");
                }
                sb.append(indentStr).append("</").append(entry.getKey()).append(">\n");
            } else {
                sb.append(indentStr).append("<").append(entry.getKey()).append(">")
                  .append(value != null ? escapeXml(value.toString()) : "")
                  .append("</").append(entry.getKey()).append(">\n");
            }
        }
    }

    // === GETTERS ===

    public String getExportId() {
        return exportId;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public ExportFormat getFormat() {
        return format;
    }

    public void setFormat(ExportFormat format) {
        this.format = format;
    }

    public CrmTarget getTarget() {
        return target;
    }

    public void setTarget(CrmTarget target) {
        this.target = target;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getClientData() {
        return clientData;
    }

    public Map<String, Object> getQuoteData() {
        return quoteData;
    }

    public List<Map<String, Object>> getQuoteLines() {
        return quoteLines;
    }

    public String getExportedContent() {
        return exportedContent;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    // === MÉTHODES STANDARD ===

    @Override
    public String toString() {
        return "CrmExport {\n" +
               "  exportId: \"" + exportId + "\"\n" +
               "  format: " + format.getLabel() + "\n" +
               "  target: " + target.getLabel() + "\n" +
               "  generatedAt: " + formatDateTime(generatedAt) + "\n" +
               "  dataFields: " + data.size() + "\n" +
               "  quoteLines: " + quoteLines.size() + "\n" +
               "}";
    }
}
