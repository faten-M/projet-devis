package com.projetdevis.service;

import com.projetdevis.model.*;
import com.projetdevis.repository.ClientRepository;
import com.projetdevis.repository.DraftQuoteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemoDataService {

    private final DraftQuoteRepository quoteRepository;
    private final ClientRepository     clientRepository;

    public DemoDataService(DraftQuoteRepository quoteRepository,
                           ClientRepository clientRepository) {
        this.quoteRepository = quoteRepository;
        this.clientRepository = clientRepository;
    }

    @PostConstruct
    public void init() {
        if (quoteRepository.count() > 0) return;
        creerDonnees();
    }

    @Transactional
    public void creerDonnees() {

        // ── Devis 1 — Dupont Construction ─────────────────────────────
        Client c1 = new Client("SARL Dupont Construction");
        c1.setEmailOrigine("dupont@construction-btp.fr");
        c1.setSourceOrigine("EMAIL");
        c1.getHistoriqueDevis().add("DEV-20260501-1001");
        clientRepository.save(c1);

        DraftQuote d1 = devis("DEV-20260501-1001", LocalDateTime.now().minusDays(5), c1,
            "Commande ciment et parpaings — chantier résidentiel",
            DraftQuote.DraftStatus.PRET, DraftQuote.Priority.NORMALE, 0.87,
            List.of(),
            List.of("Volume important — inclure la livraison dans le devis", "Négocier remise fournisseur 5%"),
            List.of(),
            List.of(
                item(1, "Ciment CEM II 32,5 — sac 35 kg", AnalyzedItem.Category.GROS_OEUVRE, 120, "sac", 8.50),
                item(2, "Parpaing creux 20x20x50 cm",      AnalyzedItem.Category.GROS_OEUVRE, 500, "unité", 1.20)
            )
        );
        quoteRepository.save(d1);

        // ── Devis 2 — BTP Martin ──────────────────────────────────────
        Client c2 = new Client("BTP Martin & Fils");
        c2.setEmailOrigine("martin.btpfils@outlook.com");
        c2.setSourceOrigine("EMAIL");
        c2.getHistoriqueDevis().add("DEV-20260502-1002");
        clientRepository.save(c2);

        DraftQuote d2 = devis("DEV-20260502-1002", LocalDateTime.now().minusDays(3), c2,
            "Fourniture fer à béton HA16 et sable de carrière",
            DraftQuote.DraftStatus.BROUILLON, DraftQuote.Priority.HAUTE, 0.72,
            List.of("Compléter les informations de livraison", "Vérifier disponibilité stock HA16"),
            List.of("Commander avant vendredi pour livraison semaine prochaine"),
            List.of("Quantité de sable à confirmer avec le client"),
            List.of(
                item(1, "Fer à béton HA16 — barre 6 m",   AnalyzedItem.Category.GROS_OEUVRE,  80, "barre", 12.40),
                item(2, "Sable de carrière 0/4 — big bag", AnalyzedItem.Category.VRD,          10, "big bag", 38.00),
                item(3, "Gravier concassé 8/15 — big bag", AnalyzedItem.Category.VRD,           8, "big bag", 42.00)
            )
        );
        quoteRepository.save(d2);

        // ── Devis 3 — Khalid Rénovation ───────────────────────────────
        Client c3 = new Client("Entreprise Khalid Rénovation");
        c3.setEmailOrigine("khalid.renov@gmail.com");
        c3.setSourceOrigine("EMAIL");
        c3.getHistoriqueDevis().add("DEV-20260503-1003");
        clientRepository.save(c3);

        DraftQuote d3 = devis("DEV-20260503-1003", LocalDateTime.now().minusDays(2), c3,
            "Carrelage sol + isolant murs — appartement 80 m²",
            DraftQuote.DraftStatus.A_COMPLETER, DraftQuote.Priority.NORMALE, 0.61,
            List.of("Préciser la couleur du carrelage", "Confirmer surface exacte à isoler"),
            List.of("Prévoir joint de carrelage assorti", "Inclure l'outillage de pose"),
            List.of("Surface carrelage incohérente avec surface isolant"),
            List.of(
                item(1, "Carrelage grès cérame 60x60 beige", AnalyzedItem.Category.FINITION,  100, "m²", 22.50),
                item(2, "Colle carrelage flex C2",            AnalyzedItem.Category.FINITION,   25, "sac", 14.80),
                item(3, "Isolant laine de verre 100mm",       AnalyzedItem.Category.ISOLATION,  80, "m²", 8.90)
            )
        );
        quoteRepository.save(d3);

        // ── Devis 4 — Maçonnerie Bernard ──────────────────────────────
        Client c4 = new Client("Maçonnerie Bernard SAS");
        c4.setEmailOrigine("contact@maconnerie-bernard.fr");
        c4.setSourceOrigine("EMAIL");
        c4.getHistoriqueDevis().add("DEV-20260503-1004");
        clientRepository.save(c4);

        DraftQuote d4 = devis("DEV-20260503-1004", LocalDateTime.now().minusDays(1), c4,
            "Couverture tuiles + charpente — maison individuelle",
            DraftQuote.DraftStatus.PRET, DraftQuote.Priority.HAUTE, 0.91,
            List.of(),
            List.of("Client fidèle — proposer remise 3%", "Prévoir livraison en deux fois"),
            List.of(),
            List.of(
                item(1, "Tuile terre cuite rouge 17×27",    AnalyzedItem.Category.COUVERTURE, 350, "unité", 1.85),
                item(2, "Chevron sapin 63x75 mm — 4 m",     AnalyzedItem.Category.CHARPENTE,   40, "pièce", 9.20),
                item(3, "Sous-toiture respirante — 150 m²",  AnalyzedItem.Category.COUVERTURE,   2, "rouleau", 85.00)
            )
        );
        quoteRepository.save(d4);

        // ── Devis 5 — Constructions Modernes ──────────────────────────
        Client c5 = new Client("Constructions Modernes SARL");
        c5.setEmailOrigine("devis@constructions-modernes.com");
        c5.setSourceOrigine("EMAIL");
        c5.getHistoriqueDevis().add("DEV-20260504-1005");
        clientRepository.save(c5);

        DraftQuote d5 = devis("DEV-20260504-1005", LocalDateTime.now().minusHours(6), c5,
            "Installation électrique complète — local commercial 200 m²",
            DraftQuote.DraftStatus.BROUILLON, DraftQuote.Priority.URGENTE, 0.78,
            List.of("Vérifier la puissance du disjoncteur principal"),
            List.of("Commande urgente — contacter fournisseur dès aujourd'hui"),
            List.of(),
            List.of(
                item(1, "Câble électrique 2,5mm² — 100 m",  AnalyzedItem.Category.ELECTRICITE,  5, "rouleau", 48.00),
                item(2, "Tableau électrique 36 modules",     AnalyzedItem.Category.ELECTRICITE,  1, "unité", 185.00),
                item(3, "Gaine ICTA 3320 — 100 m",          AnalyzedItem.Category.ELECTRICITE,  3, "rouleau", 32.00),
                item(4, "Prises 2P+T encastrables (lot 5)", AnalyzedItem.Category.ELECTRICITE, 10, "lot", 18.50)
            )
        );
        quoteRepository.save(d5);

        // ── Devis 6 — Plomberie Legrand ───────────────────────────────
        Client c6 = new Client("Plomberie Legrand");
        c6.setEmailOrigine("legrand.plomberie@wanadoo.fr");
        c6.setSourceOrigine("EMAIL");
        c6.getHistoriqueDevis().add("DEV-20260504-1006");
        clientRepository.save(c6);

        DraftQuote d6 = devis("DEV-20260504-1006", LocalDateTime.now().minusHours(2), c6,
            "Fourniture plomberie sanitaires — immeuble 12 logements",
            DraftQuote.DraftStatus.REJETE, DraftQuote.Priority.BASSE, 0.55,
            List.of(),
            List.of(),
            List.of("Budget client dépassé : 859 € demandé pour un budget de 500 €"),
            List.of(
                item(1, "Tube cuivre 16/18 — barre 5 m",   AnalyzedItem.Category.PLOMBERIE, 24, "barre", 28.00),
                item(2, "Raccord compression laiton 16mm",  AnalyzedItem.Category.PLOMBERIE, 60, "unité",  3.20)
            )
        );
        d6.setClientBudget(500.0);
        quoteRepository.save(d6);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private DraftQuote devis(String numero, LocalDateTime createdAt, Client client,
                              String subject, DraftQuote.DraftStatus status,
                              DraftQuote.Priority priority, double confidence,
                              List<String> requiredActions,
                              List<String> recommendations,
                              List<String> inconsistencies,
                              List<QuoteItem> items) {
        DraftQuote d = new DraftQuote();
        d.setQuoteNumber(numero);
        d.setCreatedAt(createdAt);
        d.setModifiedAt(createdAt);
        d.setValidUntil(createdAt.toLocalDate().plusDays(30));
        d.setSubject(subject);
        d.setStatus(status);
        d.setPriority(priority);
        d.setConfidence(confidence);
        d.setClientReference(client.getClientId());
        d.setClientNom(client.getRaisonSociale());
        d.setClientEmail(client.getEmailOrigine());
        d.setTvaRate(20.0);
        d.setItems(items);
        d.setRequiredActions(requiredActions);
        d.setRecommendations(recommendations);
        d.setInconsistencies(inconsistencies);

        double totalHT = items.stream()
            .mapToDouble(i -> i.getTotalPriceHT() != null ? i.getTotalPriceHT() : 0)
            .sum();
        d.setTotalHT(totalHT);
        d.setTotalTVA(Math.round(totalHT * 0.20 * 100.0) / 100.0);
        d.setTotalTTC(Math.round(totalHT * 1.20 * 100.0) / 100.0);
        return d;
    }

    private QuoteItem item(int line, String designation, AnalyzedItem.Category category,
                            int qty, String unit, double unitPrice) {
        QuoteItem i = new QuoteItem();
        i.setLineNumber(line);
        i.setDesignation(designation);
        i.setCategory(category);
        i.setQuantity(qty);
        i.setUnit(unit);
        i.setUnitPriceHT(unitPrice);
        i.setTotalPriceHT(Math.round(unitPrice * qty * 100.0) / 100.0);
        i.setTvaRate(20.0);
        i.setStatus(QuoteItem.LineStatus.COMPLETE);
        i.setPriceRange(QuoteItem.PriceRange.STANDARD);
        return i;
    }
}
