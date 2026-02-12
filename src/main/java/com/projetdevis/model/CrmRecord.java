package com.projetdevis.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Représente un enregistrement CRM pour un devis validé.
 *
 * Cette classe centralise toutes les informations nécessaires à l'intégration CRM :
 * - Référence au client
 * - Référence au devis validé
 * - Données de l'opportunité commerciale
 * - Historique des actions
 * - Métriques de suivi
 *
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * @author BMAD Pipeline - Étape 7
 * @version 1.0
 */
public class CrmRecord {

    // === ÉNUMÉRATIONS ===

    /**
     * Statut de l'opportunité dans le CRM.
     */
    public enum OpportunityStatus {
        NOUVELLE("Nouvelle", "Opportunité nouvellement créée", 10),
        QUALIFICATION("Qualification", "En cours de qualification", 20),
        PROPOSITION("Proposition", "Devis envoyé au client", 40),
        NEGOCIATION("Négociation", "En négociation avec le client", 60),
        GAGNEE("Gagnée", "Commande confirmée", 100),
        PERDUE("Perdue", "Opportunité perdue", 0),
        ABANDONNEE("Abandonnée", "Opportunité abandonnée", 0);

        private final String label;
        private final String description;
        private final int probability;

        OpportunityStatus(String label, String description, int probability) {
            this.label = label;
            this.description = description;
            this.probability = probability;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        public int getProbability() {
            return probability;
        }
    }

    /**
     * Type d'action CRM.
     */
    public enum ActionType {
        CREATION("Création", "Création de l'enregistrement"),
        ENVOI_DEVIS("Envoi devis", "Devis envoyé au client"),
        RELANCE("Relance", "Relance client"),
        APPEL("Appel", "Appel téléphonique"),
        EMAIL("Email", "Email envoyé"),
        REUNION("Réunion", "Réunion avec le client"),
        NEGOCIATION("Négociation", "Session de négociation"),
        MODIFICATION("Modification", "Modification du devis"),
        CLOTURE("Clôture", "Clôture de l'opportunité");

        private final String label;
        private final String description;

        ActionType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }
    }

    // === CLASSE INTERNE : ACTION CRM ===

    /**
     * Représente une action/interaction enregistrée dans le CRM.
     */
    public static class CrmAction {
        private LocalDateTime timestamp;
        private ActionType type;
        private String description;
        private String operateur;
        private String resultat;

        public CrmAction() {
            this.timestamp = LocalDateTime.now();
        }

        public CrmAction(ActionType type, String description) {
            this();
            this.type = type;
            this.description = description;
        }

        public CrmAction(ActionType type, String description, String operateur) {
            this(type, description);
            this.operateur = operateur;
        }

