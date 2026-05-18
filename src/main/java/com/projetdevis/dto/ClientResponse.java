package com.projetdevis.dto;

import com.projetdevis.model.Client;

import java.util.List;

/**
 * DTO de réponse pour GET /api/clients.
 * N'expose que les champs utiles au frontend — pas les champs comptables internes.
 */
public class ClientResponse {

    private String clientId;
    private String raisonSociale;
    private String emailOrigine;
    private String segment;
    private String status;
    private int    nombreCommandes;
    private List<String> historiqueDevis;

    public static ClientResponse from(Client c) {
        ClientResponse r = new ClientResponse();
        r.clientId        = c.getClientId();
        r.raisonSociale   = c.getRaisonSociale();
        r.emailOrigine    = c.getEmailOrigine();
        r.segment         = c.getSegment()  != null ? c.getSegment().getLabel()  : null;
        r.status          = c.getStatus()   != null ? c.getStatus().getLabel()   : null;
        r.nombreCommandes = c.getNombreCommandes();
        r.historiqueDevis = c.getHistoriqueDevis();
        return r;
    }

    public String       getClientId()       { return clientId; }
    public String       getRaisonSociale()  { return raisonSociale; }
    public String       getEmailOrigine()   { return emailOrigine; }
    public String       getSegment()        { return segment; }
    public String       getStatus()         { return status; }
    public int          getNombreCommandes(){ return nombreCommandes; }
    public List<String> getHistoriqueDevis(){ return historiqueDevis; }
}
