package com.projetdevis.service;

import com.projetdevis.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service d'intégration CRM.
 *
 * Ce service gère :
 * 1. Création et mise à jour des fiches clients
 * 2. Enregistrement des devis validés dans le CRM
 * 3. Extraction d'informations client depuis les emails
 * 4. Export des données pour CRM externes
 * 5. Suivi des opportunités commerciales
 *
 * Pipeline BMAD - Étape 7 : Intégration CRM
 *
 * @author BMAD Pipeline - Étape 7
 * @version 1.0
 */
public class CrmService {

    // === BASE DE DONNÉES SIMULÉE ===

    /** Base de données des clients (simulation) */
    private Map<String, Client> clientDatabase;

    /** Base de données des enregistrements CRM (simulation) */
    private Map<String, CrmRecord> recordDatabase;

    /** Index des clients par email */
    private Map<String, String> emailToClientId;

    /** Index des clients par SIRET */
    private Map<String, String> siretToClientId;

    // === PATTERNS POUR EXTRACTION ===

    /** Pattern pour email */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    /** Pattern pour téléphone français */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:(?:\\+33|0033|0)\\s*[1-9])(?:[\\s.-]*\\d{2}){4}"
    );

    /** Pattern pour SIRET */
    private static final Pattern SIRET_PATTERN = Pattern.compile(
        "\\b\\d{3}\\s*\\d{3}\\s*\\d{3}\\s*\\d{5}\\b"
    );

    /** Pattern pour nom de société */
    private static final Pattern COMPANY_PATTERNS = Pattern.compile(
        "(?i)(?:société|entreprise|sarl|sas|sa|eurl|sasu)\\s+([A-Za-zÀ-ÿ\\s&'-]+)|" +
        "([A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)*)\\s+(?:sarl|sas|sa|eurl|sasu)"
    );

    /** Patterns pour signature email */
    private static final List<Pattern> SIGNATURE_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)^([A-Z][a-zà-ÿ]+\\s+[A-Z][A-Zà-ÿ]+)$", Pattern.MULTILINE),
        Pattern.compile("(?i)cordialement[,.]?\\s*\\n+([A-Za-zà-ÿ\\s]+)", Pattern.MULTILINE)
    );

    // === CONSTRUCTEUR ===

    /**
     * Constructeur - initialise les bases de données simulées.
     */
    public CrmService() {
        this.clientDatabase = new HashMap<>();
        this.recordDatabase = new HashMap<>();
        this.emailToClientId = new HashMap<>();
        this.siretToClientId = new HashMap<>();
    }

    // === GESTION DES CLIENTS ===

    /**
     * Recherche un client par email.
     *
     * @param email Email à rechercher
     * @return Client trouvé ou null
     */
    public Client findClientByEmail(String email) {
        if (email == null || email.isBlank()) return null;

        String normalizedEmail = email.toLowerCase().trim();
        String clientId = emailToClientId.get(normalizedEmail);

        return clientId != null ? clientDatabase.get(clientId) : null;
    }

    /**
     * Recherche un client par SIRET.
     *
     * @param siret SIRET à rechercher
     * @return Client trouvé ou null
     */
    public Client findClientBySiret(String siret) {
        if (siret == null || siret.isBlank()) return null;

        String normalizedSiret = siret.replaceAll("\\s", "");
        String clientId = siretToClientId.get(normalizedSiret);

        return clientId != null ? clientDatabase.get(clientId) : null;
    }

    /**
     * Recherche un client par ID.
     *
     * @param clientId ID du client
     * @return Client trouvé ou null
     */
    public Client findClientById(String clientId) {
        return clientDatabase.get(clientId);
    }

    /**
     * Crée un nouveau client.
     *
     * @param raisonSociale Raison sociale
     * @return Nouveau client créé
     */
    public Client createClient(String raisonSociale) {
        Client client = new Client(raisonSociale);
        saveClient(client);
        return client;
    }

    /**
     * Crée un nouveau client avec email.
     *
     * @param raisonSociale Raison sociale
     * @param email Email du contact principal
     * @return Nouveau client créé
     */
    public Client createClient(String raisonSociale, String email) {
        Client client = new Client(raisonSociale, email);
        saveClient(client);
        return client;
    }

    /**
     * Sauvegarde un client dans la base.
     *
     * @param client Client à sauvegarder
     */
    public void saveClient(Client client) {
        if (client == null) return;

        clientDatabase.put(client.getClientId(), client);

        // Index par email
        String email = client.getEmail();
        if (email != null) {
            emailToClientId.put(email.toLowerCase().trim(), client.getClientId());
        }

        // Index par SIRET
        if (client.getSiret() != null) {
            siretToClientId.put(client.getSiret().replaceAll("\\s", ""), client.getClientId());
        }
    }

    /**
     * Crée ou récupère un client à partir d'informations extraites.
     *
     * @param raisonSociale Raison sociale (peut être null)
     * @param email Email (peut être null)
     * @param siret SIRET (peut être null)
     * @return Client existant ou nouveau
     */
    public Client getOrCreateClient(String raisonSociale, String email, String siret) {
        // Recherche par SIRET d'abord (plus fiable)
        if (siret != null && !siret.isBlank()) {
            Client existing = findClientBySiret(siret);
            if (existing != null) {
                updateClientIfNeeded(existing, raisonSociale, email);
                return existing;
            }
        }

        // Recherche par email
        if (email != null && !email.isBlank()) {
            Client existing = findClientByEmail(email);
            if (existing != null) {
                updateClientIfNeeded(existing, raisonSociale, siret);
                return existing;
            }
        }

        // Création d'un nouveau client
        String name = raisonSociale != null ? raisonSociale : "Client " + email;
        Client newClient = createClient(name, email);

        if (siret != null && !siret.isBlank()) {
            newClient.setSiret(siret.replaceAll("\\s", ""));
        }

        newClient.setSourceOrigine("Email entrant");
        newClient.setEmailOrigine(email);

        return newClient;
    }

    /**
     * Met à jour un client avec de nouvelles informations si nécessaires.
     */
    private void updateClientIfNeeded(Client client, String raisonSociale, String newInfo) {
        if (raisonSociale != null && client.getRaisonSociale() == null) {
            client.setRaisonSociale(raisonSociale);
        }
        // Mise à jour silencieuse
        saveClient(client);
    }

    // === EXTRACTION D'INFORMATIONS CLIENT ===

    /**
     * Extrait les informations client depuis un email brut.
     *
     * @param emailContent Contenu de l'email
     * @return Informations extraites sous forme de Map
     */
    public Map<String, String> extractClientInfoFromEmail(String emailContent) {
        Map<String, String> info = new LinkedHashMap<>();

        if (emailContent == null || emailContent.isBlank()) {
            return info;
        }

        // Extraction de l'email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(emailContent);
        if (emailMatcher.find()) {
            info.put("email", emailMatcher.group());
        }

        // Extraction du téléphone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(emailContent);
        if (phoneMatcher.find()) {
            info.put("telephone", normalizePhone(phoneMatcher.group()));
        }

        // Extraction du SIRET
        Matcher siretMatcher = SIRET_PATTERN.matcher(emailContent);
        if (siretMatcher.find()) {
            info.put("siret", siretMatcher.group().replaceAll("\\s", ""));
        }

        // Extraction du nom de société
        Matcher companyMatcher = COMPANY_PATTERNS.matcher(emailContent);
        if (companyMatcher.find()) {
            String company = companyMatcher.group(1);
            if (company == null) company = companyMatcher.group(2);
            if (company != null) {
                info.put("societe", company.trim());
            }
        }

        // Extraction du nom de la personne (depuis signature)
        for (Pattern pattern : SIGNATURE_PATTERNS) {
            Matcher sigMatcher = pattern.matcher(emailContent);
            if (sigMatcher.find()) {
                String name = sigMatcher.group(1);
                if (name != null && !name.isBlank() && name.length() > 3) {
                    info.put("contact", name.trim());
                    break;
                }
            }
        }

        // Extraction de l'adresse (simplifiée)
        Pattern addressPattern = Pattern.compile(
            "(\\d{1,4}[,\\s]+[^\\n]+)\\s*(\\d{5})\\s+([A-Za-zÀ-ÿ\\s-]+)",
            Pattern.MULTILINE
        );
        Matcher addressMatcher = addressPattern.matcher(emailContent);
        if (addressMatcher.find()) {
            info.put("adresse", addressMatcher.group(1).trim());
            info.put("codePostal", addressMatcher.group(2));
            info.put("ville", addressMatcher.group(3).trim());
        }

        return info;
    }

    /**
     * Normalise un numéro de téléphone.
     */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+33")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("0033")) {
            digits = "0" + digits.substring(4);
        }
        // Formatage XX XX XX XX XX
        if (digits.length() == 10) {
            return digits.replaceAll("(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})", "$1 $2 $3 $4 $5");
        }
        return phone;
    }

    /**
     * Crée un client à partir des informations extraites d'un email.
     *
     * @param emailContent Contenu de l'email
     * @return Client créé ou existant
     */
    public Client createClientFromEmail(String emailContent) {
        Map<String, String> info = extractClientInfoFromEmail(emailContent);

        String email = info.get("email");
        String siret = info.get("siret");
        String societe = info.get("societe");

        // Recherche ou création du client
        Client client = getOrCreateClient(societe, email, siret);

        // Enrichissement avec les informations extraites
        if (info.containsKey("telephone")) {
            client.setTelephone(info.get("telephone"));
        }

        if (info.containsKey("contact")) {
            String contactName = info.get("contact");
            String[] parts = contactName.split("\\s+", 2);
            Client.Contact contact = new Client.Contact();
            if (parts.length >= 2) {
                contact.setPrenom(parts[0]);
                contact.setNom(parts[1]);
            } else {
                contact.setNom(contactName);
            }
            contact.setEmail(email);
            contact.setTelephone(info.get("telephone"));
            contact.setPrincipal(true);

            // Remplacer le contact existant si c'est juste un email
            if (client.getContacts().isEmpty() ||
                client.getContactPrincipal().getNom() == null) {
                client.getContacts().clear();
                client.addContact(contact);
            }
        }

        // Adresse
        if (info.containsKey("adresse")) {
            Client.Adresse adresse = new Client.Adresse(
                info.get("adresse"),
                info.get("codePostal"),
                info.get("ville")
            );
            client.setAdresseSiege(adresse);
        }

        client.setEmailOrigine(emailContent.length() > 500 ?
            emailContent.substring(0, 500) + "..." : emailContent);

        saveClient(client);
        return client;
    }

    // === GESTION DES ENREGISTREMENTS CRM ===

    /**
     * Crée un enregistrement CRM pour un devis validé.
     *
     * @param validatedQuote Devis validé
     * @param client Client associé
     * @return Enregistrement CRM créé
     */
    public CrmRecord createRecord(ValidatedQuote validatedQuote, Client client) {
        CrmRecord record = new CrmRecord(validatedQuote, client);

        // Configuration supplémentaire
        record.setSource("Pipeline BMAD");

        // Tags automatiques
        if (validatedQuote.getDraft().getPriority() == DraftQuote.Priority.URGENTE ||
            validatedQuote.getDraft().getPriority() == DraftQuote.Priority.CRITIQUE) {
            record.addTag("urgent");
        }

        Double montant = record.getMontantHT();
        if (montant != null) {
            if (montant > 50000) {
                record.addTag("grand-projet");
            } else if (montant > 10000) {
                record.addTag("projet-moyen");
            }
        }

        if (client != null && client.isProspect()) {
            record.addTag("nouveau-client");
        }

        // Sauvegarde
        saveRecord(record);

        // Mise à jour du client
        if (client != null) {
            client.addDevis(validatedQuote.getDraft().getQuoteNumber());
            saveClient(client);
        }

        return record;
    }

    /**
     * Crée un enregistrement CRM avec extraction automatique du client.
     *
     * @param validatedQuote Devis validé
     * @param originalEmail Email d'origine
     * @return Enregistrement CRM créé
     */
    public CrmRecord createRecordWithClientExtraction(ValidatedQuote validatedQuote, String originalEmail) {
        // Extraction/création du client
        Client client = createClientFromEmail(originalEmail);

        // Création de l'enregistrement
        CrmRecord record = createRecord(validatedQuote, client);

        // Ajout de l'email d'origine comme référence
        record.setCustomField("originalEmail", originalEmail.length() > 200 ?
            originalEmail.substring(0, 200) + "..." : originalEmail);

        return record;
    }

    /**
     * Sauvegarde un enregistrement CRM.
     *
     * @param record Enregistrement à sauvegarder
     */
    public void saveRecord(CrmRecord record) {
        if (record != null) {
            recordDatabase.put(record.getRecordId(), record);
        }
    }

    /**
     * Recherche un enregistrement par ID.
     *
     * @param recordId ID de l'enregistrement
     * @return Enregistrement trouvé ou null
     */
    public CrmRecord findRecordById(String recordId) {
        return recordDatabase.get(recordId);
    }

    /**
     * Recherche les enregistrements par client.
     *
     * @param clientId ID du client
     * @return Liste des enregistrements
     */
    public List<CrmRecord> findRecordsByClient(String clientId) {
        return recordDatabase.values().stream()
            .filter(r -> clientId.equals(r.getClientId()))
            .collect(Collectors.toList());
    }

    /**
     * Recherche les enregistrements par statut.
     *
     * @param status Statut recherché
     * @return Liste des enregistrements
     */
    public List<CrmRecord> findRecordsByStatus(CrmRecord.OpportunityStatus status) {
        return recordDatabase.values().stream()
            .filter(r -> r.getStatus() == status)
            .collect(Collectors.toList());
    }

    /**
     * Retourne les opportunités ouvertes.
     *
     * @return Liste des opportunités ouvertes
     */
    public List<CrmRecord> getOpenOpportunities() {
        return recordDatabase.values().stream()
            .filter(CrmRecord::isOpen)
            .collect(Collectors.toList());
    }

    /**
     * Retourne les opportunités nécessitant un suivi.
     *
     * @return Liste des opportunités à relancer
     */
    public List<CrmRecord> getOpportunitiesNeedingFollowUp() {
        return recordDatabase.values().stream()
            .filter(CrmRecord::needsFollowUp)
            .collect(Collectors.toList());
    }

    // === EXPORT CRM ===

    /**
     * Génère un export CRM pour un enregistrement.
     *
     * @param record Enregistrement à exporter
     * @param format Format d'export
     * @param target CRM cible
     * @return Export généré
     */
    public CrmExport generateExport(CrmRecord record, CrmExport.ExportFormat format,
                                     CrmExport.CrmTarget target) {
        CrmExport export = new CrmExport(record, format, target);
        export.addMetadata("exportedBy", "CrmService");
        export.addMetadata("targetCrm", target.getLabel());
        export.generate();
        return export;
    }

    /**
     * Génère un export JSON.
     *
     * @param record Enregistrement à exporter
     * @return Export JSON
     */
    public CrmExport generateJsonExport(CrmRecord record) {
        return generateExport(record, CrmExport.ExportFormat.JSON, CrmExport.CrmTarget.GENERIC);
    }

    /**
     * Génère un export CSV.
     *
     * @param record Enregistrement à exporter
     * @return Export CSV
     */
    public CrmExport generateCsvExport(CrmRecord record) {
        return generateExport(record, CrmExport.ExportFormat.CSV, CrmExport.CrmTarget.GENERIC);
    }

    /**
     * Génère un export pour Salesforce.
     *
     * @param record Enregistrement à exporter
     * @return Export Salesforce
     */
    public CrmExport generateSalesforceExport(CrmRecord record) {
        return generateExport(record, CrmExport.ExportFormat.JSON, CrmExport.CrmTarget.SALESFORCE);
    }

    // === STATISTIQUES ===

    /**
     * Calcule les statistiques CRM.
     *
     * @return Map des statistiques
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Clients
        stats.put("totalClients", clientDatabase.size());
        long prospects = clientDatabase.values().stream()
            .filter(Client::isProspect)
            .count();
        stats.put("prospects", prospects);
        stats.put("clientsActifs", clientDatabase.size() - prospects);

        // Opportunités
        stats.put("totalOpportunites", recordDatabase.size());

        Map<CrmRecord.OpportunityStatus, Long> byStatus = recordDatabase.values().stream()
            .collect(Collectors.groupingBy(CrmRecord::getStatus, Collectors.counting()));
        stats.put("opportunitesParStatut", byStatus);

        long open = recordDatabase.values().stream().filter(CrmRecord::isOpen).count();
        stats.put("opportunitesOuvertes", open);

        long won = recordDatabase.values().stream().filter(CrmRecord::isWon).count();
        stats.put("opportunitesGagnees", won);

        // Pipeline
        double pipelineValue = recordDatabase.values().stream()
            .filter(CrmRecord::isOpen)
            .mapToDouble(r -> r.getMontantHT() != null ? r.getMontantHT() : 0)
            .sum();
        stats.put("valeurPipeline", pipelineValue);

        double weightedPipeline = recordDatabase.values().stream()
            .filter(CrmRecord::isOpen)
            .mapToDouble(r -> r.getMontantPondere() != null ? r.getMontantPondere() : 0)
            .sum();
        stats.put("valeurPipelinePonderee", weightedPipeline);

        // Relances nécessaires
        long needsFollowUp = recordDatabase.values().stream()
            .filter(CrmRecord::needsFollowUp)
            .count();
        stats.put("relancesNecessaires", needsFollowUp);

        return stats;
    }

    // === RAPPORT ===

    /**
     * Génère un rapport CRM complet.
     *
     * @return Rapport textuel
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              RAPPORT CRM - ÉTAPE 7                               ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");

        Map<String, Object> stats = getStatistics();

        // Clients
        sb.append("=== CLIENTS ===\n");
        sb.append(String.format("  Total clients: %d\n", stats.get("totalClients")));
        sb.append(String.format("  Prospects: %d\n", stats.get("prospects")));
        sb.append(String.format("  Clients actifs: %d\n", stats.get("clientsActifs")));
        sb.append("\n");

        // Opportunités
        sb.append("=== OPPORTUNITÉS ===\n");
        sb.append(String.format("  Total: %d\n", stats.get("totalOpportunites")));
        sb.append(String.format("  Ouvertes: %d\n", stats.get("opportunitesOuvertes")));
        sb.append(String.format("  Gagnées: %d\n", stats.get("opportunitesGagnees")));
        sb.append("\n");

        // Par statut
        @SuppressWarnings("unchecked")
        Map<CrmRecord.OpportunityStatus, Long> byStatus =
            (Map<CrmRecord.OpportunityStatus, Long>) stats.get("opportunitesParStatut");
        if (byStatus != null && !byStatus.isEmpty()) {
            sb.append("  Par statut:\n");
            for (Map.Entry<CrmRecord.OpportunityStatus, Long> entry : byStatus.entrySet()) {
                sb.append(String.format("    - %s: %d\n", entry.getKey().getLabel(), entry.getValue()));
            }
            sb.append("\n");
        }

        // Pipeline
        sb.append("=== PIPELINE ===\n");
        sb.append(String.format("  Valeur totale: %,.2f €\n", stats.get("valeurPipeline")));
        sb.append(String.format("  Valeur pondérée: %,.2f €\n", stats.get("valeurPipelinePonderee")));
        sb.append(String.format("  Relances nécessaires: %d\n", stats.get("relancesNecessaires")));

        // Liste des opportunités ouvertes
        List<CrmRecord> openOps = getOpenOpportunities();
        if (!openOps.isEmpty()) {
            sb.append("\n=== OPPORTUNITÉS OUVERTES ===\n");
            for (CrmRecord record : openOps) {
                sb.append(String.format("  • %s | %s | %s | %,.2f € (%d%%)\n",
                    record.getQuoteNumber(),
                    record.getOpportunityName() != null ?
                        (record.getOpportunityName().length() > 30 ?
                            record.getOpportunityName().substring(0, 30) + "..." :
                            record.getOpportunityName()) : "N/A",
                    record.getStatus().getLabel(),
                    record.getMontantHT() != null ? record.getMontantHT() : 0,
                    record.getProbabiliteConversion()
                ));
            }
        }

        return sb.toString();
    }

    // === ACCESSEURS ===

    /**
     * Retourne tous les clients.
     *
     * @return Collection des clients
     */
    public Collection<Client> getAllClients() {
        return clientDatabase.values();
    }

    /**
     * Retourne tous les enregistrements.
     *
     * @return Collection des enregistrements
     */
    public Collection<CrmRecord> getAllRecords() {
        return recordDatabase.values();
    }

    /**
     * Retourne le nombre de clients.
     *
     * @return Nombre de clients
     */
    public int getClientCount() {
        return clientDatabase.size();
    }

    /**
     * Retourne le nombre d'enregistrements.
     *
     * @return Nombre d'enregistrements
     */
    public int getRecordCount() {
        return recordDatabase.size();
    }
}
