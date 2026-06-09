package com.projetdevis.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailReaderService.
 *
 * La connexion IMAP réelle (Session/Store/Folder) n'est pas testée ici : elle
 * dépend d'une boîte mail externe et serait du ressort d'un test d'intégration.
 * On teste à la place la logique de traitement d'un message une fois reçu :
 * extraction du texte et déclenchement du pipeline. Les classes jakarta.mail
 * (Message, MimeMultipart, BodyPart) sont mockées.
 */
@ExtendWith(MockitoExtension.class)
class EmailReaderServiceTest {

    @Mock
    private DevisPipelineService pipelineService;

    @Mock
    private Message message;

    private EmailReaderService reader;

    @BeforeEach
    void setUp() {
        reader = new EmailReaderService(pipelineService);
    }

    // =========================================================================
    // traiterEmail
    // =========================================================================

    @Test
    void traiterEmail_lancePipelineEtMarqueLeMessageCommeLu() throws Exception {
        when(message.getSubject()).thenReturn("Demande de devis bureaux");
        when(message.getContent()).thenReturn("Bonjour, je voudrais 10 bureaux en chêne.");

        reader.traiterEmail(message);

        verify(pipelineService).process("Bonjour, je voudrais 10 bureaux en chêne.");
        verify(message).setFlag(Flags.Flag.SEEN, true);
    }

    @Test
    void traiterEmail_ignoreUnEmailVide() throws Exception {
        when(message.getSubject()).thenReturn("Email vide");
        when(message.getContent()).thenReturn("   ");

        reader.traiterEmail(message);

        verify(pipelineService, never()).process(anyString());
        verify(message, never()).setFlag(eq(Flags.Flag.SEEN), anyBoolean());
    }

    @Test
    void traiterEmail_neRemontePasUneErreurDeLecture() throws Exception {
        when(message.getSubject()).thenReturn("Email illisible");
        when(message.getContent()).thenThrow(new RuntimeException("contenu illisible"));

        // Ne doit pas lancer d'exception : une erreur sur un email ne doit pas
        // interrompre le scan des autres messages de la boîte.
        reader.traiterEmail(message);

        verify(pipelineService, never()).process(anyString());
        verify(message, never()).setFlag(eq(Flags.Flag.SEEN), anyBoolean());
    }

    // =========================================================================
    // extraireTexte
    // =========================================================================

    @Test
    void extraireTexte_retourneDirectementLeTextePourUnEmailSimple() throws Exception {
        when(message.getContent()).thenReturn("Texte brut de l'email");

        String texte = reader.extraireTexte(message);

        assertEquals("Texte brut de l'email", texte);
    }

    // =========================================================================
    // extraireTexteMultipart
    // =========================================================================

    @Test
    void extraireTexteMultipart_preferLeTextePlainAuHtml() throws Exception {
        BodyPart htmlPart = mock(BodyPart.class);
        when(htmlPart.getContentType()).thenReturn("text/html; charset=UTF-8");
        when(htmlPart.getContent()).thenReturn("<p>Version HTML</p>");

        BodyPart plainPart = mock(BodyPart.class);
        when(plainPart.getContentType()).thenReturn("text/plain; charset=UTF-8");
        when(plainPart.getContent()).thenReturn("Version texte brut");

        MimeMultipart multipart = mock(MimeMultipart.class);
        when(multipart.getCount()).thenReturn(2);
        when(multipart.getBodyPart(0)).thenReturn(htmlPart);
        when(multipart.getBodyPart(1)).thenReturn(plainPart);

        String texte = reader.extraireTexteMultipart(multipart);

        assertEquals("Version texte brut", texte);
    }

    @Test
    void extraireTexteMultipart_seRabatSurLeHtmlSiAucunTextePlain() throws Exception {
        BodyPart htmlPart = mock(BodyPart.class);
        when(htmlPart.getContentType()).thenReturn("text/html; charset=UTF-8");
        when(htmlPart.getContent()).thenReturn("<p>Version HTML uniquement</p>");

        MimeMultipart multipart = mock(MimeMultipart.class);
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(htmlPart);

        String texte = reader.extraireTexteMultipart(multipart);

        assertEquals("<p>Version HTML uniquement</p>", texte);
    }
}
