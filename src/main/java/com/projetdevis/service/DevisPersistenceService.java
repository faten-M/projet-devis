package com.projetdevis.service;

import com.projetdevis.model.Client;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevisPersistenceService {

    private final ClientRepository     clientRepository;
    private final DraftQuoteRepository quoteRepository;

    public DevisPersistenceService(ClientRepository clientRepository,
                                   DraftQuoteRepository quoteRepository) {
        this.clientRepository = clientRepository;
        this.quoteRepository  = quoteRepository;
    }

    @Transactional
    public DraftQuote saveDevisWithClient(DraftQuote draft, String nomClient,
                                          String emailClient, String segmentDetecte) {
        Client client = emailClient == null || emailClient.isBlank()
                ? new Client(nomClient)
                : clientRepository.findByEmailOrigine(emailClient)
                                  .orElseGet(() -> new Client(nomClient));

        client.setSourceOrigine("EMAIL");
        client.setEmailOrigine(emailClient == null || emailClient.isBlank() ? null : emailClient);

        if (segmentDetecte != null && !segmentDetecte.isBlank()
                && client.getSegment() == Client.Segment.PROSPECT) {
            try {
                client.setSegment(Client.Segment.valueOf(segmentDetecte));
            } catch (IllegalArgumentException ignored) {}
        }

        if (!client.getHistoriqueDevis().contains(draft.getQuoteNumber())) {
            client.getHistoriqueDevis().add(draft.getQuoteNumber());
        }

        clientRepository.save(client);
        draft.setClientReference(client.getClientId());
        draft.setClientNom(client.getRaisonSociale());
        draft.setClientEmail(client.getEmailOrigine());

        quoteRepository.save(draft);
        return draft;
    }
}
