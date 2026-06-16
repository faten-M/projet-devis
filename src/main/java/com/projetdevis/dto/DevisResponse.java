package com.projetdevis.dto;

import com.projetdevis.model.AnalyzedItem;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.QuoteItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Réponse JSON du pipeline POST /api/devis.
 * Projection plate de DraftQuote sans la source d'analyse.
 */
public class DevisResponse {

    // --- En-tête ---
    private String quoteNumber;
    private LocalDateTime createdAt;
    private LocalDate validUntil;
    private String status;
    private String priority;
    private String subject;
    private String clientNom;
    private String clientEmail;
    private String emailOriginal;

    // --- Financier ---
    private Double totalHT;
    private Double totalTVA;
    private double  tvaRate;
    private Double totalTTC;
    private Double clientBudget;
    private Boolean budgetRespected;

    // --- Livraison ---
    private LocalDate requestedDeliveryDate;
    private Double    deliveryFees;
    private boolean   deliveryIncluded;

    // --- Garantie & conditions ---
    private String warranty;

    // --- Qualité ---
    private int confidence;
    private List<String> requiredActions;
    private List<String> recommendations;
    private List<String> warnings;
    private List<String> inconsistencies;

    // --- Articles ---
    private List<QuoteItemDto> items;

    // =========================================================================
    // DTO imbriqué : ligne de devis
    // =========================================================================

    public static class QuoteItemDto {
        private int    lineNumber;
        private String designation;
        private int    quantity;
        private Double unitPriceHT;
        private Double discountPercent;
        private Double totalPriceHT;
        private String category;
        private String status;

        public static QuoteItemDto from(QuoteItem item) {
            QuoteItemDto dto = new QuoteItemDto();
            dto.lineNumber     = item.getLineNumber();
            dto.designation    = item.getDesignation();
            dto.quantity       = item.getQuantity();
            dto.unitPriceHT    = item.getUnitPriceHT();
            dto.discountPercent = item.getDiscountPercent();
            dto.totalPriceHT   = item.getTotalPriceHT();
            dto.category       = item.getCategory() != null ? item.getCategory().name() : null;
            dto.status         = item.getStatus()   != null ? item.getStatus().name()   : null;
            return dto;
        }

        public int    getLineNumber()     { return lineNumber; }
        public String getDesignation()     { return designation; }
        public int    getQuantity()       { return quantity; }
        public Double getUnitPriceHT()    { return unitPriceHT; }
        public Double getDiscountPercent(){ return discountPercent; }
        public Double getTotalPriceHT()   { return totalPriceHT; }
        public String getCategory()       { return category; }
        public String getStatus()         { return status; }
    }

    // =========================================================================
    // Factory
    // =========================================================================

    /**
     * Construit la réponse à partir d'un DraftQuote.
     */
    public static DevisResponse from(DraftQuote draft) {
        DevisResponse r = new DevisResponse();

        r.quoteNumber    = draft.getQuoteNumber();
        r.createdAt      = draft.getCreatedAt();
        r.validUntil     = draft.getValidUntil();
        r.status         = draft.getStatus()   != null ? draft.getStatus().getLabel()   : null;
        r.priority       = draft.getPriority() != null ? draft.getPriority().getLabel() : null;
        r.subject        = draft.getSubject();
        r.clientNom      = draft.getClientNom();
        r.clientEmail    = draft.getClientEmail();
        r.emailOriginal  = draft.getEmailOriginal();

        r.totalHT        = draft.getTotalHT();
        r.totalTVA       = draft.getTotalTVA();
        r.tvaRate        = draft.getTvaRate();
        r.totalTTC       = draft.getTotalTTC();
        r.clientBudget   = draft.getClientBudget();
        r.budgetRespected = draft.isBudgetRespected();

        r.requestedDeliveryDate = draft.getRequestedDeliveryDate();
        r.deliveryFees          = draft.getDeliveryFees();
        r.deliveryIncluded      = draft.isDeliveryIncluded();
        r.warranty              = draft.getWarranty();

        r.confidence       = (int) Math.round(draft.getConfidence() * 100);
        r.requiredActions  = draft.getRequiredActions();
        r.recommendations  = draft.getRecommendations();
        r.warnings         = draft.getWarnings();
        r.inconsistencies  = draft.getInconsistencies();

        r.items = draft.getItems().stream()
                .map(QuoteItemDto::from)
                .collect(Collectors.toList());

        return r;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public String        getQuoteNumber()          { return quoteNumber; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public LocalDate     getValidUntil()           { return validUntil; }
    public String        getStatus()               { return status; }
    public String        getPriority()             { return priority; }
    public String        getSubject()              { return subject; }
    public String        getClientNom()            { return clientNom; }
    public String        getClientEmail()          { return clientEmail; }
    public String        getEmailOriginal()        { return emailOriginal; }
    public Double        getTotalHT()              { return totalHT; }
    public Double        getTotalTVA()             { return totalTVA; }
    public double        getTvaRate()              { return tvaRate; }
    public Double        getTotalTTC()             { return totalTTC; }
    public Double        getClientBudget()         { return clientBudget; }
    public Boolean       isBudgetRespected()       { return budgetRespected; }
    public LocalDate     getRequestedDeliveryDate(){ return requestedDeliveryDate; }
    public Double        getDeliveryFees()         { return deliveryFees; }
    public boolean       isDeliveryIncluded()      { return deliveryIncluded; }
    public String        getWarranty()             { return warranty; }
    public int           getConfidence()           { return confidence; }
    public List<String>  getRequiredActions()      { return requiredActions; }
    public List<String>  getRecommendations()      { return recommendations; }
    public List<String>  getWarnings()             { return warnings; }
    public List<String>  getInconsistencies()      { return inconsistencies; }
    public List<QuoteItemDto> getItems()           { return items; }
}
