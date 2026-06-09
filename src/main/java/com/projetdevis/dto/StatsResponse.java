package com.projetdevis.dto;

import java.util.Map;

/**
 * Réponse GET /api/stats — tableau de bord global de l'application.
 */
public class StatsResponse {

    private long   totalDevis;
    private double montantTotalHT;
    private double montantMoyenHT;
    private Map<String, Long> devisParStatut;

    private long   totalClients;
    private long   totalProduits;
    private double tauxConfidenceMoyen;
    private double tauxValidation;
    private double tempsMoyenValidationMinutes;

    private long   totalCorrections;
    private double tauxPrecisionIa;

    // === Getters / Setters ===

    public long   getTotalDevis()         { return totalDevis; }
    public void   setTotalDevis(long v)   { this.totalDevis = v; }

    public double getMontantTotalHT()           { return montantTotalHT; }
    public void   setMontantTotalHT(double v)   { this.montantTotalHT = v; }

    public double getMontantMoyenHT()           { return montantMoyenHT; }
    public void   setMontantMoyenHT(double v)   { this.montantMoyenHT = v; }

    public Map<String, Long> getDevisParStatut()               { return devisParStatut; }
    public void              setDevisParStatut(Map<String, Long> v) { this.devisParStatut = v; }

    public long   getTotalClients()        { return totalClients; }
    public void   setTotalClients(long v)  { this.totalClients = v; }

    public long   getTotalProduits()       { return totalProduits; }
    public void   setTotalProduits(long v) { this.totalProduits = v; }

    public double getTauxConfidenceMoyen()         { return tauxConfidenceMoyen; }
    public void   setTauxConfidenceMoyen(double v) { this.tauxConfidenceMoyen = v; }

    public double getTauxValidation()              { return tauxValidation; }
    public void   setTauxValidation(double v)      { this.tauxValidation = v; }

    public double getTempsMoyenValidationMinutes()         { return tempsMoyenValidationMinutes; }
    public void   setTempsMoyenValidationMinutes(double v) { this.tempsMoyenValidationMinutes = v; }

    public long   getTotalCorrections()          { return totalCorrections; }
    public void   setTotalCorrections(long v)    { this.totalCorrections = v; }

    public double getTauxPrecisionIa()           { return tauxPrecisionIa; }
    public void   setTauxPrecisionIa(double v)   { this.tauxPrecisionIa = v; }
}
