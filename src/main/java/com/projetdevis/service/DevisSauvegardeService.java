package com.projetdevis.service;

import com.projetdevis.model.Client;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère la sauvegarde atomique du client et du devis en base.
 *
 * Séparé de DevisPipelineService pour que @Transactional ne s'ouvre
 * qu'au moment de l'écriture en base — après tous les appels IA.
 */
@Service
public class DevisSauvegardeService {

    private final ClientRepository     clientRepository;
    private final DraftQuoteRepository quoteRepository;

    public DevisSauvegardeService(ClientRepository clientRepository,
                                   DraftQuoteRepository quoteRepository) {
        this.clientRepository = clientRepository;
        this.quoteRepository  = quoteRepository;
    }

    @Transactional
    public DraftQuote sauvegarder(Client client, DraftQuote draft) {
        clientRepository.save(client);
        return quoteRepository.save(draft);
    }
}
