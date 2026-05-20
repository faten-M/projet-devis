package com.projetdevis.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Lit automatiquement une boîte mail Gmail en IMAP toutes les 5 minutes.
 * Pour chaque email non lu, lance le pipeline de génération de devis.
 */
@Service
public class EmailReaderService {

    private final DevisPipelineService pipelineService;

    @Value("${email.imap.host:imap.gmail.com}")
    private String imapHost;

    @Value("${email.imap.port:993}")
    private int imapPort;

    @Value("${email.imap.username:}")
    private String username;

    @Value("${email.imap.password:}")
    private String password;

    @Value("${email.imap.enabled:false}")
    private boolean enabled;

    public EmailReaderService(DevisPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * Se déclenche toutes les 5 minutes.
     * fixedDelay = 300000 ms = 5 minutes.
     * initialDelay = 10000 ms = attend 10 secondes après le démarrage avant le premier scan.
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 10000)
    public void lireEmailsNonLus() {
        if (!enabled) {
            return; // Désactivé par défaut — s'active via application.properties
        }
        if (username.isBlank() || password.isBlank()) {
            System.err.println("[EmailReader] Identifiants manquants — scan annulé.");
            return;
        }

        System.out.println("[EmailReader] Scan de la boîte mail : " + username);

        try {
            Store store = connecterBoiteMail();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Récupère uniquement les emails non lus
            Message[] messages = inbox.search(
                new FlagTerm(new Flags(Flags.Flag.SEEN), false)
            );

            System.out.println("[EmailReader] " + messages.length + " email(s) non lu(s) trouvé(s).");

            for (Message message : messages) {
                traiterEmail(message);
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println("[EmailReader] Erreur lors de la lecture : " + e.getMessage());
        }
    }

    /**
     * Traite un seul email : extrait le texte, lance le pipeline, marque comme lu.
     */
    private void traiterEmail(Message message) {
        try {
            String sujet = message.getSubject() != null ? message.getSubject() : "(sans sujet)";
            System.out.println("[EmailReader] Traitement : " + sujet);

            String texte = extraireTexte(message);
            if (texte == null || texte.isBlank()) {
                System.out.println("[EmailReader] Email vide — ignoré.");
                return;
            }

            // Lance le pipeline complet → génère le devis en base
            pipelineService.process(texte);
            System.out.println("[EmailReader] Devis généré pour : " + sujet);

            // Marque l'email comme lu pour ne pas le retraiter
            message.setFlag(Flags.Flag.SEEN, true);

        } catch (Exception e) {
            System.err.println("[EmailReader] Erreur sur l'email '" + getSubjectSafe(message) + "' : " + e.getMessage());
        }
    }

    /**
     * Extrait le contenu texte d'un email (supporte texte simple et multipart).
     */
    private String extraireTexte(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof String texte) {
            return texte;
        }

        if (content instanceof MimeMultipart multipart) {
            return extraireTexteMultipart(multipart);
        }

        return null;
    }

    /**
     * Parcourt les parties d'un email multipart pour trouver le texte.
     * Cherche text/plain en priorité, puis text/html en dernier recours.
     */
    private String extraireTexteMultipart(MimeMultipart multipart) throws Exception {
        String textePlain = null;
        String texteHtml  = null;

        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            String type = part.getContentType().toLowerCase();

            if (type.startsWith("text/plain")) {
                textePlain = (String) part.getContent();
            } else if (type.startsWith("text/html")) {
                texteHtml = part.getContent().toString();
            } else if (part.getContent() instanceof MimeMultipart nested) {
                String nested_text = extraireTexteMultipart(nested);
                if (nested_text != null) textePlain = nested_text;
            }
        }

        // Préfère le texte brut, l'HTML en dernier recours
        return textePlain != null ? textePlain : texteHtml;
    }

    /**
     * Se connecte à la boîte mail via IMAP SSL.
     */
    private Store connecterBoiteMail() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imapHost);
        props.put("mail.imaps.port", String.valueOf(imapPort));
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(imapHost, username, password);
        return store;
    }

    private String getSubjectSafe(Message message) {
        try { return message.getSubject(); } catch (Exception e) { return "(inconnu)"; }
    }
}
