package com.projetdevis.service;

import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Purge automatiquement le corps des emails après 30 jours (RGPD Art. 5.1.e — limitation de la conservation).
 * Le commercial peut consulter l'email original pendant 30 jours ; après, seules les données extraites restent.
 */
@Service
public class EmailRetentionService {

    private final DraftQuoteRepository quoteRepository;

    public EmailRetentionService(DraftQuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Scheduled(cron = "0 0 2 * * *") // chaque nuit à 2h00
    @Transactional
    public void purgerEmailsExpires() {
        int count = quoteRepository.purgerEmailsExpires(LocalDateTime.now());
        if (count > 0) {
            System.out.println("[RGPD] " + count + " email(s) purgé(s) — rétention 30 jours expirée.");
        }
    }
}
