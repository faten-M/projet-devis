package com.projetdevis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente une fiche client dans le système CRM.
 *
 * Cette classe contient toutes les informations relatives à un client :
 * - Identification (numéro, nom, SIRET)
 * - Coordonnées (adresse, téléphone, email)
 * - Informations commerciales (segment, commercial attitré)
 * - Historique des interactions
 *
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * @author BMAD Pipeline - Étape 7
 * @version 1.0
 */
public class Client {

    // === ÉNUMÉRATIONS ===

    /**
     * Segment client (classification commerciale).
     */
    public enum Segment {
        PROSPECT("Prospect", "Nouveau contact, jamais commandé"),
        TPE("TPE", "Très petite entreprise (< 10 salariés)"),
        PME("PME", "Petite et moyenne entreprise (10-250 salariés)"),
        ETI("ETI", "Entreprise de taille intermédiaire (250-5000)"),
        GRAND_COMPTE("Grand compte", "Grande entreprise (> 5000 salariés)"),
        ADMINISTRATION("Administration", "Secteur public"),
        ASSOCIATION("Association", "Secteur associatif");

        private final String label;
        private final String description;

        Segment(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Statut du client.
     */
    public enum Status {
        ACTIF("Actif", "Client actif avec commandes récentes"),
        INACTIF("Inactif", "Pas de commande depuis plus d'un an"),
        BLOQUE("Bloqué", "Client bloqué (impayés, litige)"),
        ARCHIVE("Archivé", "Client archivé");

        private final String label;
        private final String description;

        Status(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Type de contact.
     */
    public enum ContactType {
        PRINCIPAL("Contact principal"),
        FACTURATION("Contact facturation"),
        LIVRAISON("Contact livraison"),
        TECHNIQUE("Contact technique"),
        DECISION("Décisionnaire");

        private final String label;

        ContactType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // === CLASSE INTERNE : CONTACT ===

    /**
     * Représente un contact chez le client.
     */
    public static class Contact {
        private String nom;
        private String prenom;
        private String fonction;
        private String email;
        private String telephone;
        private String mobile;
        private ContactType type;
        private boolean principal;

        public Contact() {
            this.type = ContactType.PRINCIPAL;
            this.principal = false;
        }

        public Contact(String nom, String prenom, String email) {
            this();
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
        }

        // Getters et Setters
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }

        public String getFonction() { return fonction; }
        public void setFonction(String fonction) { this.fonction = fonction; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }

        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }

        public ContactType getType() { return type; }
        public void setType(ContactType type) { this.type = type; }

        public boolean isPrincipal() { return principal; }
        public void setPrincipal(boolean principal) { this.principal = principal; }

        public String getNomComplet() {
            if (prenom != null && nom != null) {
                return prenom + " " + nom;
            }
            return nom != null ? nom : prenom;
        }

        @Override
        public String toString() {
            return getNomComplet() + (fonction != null ? " (" + fonction + ")" : "");
        }
    }

    // === CLASSE INTERNE : ADRESSE ===

    /**
     * Représente une adresse.
     */
    public static class Adresse {
        private String ligne1;
        private String ligne2;
        private String codePostal;
        private String ville;
        private String pays;

        public Adresse() {
            this.pays = "France";
        }

        public Adresse(String ligne1, String codePostal, String ville) {
            this();
            this.ligne1 = ligne1;
            this.codePostal = codePostal;
            this.ville = ville;
        }

        // Getters et Setters
        public String getLigne1() { return ligne1; }
        public void setLigne1(String ligne1) { this.ligne1 = ligne1; }

        public String getLigne2() { return ligne2; }
        public void setLigne2(String ligne2) { this.ligne2 = ligne2; }

        public String getCodePostal() { return codePostal; }
        public void setCodePostal(String codePostal) { this.codePostal = codePostal; }

        public String getVille() { return ville; }
        public void setVille(String ville) { this.ville = ville; }

        public String getPays() { return pays; }
        public void setPays(String pays) { this.pays = pays; }

        public String getAdresseComplete() {
            StringBuilder sb = new StringBuilder();
            if (ligne1 != null) sb.append(ligne1);
            if (ligne2 != null) sb.append(", ").append(ligne2);
            if (codePostal != null || ville != null) {
                sb.append(", ");
                if (codePostal != null) sb.append(codePostal).append(" ");
                if (ville != null) sb.append(ville);
            }
            if (pays != null && !pays.equals("France")) {
                sb.append(", ").append(pays);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return getAdresseComplete();
        }
    }

    // === ATTRIBUTS IDENTIFICATION ===

    /** Identifiant unique du client */
    private String clientId;

    /** Numéro de compte client */
    private String numeroCompte;

    /** Raison sociale */
    private String raisonSociale;

    /** Nom commercial */
    private String nomCommercial;

    /** Numéro SIRET */
    private String siret;

    /** Numéro TVA intracommunautaire */
    private String numeroTva;

    /** Code APE/NAF */
    private String codeApe;

    // === ATTRIBUTS COORDONNÉES ===

    /** Adresse du siège */
    private Adresse adresseSiege;

    /** Adresse de facturation */
    private Adresse adresseFacturation;

    /** Adresse de livraison */
    private Adresse adresseLivraison;

    /** Téléphone principal */
    private String telephone;

    /** Site web */
    private String siteWeb;

    /** Liste des contacts */
    private List<Contact> contacts;

    // === ATTRIBUTS COMMERCIAUX ===

    /** Segment client */
    private Segment segment;

    /** Statut client */
    private Status status;

    /** Commercial attitré */
    private String commercialAttitre;

    /** Conditions de paiement spécifiques */
    private String conditionsPaiement;

    /** Remise standard accordée (%) */
    private Double remiseStandard;

    /** Plafond de crédit */
    private Double plafondCredit;

    /** Encours actuel */
    private Double encoursActuel;

    // === ATTRIBUTS HISTORIQUE ===

    /** Date de création de la fiche */
    private LocalDateTime dateCreation;

    /** Date de dernière modification */
    private LocalDateTime dateModification;

    /** Date de première commande */
    private LocalDate datePremiereCommande;

    /** Date de dernière commande */
    private LocalDate dateDerniereCommande;

    /** Chiffre d'affaires cumulé */
    private Double chiffreAffairesCumule;

    /** Nombre de commandes */
    private int nombreCommandes;

    /** Liste des numéros de devis */
    private List<String> historiqueDevis;

    /** Notes internes */
    private List<String> notes;

    // === ATTRIBUTS ORIGINE ===

    /** Source du lead (email, téléphone, salon, etc.) */
    private String sourceOrigine;

    /** Email d'origine (si créé à partir d'un email) */
    private String emailOrigine;

    // === COMPTEUR POUR ID UNIQUE ===

    private static int counter = 0;

    // === CONSTRUCTEURS ===

    /**
     * Constructeur par défaut.
     */
    public Client() {
        this.clientId = generateClientId();
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.segment = Segment.PROSPECT;
        this.status = Status.ACTIF;
        this.contacts = new ArrayList<>();
        this.historiqueDevis = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.nombreCommandes = 0;
        this.chiffreAffairesCumule = 0.0;
        this.encoursActuel = 0.0;
    }

    /**
     * Constructeur avec raison sociale.
     *
     * @param raisonSociale Raison sociale du client
     */
    public Client(String raisonSociale) {
        this();
        this.raisonSociale = raisonSociale;
        this.numeroCompte = generateNumeroCompte();
    }

    /**
     * Constructeur complet.
     *
     * @param raisonSociale Raison sociale
     * @param email Email du contact principal
     */
    public Client(String raisonSociale, String email) {
        this(raisonSociale);
        if (email != null) {
            Contact contact = new Contact();
            contact.setEmail(email);
            contact.setPrincipal(true);
            this.contacts.add(contact);
        }
    }

    // === GÉNÉRATION D'IDENTIFIANTS ===

    /**
     * Génère un identifiant client unique.
     */
    private static synchronized String generateClientId() {
        counter++;
        return String.format("CLI-%d-%04d",
            System.currentTimeMillis() % 100000, counter);
    }

    /**
     * Génère un numéro de compte client.
     */
    private String generateNumeroCompte() {
        return String.format("C%d%04d",
            LocalDate.now().getYear() % 100,
            (int) (Math.random() * 10000));
    }

    // === GETTERS ET SETTERS ===

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
        markModified();
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
        markModified();
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
        markModified();
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
        markModified();
    }

    public String getNumeroTva() {
        return numeroTva;
    }

    public void setNumeroTva(String numeroTva) {
        this.numeroTva = numeroTva;
        markModified();
    }

    public String getCodeApe() {
        return codeApe;
    }

    public void setCodeApe(String codeApe) {
        this.codeApe = codeApe;
        markModified();
    }

    public Adresse getAdresseSiege() {
        return adresseSiege;
    }

    public void setAdresseSiege(Adresse adresseSiege) {
        this.adresseSiege = adresseSiege;
        markModified();
    }

    public Adresse getAdresseFacturation() {
        return adresseFacturation;
    }

    public void setAdresseFacturation(Adresse adresseFacturation) {
        this.adresseFacturation = adresseFacturation;
        markModified();
    }

    public Adresse getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(Adresse adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
        markModified();
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
        markModified();
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
        markModified();
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<>();
        markModified();
    }

    public void addContact(Contact contact) {
        if (contact != null) {
            // Si c'est le premier contact, le définir comme principal
            if (contacts.isEmpty()) {
                contact.setPrincipal(true);
            }
            this.contacts.add(contact);
            markModified();
        }
    }

    public Contact getContactPrincipal() {
        return contacts.stream()
            .filter(Contact::isPrincipal)
            .findFirst()
            .orElse(contacts.isEmpty() ? null : contacts.get(0));
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
        markModified();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        markModified();
    }

    public String getCommercialAttitre() {
        return commercialAttitre;
    }

    public void setCommercialAttitre(String commercialAttitre) {
        this.commercialAttitre = commercialAttitre;
        markModified();
    }

    public String getConditionsPaiement() {
        return conditionsPaiement;
    }

    public void setConditionsPaiement(String conditionsPaiement) {
        this.conditionsPaiement = conditionsPaiement;
        markModified();
    }

    public Double getRemiseStandard() {
        return remiseStandard;
    }

    public void setRemiseStandard(Double remiseStandard) {
        this.remiseStandard = remiseStandard;
        markModified();
    }

    public Double getPlafondCredit() {
        return plafondCredit;
    }

    public void setPlafondCredit(Double plafondCredit) {
        this.plafondCredit = plafondCredit;
        markModified();
    }

    public Double getEncoursActuel() {
        return encoursActuel;
    }

    public void setEncoursActuel(Double encoursActuel) {
        this.encoursActuel = encoursActuel;
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

    public LocalDate getDatePremiereCommande() {
        return datePremiereCommande;
    }

    public void setDatePremiereCommande(LocalDate datePremiereCommande) {
        this.datePremiereCommande = datePremiereCommande;
    }

    public LocalDate getDateDerniereCommande() {
        return dateDerniereCommande;
    }

    public void setDateDerniereCommande(LocalDate dateDerniereCommande) {
        this.dateDerniereCommande = dateDerniereCommande;
    }

    public Double getChiffreAffairesCumule() {
        return chiffreAffairesCumule;
    }

    public void setChiffreAffairesCumule(Double chiffreAffairesCumule) {
        this.chiffreAffairesCumule = chiffreAffairesCumule;
    }

    public int getNombreCommandes() {
        return nombreCommandes;
    }

    public void setNombreCommandes(int nombreCommandes) {
        this.nombreCommandes = nombreCommandes;
    }

    public List<String> getHistoriqueDevis() {
        return historiqueDevis;
    }

    public void setHistoriqueDevis(List<String> historiqueDevis) {
        this.historiqueDevis = historiqueDevis != null ? historiqueDevis : new ArrayList<>();
    }

    public void addDevis(String numeroDevis) {
        if (numeroDevis != null && !historiqueDevis.contains(numeroDevis)) {
            this.historiqueDevis.add(numeroDevis);
        }
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
    }

    public void addNote(String note) {
        if (note != null && !note.isBlank()) {
            String timestamped = String.format("[%s] %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                note);
            this.notes.add(timestamped);
            markModified();
        }
    }

    public String getSourceOrigine() {
        return sourceOrigine;
    }

    public void setSourceOrigine(String sourceOrigine) {
        this.sourceOrigine = sourceOrigine;
    }

    public String getEmailOrigine() {
        return emailOrigine;
    }

    public void setEmailOrigine(String emailOrigine) {
        this.emailOrigine = emailOrigine;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Marque la fiche comme modifiée.
     */
    private void markModified() {
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Vérifie si le client est un prospect.
     *
     * @return true si prospect
     */
    public boolean isProspect() {
        return segment == Segment.PROSPECT || nombreCommandes == 0;
    }

    /**
     * Vérifie si le client a un email.
     *
     * @return true si un email est disponible
     */
    public boolean hasEmail() {
        Contact principal = getContactPrincipal();
        return principal != null && principal.getEmail() != null;
    }

    /**
     * Retourne l'email du contact principal.
     *
     * @return Email ou null
     */
    public String getEmail() {
        Contact principal = getContactPrincipal();
        return principal != null ? principal.getEmail() : null;
    }

    /**
     * Vérifie si le client peut commander (non bloqué, crédit ok).
     *
     * @return true si le client peut commander
     */
    public boolean canOrder() {
        if (status == Status.BLOQUE || status == Status.ARCHIVE) {
            return false;
        }
        if (plafondCredit != null && encoursActuel != null) {
            return encoursActuel < plafondCredit;
        }
        return true;
    }

    /**
     * Calcule le crédit disponible.
     *
     * @return Crédit disponible ou null si pas de plafond
     */
    public Double getCreditDisponible() {
        if (plafondCredit == null) return null;
        double encours = encoursActuel != null ? encoursActuel : 0;
        return Math.max(0, plafondCredit - encours);
    }

    /**
     * Retourne le nom d'affichage du client.
     *
     * @return Nom commercial ou raison sociale
     */
    public String getNomAffichage() {
        return nomCommercial != null ? nomCommercial : raisonSociale;
    }

    /**
     * Enregistre une nouvelle commande.
     *
     * @param montantHT Montant HT de la commande
     */
    public void enregistrerCommande(double montantHT) {
        this.nombreCommandes++;
        this.chiffreAffairesCumule = (chiffreAffairesCumule != null ? chiffreAffairesCumule : 0) + montantHT;
        this.dateDerniereCommande = LocalDate.now();
        if (datePremiereCommande == null) {
            this.datePremiereCommande = LocalDate.now();
        }
        if (segment == Segment.PROSPECT) {
            this.segment = Segment.TPE; // Par défaut, à ajuster selon le contexte
        }
        markModified();
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
     * Retourne le chiffre d'affaires formaté.
     *
     * @return CA formaté
     */
    public String getFormattedCA() {
        if (chiffreAffairesCumule == null) return "0,00 €";
        return String.format("%,.2f €", chiffreAffairesCumule);
    }

    /**
     * Génère un résumé de la fiche client.
     *
     * @return Résumé textuel
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== FICHE CLIENT ===\n");
        sb.append("ID: ").append(clientId).append("\n");
        sb.append("Compte: ").append(numeroCompte).append("\n");
        sb.append("Raison sociale: ").append(raisonSociale).append("\n");

        if (siret != null) {
            sb.append("SIRET: ").append(siret).append("\n");
        }

        sb.append("\nStatut: ").append(status.getLabel());
        sb.append(" | Segment: ").append(segment.getLabel()).append("\n");

        if (commercialAttitre != null) {
            sb.append("Commercial: ").append(commercialAttitre).append("\n");
        }

        Contact principal = getContactPrincipal();
        if (principal != null) {
            sb.append("\nContact principal: ").append(principal.getNomComplet()).append("\n");
            if (principal.getEmail() != null) {
                sb.append("Email: ").append(principal.getEmail()).append("\n");
            }
        }

        if (adresseSiege != null) {
            sb.append("\nAdresse: ").append(adresseSiege.getAdresseComplete()).append("\n");
        }

        sb.append("\nHistorique:\n");
        sb.append("  Commandes: ").append(nombreCommandes).append("\n");
        sb.append("  CA cumulé: ").append(getFormattedCA()).append("\n");
        sb.append("  Devis: ").append(historiqueDevis.size()).append("\n");

        return sb.toString();
    }

    // === MÉTHODES STANDARD ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(clientId, client.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }

    @Override
    public String toString() {
        return "Client {\n" +
               "  clientId: \"" + clientId + "\"\n" +
               "  numeroCompte: \"" + numeroCompte + "\"\n" +
               "  raisonSociale: \"" + raisonSociale + "\"\n" +
               "  segment: " + segment.getLabel() + "\n" +
               "  status: " + status.getLabel() + "\n" +
               "  contacts: " + contacts.size() + "\n" +
               "  nombreCommandes: " + nombreCommandes + "\n" +
               "  caCumule: " + getFormattedCA() + "\n" +
               "}";
    }
}