        // Getters et Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public ActionType getType() { return type; }
        public void setType(ActionType type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getOperateur() { return operateur; }
        public void setOperateur(String operateur) { this.operateur = operateur; }

        public String getResultat() { return resultat; }
        public void setResultat(String resultat) { this.resultat = resultat; }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s%s",
                getFormattedTimestamp(),
                type.getLabel(),
                description,
                operateur != null ? " (par " + operateur + ")" : "");
        }
    }

    // === ATTRIBUTS IDENTIFICATION ===

    /** Identifiant unique de l'enregistrement CRM */
    private String recordId;

    /** Référence à l'opportunité (si générée par CRM externe) */
    private String opportunityId;

    /** Numéro du devis associé */
    private String quoteNumber;

    /** Identifiant du client */
    private String clientId;

    // === ATTRIBUTS OPPORTUNITÉ ===

    /** Nom de l'opportunité */
    private String opportunityName;

    /** Description de l'opportunité */
    private String description;

    /** Statut de l'opportunité */
    private OpportunityStatus status;

    /** Montant potentiel HT */
    private Double montantHT;

    /** Montant potentiel TTC */
    private Double montantTTC;

    /** Probabilité de conversion (%) */
    private int probabiliteConversion;

    /** Date de clôture prévue */
    private LocalDateTime dateCloturePrevue;

    /** Source de l'opportunité */
    private String source;

    /** Campagne marketing associée */
    private String campagne;

    // === ATTRIBUTS SUIVI ===

    /** Date de création */
    private LocalDateTime dateCreation;

    /** Date de dernière modification */
    private LocalDateTime dateModification;

    /** Date d'envoi du devis */
    private LocalDateTime dateEnvoiDevis;

    /** Date de dernière relance */
    private LocalDateTime dateDerniereRelance;

    /** Nombre de relances effectuées */
    private int nombreRelances;

    /** Commercial responsable */
    private String commercialResponsable;

    /** Historique des actions */
    private List<CrmAction> actions;

    /** Tags/étiquettes */
    private List<String> tags;

    /** Données personnalisées (pour intégration CRM externe) */
    private Map<String, String> customFields;

    // === ATTRIBUTS RÉFÉRENCE OBJETS ===

    /** Client associé */
    private Client client;

    /** Devis validé associé */
    private ValidatedQuote validatedQuote;

    // === COMPTEUR POUR ID UNIQUE ===

    private static int counter = 0;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public CrmRecord() {
        this.recordId = generateRecordId();
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.status = OpportunityStatus.NOUVELLE;
        this.probabiliteConversion = 10;
        this.nombreRelances = 0;
        this.actions = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.customFields = new HashMap<>();

        // Action de création automatique
        addAction(new CrmAction(ActionType.CREATION, "Création de l'enregistrement CRM"));
    }

    /**
     * Constructeur avec devis validé.
     *
     * @param validatedQuote Devis validé source
     */
    public CrmRecord(ValidatedQuote validatedQuote) {
        this();
        this.validatedQuote = validatedQuote;

        if (validatedQuote != null && validatedQuote.getDraft() != null) {
            DraftQuote draft = validatedQuote.getDraft();
            this.quoteNumber = draft.getQuoteNumber();
            this.opportunityName = draft.getSubject();
            this.montantHT = draft.getTotalHT();
            this.montantTTC = draft.getTotalTTC();
            this.commercialResponsable = validatedQuote.getValidatorName();

            // Statut initial
            if (validatedQuote.isApproved()) {
                this.status = OpportunityStatus.PROPOSITION;
                this.probabiliteConversion = 40;
            }
        }
    }

    /**
     * Constructeur complet.
     *
     * @param validatedQuote Devis validé
     * @param client Client associé
     */
    public CrmRecord(ValidatedQuote validatedQuote, Client client) {
        this(validatedQuote);
        this.client = client;
        if (client != null) {
            this.clientId = client.getClientId();
        }
    }

    // === GÉNÉRATION D'IDENTIFIANTS ===

    /**
     * Génère un identifiant d'enregistrement unique.
     */
    private static synchronized String generateRecordId() {
        counter++;
        return String.format("CRM-%d-%04d",
            System.currentTimeMillis() % 100000, counter);
    }

    // === GETTERS ET SETTERS ===

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
        markModified();
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
        markModified();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        markModified();
    }

    public String getOpportunityName() {
        return opportunityName;
    }

    public void setOpportunityName(String opportunityName) {
        this.opportunityName = opportunityName;
        markModified();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        markModified();
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public void setStatus(OpportunityStatus status) {
        this.status = status;
        this.probabiliteConversion = status.getProbability();
        markModified();
    }

    public Double getMontantHT() {
        return montantHT;
    }

    public void setMontantHT(Double montantHT) {
        this.montantHT = montantHT;
        markModified();
    }

    public Double getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(Double montantTTC) {
        this.montantTTC = montantTTC;
        markModified();
    }

    public int getProbabiliteConversion() {
        return probabiliteConversion;
    }

    public void setProbabiliteConversion(int probabiliteConversion) {
        this.probabiliteConversion = Math.max(0, Math.min(100, probabiliteConversion));
        markModified();
    }

    public LocalDateTime getDateCloturePrevue() {
        return dateCloturePrevue;
    }

    public void setDateCloturePrevue(LocalDateTime dateCloturePrevue) {
        this.dateCloturePrevue = dateCloturePrevue;
        markModified();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
        markModified();
    }

    public String getCampagne() {
        return campagne;
    }

    public void setCampagne(String campagne) {
        this.campagne = campagne;
        markModified();
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public LocalDateTime getDateEnvoiDevis() {
        return dateEnvoiDevis;
    }

    public void setDateEnvoiDevis(LocalDateTime dateEnvoiDevis) {
        this.dateEnvoiDevis = dateEnvoiDevis;
        markModified();
    }

    public LocalDateTime getDateDerniereRelance() {
        return dateDerniereRelance;
    }

    public void setDateDerniereRelance(LocalDateTime dateDerniereRelance) {
        this.dateDerniereRelance = dateDerniereRelance;
    }

    public int getNombreRelances() {
        return nombreRelances;
    }

    public void setNombreRelances(int nombreRelances) {
        this.nombreRelances = nombreRelances;
    }

    public String getCommercialResponsable() {
        return commercialResponsable;
    }

    public void setCommercialResponsable(String commercialResponsable) {
        this.commercialResponsable = commercialResponsable;
        markModified();
    }

    public List<CrmAction> getActions() {
        return actions;
    }

    public void setActions(List<CrmAction> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public void addTag(String tag) {
        if (tag != null && !tag.isBlank() && !tags.contains(tag)) {
            this.tags.add(tag);
            markModified();
        }
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields != null ? customFields : new HashMap<>();
    }

    public void setCustomField(String key, String value) {
        this.customFields.put(key, value);
        markModified();
    }

    public String getCustomField(String key) {
        return customFields.get(key);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            this.clientId = client.getClientId();
        }
        markModified();
    }

    public ValidatedQuote getValidatedQuote() {
        return validatedQuote;
    }

    public void setValidatedQuote(ValidatedQuote validatedQuote) {
        this.validatedQuote = validatedQuote;
        markModified();
    }

    // === MÉTHODES D'ACTION ===

    /**
     * Marque la fiche comme modifiée.
     */
    private void markModified() {
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Ajoute une action à l'historique.
     *
     * @param action Action à ajouter
     */
    public void addAction(CrmAction action) {
        if (action != null) {
            this.actions.add(action);
            markModified();
        }
    }

    /**
     * Enregistre l'envoi du devis.
     *
     * @param operateur Opérateur ayant envoyé le devis
     */
    public void enregistrerEnvoiDevis(String operateur) {
        this.dateEnvoiDevis = LocalDateTime.now();
        this.status = OpportunityStatus.PROPOSITION;
        this.probabiliteConversion = 40;

        CrmAction action = new CrmAction(
            ActionType.ENVOI_DEVIS,
            "Devis " + quoteNumber + " envoyé au client",
            operateur
        );
        addAction(action);
    }

    /**
     * Enregistre une relance.
     *
     * @param operateur Opérateur ayant fait la relance
     * @param commentaire Commentaire sur la relance
     */
    public void enregistrerRelance(String operateur, String commentaire) {
        this.dateDerniereRelance = LocalDateTime.now();
        this.nombreRelances++;

        CrmAction action = new CrmAction(
            ActionType.RELANCE,
            commentaire != null ? commentaire : "Relance n°" + nombreRelances,
            operateur
        );
        addAction(action);
    }

    /**
     * Marque l'opportunité comme gagnée.
     *
     * @param operateur Opérateur
     * @param commentaire Commentaire
     */
    public void marquerGagnee(String operateur, String commentaire) {
        this.status = OpportunityStatus.GAGNEE;
        this.probabiliteConversion = 100;

        CrmAction action = new CrmAction(
            ActionType.CLOTURE,
            "Opportunité gagnée" + (commentaire != null ? " - " + commentaire : ""),
            operateur
        );
        action.setResultat("GAGNEE");
        addAction(action);

        // Mettre à jour le client si présent
        if (client != null && montantHT != null) {
            client.enregistrerCommande(montantHT);
            client.addDevis(quoteNumber);
        }
    }

    /**
     * Marque l'opportunité comme perdue.
     *
     * @param operateur Opérateur
     * @param raison Raison de la perte
     */
    public void marquerPerdue(String operateur, String raison) {
        this.status = OpportunityStatus.PERDUE;
        this.probabiliteConversion = 0;

        CrmAction action = new CrmAction(
            ActionType.CLOTURE,
            "Opportunité perdue - Raison: " + (raison != null ? raison : "Non spécifiée"),
            operateur
        );
        action.setResultat("PERDUE");
        addAction(action);
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Vérifie si l'opportunité est ouverte (non clôturée).
     *
     * @return true si ouverte
     */
    public boolean isOpen() {
        return status != OpportunityStatus.GAGNEE &&
               status != OpportunityStatus.PERDUE &&
               status != OpportunityStatus.ABANDONNEE;
    }

    /**
     * Vérifie si l'opportunité est gagnée.
     *
     * @return true si gagnée
     */
    public boolean isWon() {
        return status == OpportunityStatus.GAGNEE;
    }

    /**
     * Vérifie si le devis a été envoyé.
     *
     * @return true si envoyé
     */
    public boolean isDevisEnvoye() {
        return dateEnvoiDevis != null;
    }

    /**
     * Calcule le montant pondéré (montant * probabilité).
     *
     * @return Montant pondéré
     */
    public Double getMontantPondere() {
        if (montantHT == null) return null;
        return montantHT * probabiliteConversion / 100.0;
    }

    /**
     * Calcule le nombre de jours depuis la création.
     *
     * @return Nombre de jours
     */
    public long getAgeEnJours() {
        if (dateCreation == null) return 0;
        return java.time.Duration.between(dateCreation, LocalDateTime.now()).toDays();
    }

    /**
     * Calcule le nombre de jours depuis l'envoi du devis.
     *
     * @return Nombre de jours ou -1 si non envoyé
     */
    public long getJoursDepuisEnvoi() {
        if (dateEnvoiDevis == null) return -1;
        return java.time.Duration.between(dateEnvoiDevis, LocalDateTime.now()).toDays();
    }

    /**
     * Vérifie si une relance est nécessaire (> 7 jours sans action).
     *
     * @return true si relance nécessaire
     */
    public boolean needsFollowUp() {
        if (!isOpen()) return false;
        if (dateEnvoiDevis == null) return false;

        LocalDateTime lastAction = dateDerniereRelance != null ?
            dateDerniereRelance : dateEnvoiDevis;

        long daysSinceAction = java.time.Duration.between(lastAction, LocalDateTime.now()).toDays();
        return daysSinceAction > 7;
    }

    // === MÉTHODES DE FORMATAGE ===

    /**
     * Retourne la date de création formatée.
     *
     * @return Date formatée
     */
    public String getFormattedDateCreation() {
        if (dateCreation == null) return "N/A";
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Retourne le montant formaté.
     *
     * @return Montant formaté
     */
    public String getFormattedMontantHT() {
        if (montantHT == null) return "N/A";
        return String.format("%,.2f € HT", montantHT);
    }

    /**
     * Génère un résumé de l'enregistrement.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== ENREGISTREMENT CRM ===\n");
        sb.append("ID: ").append(recordId).append("\n");
        sb.append("Devis: ").append(quoteNumber).append("\n");
        sb.append("Client: ").append(clientId).append("\n");
        sb.append("\n");

        sb.append("Opportunité: ").append(opportunityName).append("\n");
        sb.append("Statut: ").append(status.getLabel()).append("\n");
        sb.append("Probabilité: ").append(probabiliteConversion).append("%\n");
        sb.append("Montant: ").append(getFormattedMontantHT()).append("\n");
        sb.append("\n");

        sb.append("Commercial: ").append(commercialResponsable).append("\n");
        sb.append("Créé le: ").append(getFormattedDateCreation()).append("\n");

        if (dateEnvoiDevis != null) {
            sb.append("Envoyé le: ").append(
                dateEnvoiDevis.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }

        sb.append("\nActions: ").append(actions.size()).append(" enregistrées\n");
        if (nombreRelances > 0) {
            sb.append("Relances: ").append(nombreRelances).append("\n");
        }

        if (!tags.isEmpty()) {
            sb.append("\nTags: ").append(String.join(", ", tags)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Génère l'historique des actions.
     *
     * @return Historique textuel
     */
    public String getActionHistory() {
        if (actions.isEmpty()) {
            return "Aucune action enregistrée.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== HISTORIQUE DES ACTIONS ===\n\n");

        int num = 1;
        for (CrmAction action : actions) {
            sb.append(String.format("%d. %s\n", num++, action.toString()));
        }

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrmRecord crmRecord = (CrmRecord) o;
        return Objects.equals(recordId, crmRecord.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "CrmRecord {\n" +
               "  recordId: \"" + recordId + "\"\n" +
               "  quoteNumber: \"" + quoteNumber + "\"\n" +
               "  clientId: \"" + clientId + "\"\n" +
               "  status: " + status.getLabel() + "\n" +
               "  montantHT: " + getFormattedMontantHT() + "\n" +
               "  probabilite: " + probabiliteConversion + "%\n" +
               "  actions: " + actions.size() + "\n" +
               "}";
    }
}
