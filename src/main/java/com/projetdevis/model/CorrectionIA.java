package com.projetdevis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Trace une correction apportée par le commercial sur un article généré par l'IA.
 * Permet de mesurer la précision des extractions et d'identifier les champs souvent mal détectés.
 */
@Entity
@Table(name = "corrections_ia")
public class CorrectionIA {

    public enum ChampCorrige {
        QUANTITE, PRIX_UNITAIRE, DESIGNATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String quoteNumber;

    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    private ChampCorrige champ;

    @Column(length = 500)
    private String valeurIa;

    @Column(length = 500)
    private String valeurCommerciale;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // === Getters / Setters ===

    public Long getId() { return id; }

    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String v) { this.quoteNumber = v; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer v) { this.lineNumber = v; }

    public ChampCorrige getChamp() { return champ; }
    public void setChamp(ChampCorrige v) { this.champ = v; }

    public String getValeurIa() { return valeurIa; }
    public void setValeurIa(String v) { this.valeurIa = v; }

    public String getValeurCommerciale() { return valeurCommerciale; }
    public void setValeurCommerciale(String v) { this.valeurCommerciale = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
