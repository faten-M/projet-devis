package com.projetdevis.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.projetdevis.model.DraftQuote;
import com.projetdevis.model.QuoteItem;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color BLEU_BTP   = new Color(26, 82, 118);
    private static final Color GRIS_CLAIR = new Color(245, 245, 245);
    private static final Color GRIS_BORD  = new Color(200, 200, 200);

    public byte[] genererPdfDevis(DraftQuote devis) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            ajouterEntete(doc, devis);
            ajouterInfosClient(doc, devis);
            ajouterTableauArticles(doc, devis.getItems());
            ajouterRecapFinancier(doc, devis);
            ajouterPiedDePage(doc, devis);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }

    private void ajouterEntete(Document doc, DraftQuote devis) throws DocumentException {
        Font fontSociete = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BLEU_BTP);
        Font fontTitre   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
        Font fontNormal  = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        // Nom société
        Paragraph societe = new Paragraph("DevisAI BTP", fontSociete);
        societe.setSpacingAfter(2);
        doc.add(societe);

        Paragraph sousTitre = new Paragraph("Automatisation & gestion des devis BTP", fontNormal);
        sousTitre.setSpacingAfter(10);
        doc.add(sousTitre);

        // Bandeau titre
        PdfPTable bandeauTable = new PdfPTable(2);
        bandeauTable.setWidthPercentage(100);
        bandeauTable.setWidths(new float[]{2f, 1f});

        PdfPCell cellTitre = new PdfPCell(new Phrase("DEVIS", fontTitre));
        cellTitre.setBackgroundColor(BLEU_BTP);
        cellTitre.setPadding(10);
        cellTitre.setBorder(Rectangle.NO_BORDER);
        bandeauTable.addCell(cellTitre);

        Font fontNumero = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BLEU_BTP);
        Font fontDate   = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        PdfPCell cellNumero = new PdfPCell();
        cellNumero.addElement(new Phrase("N° " + devis.getQuoteNumber(), fontNumero));
        String dateCreation = devis.getCreatedAt() != null
                ? devis.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";
        cellNumero.addElement(new Phrase("Date : " + dateCreation, fontDate));
        String validite = devis.getValidUntil() != null
                ? devis.getValidUntil().format(DATE_FMT)
                : "—";
        cellNumero.addElement(new Phrase("Valide jusqu'au : " + validite, fontDate));
        cellNumero.setPadding(8);
        cellNumero.setBorder(Rectangle.BOX);
        cellNumero.setBorderColor(GRIS_BORD);
        bandeauTable.addCell(cellNumero);

        bandeauTable.setSpacingAfter(16);
        doc.add(bandeauTable);
    }

    private void ajouterInfosClient(Document doc, DraftQuote devis) throws DocumentException {
        Font fontLabel  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BLEU_BTP);
        Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font fontTitre  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell entete = new PdfPCell(new Phrase("CLIENT", fontTitre));
        entete.setBackgroundColor(BLEU_BTP);
        entete.setPadding(6);
        entete.setBorder(Rectangle.NO_BORDER);
        table.addCell(entete);

        PdfPCell corps = new PdfPCell();
        corps.setPadding(8);
        corps.setBorderColor(GRIS_BORD);

        String nom = devis.getClientNom() != null ? devis.getClientNom() : "Non détecté";
        corps.addElement(new Phrase(new Chunk("Nom : ", fontLabel)));
        corps.addElement(new Phrase(nom + "\n", fontValeur));

        String email = devis.getClientEmail() != null ? devis.getClientEmail() : "—";
        corps.addElement(new Phrase(new Chunk("Email : ", fontLabel)));
        corps.addElement(new Phrase(email + "\n", fontValeur));

        if (devis.getSubject() != null) {
            corps.addElement(new Phrase(new Chunk("Objet : ", fontLabel)));
            corps.addElement(new Phrase(devis.getSubject() + "\n", fontValeur));
        }

        table.addCell(corps);
        table.setSpacingAfter(16);
        doc.add(table);
    }

    private void ajouterTableauArticles(Document doc, List<QuoteItem> items) throws DocumentException {
        Font fontEntete = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font fontCell   = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1.2f, 1f, 1.2f, 0.8f, 1.2f});

        String[] entetes = {"Désignation", "Catégorie", "Qté", "Prix unit. HT", "Remise", "Total HT"};
        for (String h : entetes) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontEntete));
            cell.setBackgroundColor(BLEU_BTP);
            cell.setPadding(7);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        boolean pair = false;
        for (QuoteItem item : items) {
            Color bg = pair ? GRIS_CLAIR : Color.WHITE;

            addCell(table, item.getDesignation(), fontCell, bg, Element.ALIGN_LEFT);
            addCell(table, item.getCategory() != null ? item.getCategory().name() : "—", fontCell, bg, Element.ALIGN_CENTER);
            addCell(table, String.valueOf(item.getQuantity()), fontCell, bg, Element.ALIGN_CENTER);
            addCell(table, item.getUnitPriceHT() != null ? String.format("%.2f €", item.getUnitPriceHT()) : "—", fontCell, bg, Element.ALIGN_RIGHT);
            addCell(table, item.getDiscountPercent() != null ? String.format("%.0f%%", item.getDiscountPercent()) : "0%", fontCell, bg, Element.ALIGN_CENTER);

            double total = 0;
            if (item.getUnitPriceHT() != null) {
                total = item.getUnitPriceHT() * item.getQuantity();
                if (item.getDiscountPercent() != null) {
                    total = total * (1 - item.getDiscountPercent() / 100);
                }
            }
            addCell(table, String.format("%.2f €", total), fontCell, bg, Element.ALIGN_RIGHT);
            pair = !pair;
        }

        table.setSpacingAfter(16);
        doc.add(table);
    }

    private void ajouterRecapFinancier(Document doc, DraftQuote devis) throws DocumentException {
        Font fontLabel  = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font fontTotal  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1.5f, 1f});

        addLigneRecap(table, "Total HT", fmt(devis.getTotalHT()), fontLabel, fontValeur, Color.WHITE);
        addLigneRecap(table, "TVA (" + (int) devis.getTvaRate() + "%)", fmt(devis.getTotalTVA()), fontLabel, fontValeur, Color.WHITE);

        if (devis.isDeliveryIncluded() && devis.getDeliveryFees() != null && devis.getDeliveryFees() > 0) {
            addLigneRecap(table, "Frais de livraison TTC", fmt(devis.getDeliveryFees()), fontLabel, fontValeur, Color.WHITE);
        }

        PdfPCell labelTTC = new PdfPCell(new Phrase("Total TTC", fontTotal));
        labelTTC.setBackgroundColor(BLEU_BTP);
        labelTTC.setPadding(8);
        labelTTC.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelTTC);

        PdfPCell valeurTTC = new PdfPCell(new Phrase(fmt(devis.getTotalTTC()), fontTotal));
        valeurTTC.setBackgroundColor(BLEU_BTP);
        valeurTTC.setPadding(8);
        valeurTTC.setBorder(Rectangle.NO_BORDER);
        valeurTTC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valeurTTC);

        table.setSpacingAfter(20);
        doc.add(table);
    }

    private void ajouterPiedDePage(Document doc, DraftQuote devis) throws DocumentException {
        Font fontLabel  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BLEU_BTP);
        Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font fontTitre  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BLEU_BTP);

        Paragraph titreConditions = new Paragraph("Conditions", fontTitre);
        titreConditions.setSpacingBefore(10);
        titreConditions.setSpacingAfter(6);
        doc.add(titreConditions);

        if (devis.getPaymentTerms() != null) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Conditions de paiement : ", fontLabel));
            p.add(new Chunk(devis.getPaymentTerms(), fontValeur));
            p.setSpacingAfter(4);
            doc.add(p);
        }

        // Mode de livraison
        if (!devis.isDeliveryIncluded()) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Mode de livraison : ", fontLabel));
            p.add(new Chunk("Retrait en entreprise", fontValeur));
            p.setSpacingAfter(4);
            doc.add(p);
        }

        // Garantie
        if (devis.getWarranty() != null && !devis.getWarranty().isBlank()) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Garantie : ", fontLabel));
            p.add(new Chunk(devis.getWarranty(), fontValeur));
            p.setSpacingAfter(4);
            doc.add(p);
        }

        if (devis.getValidUntil() != null) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("Validité de l'offre : ", fontLabel));
            p.add(new Chunk("jusqu'au " + devis.getValidUntil().format(DATE_FMT), fontValeur));
            p.setSpacingAfter(4);
            doc.add(p);
        }

        Font fontNote = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Paragraph note = new Paragraph(
            "Document généré automatiquement par DevisAI BTP — " +
            (devis.getCreatedAt() != null ? devis.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""),
            fontNote
        );
        note.setSpacingBefore(20);
        note.setAlignment(Element.ALIGN_CENTER);
        doc.add(note);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(GRIS_BORD);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addLigneRecap(PdfPTable table, String label, String valeur,
                                Font fontLabel, Font fontValeur, Color bg) {
        PdfPCell l = new PdfPCell(new Phrase(label, fontLabel));
        l.setBackgroundColor(bg);
        l.setPadding(6);
        l.setBorderColor(GRIS_BORD);
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(valeur, fontValeur));
        v.setBackgroundColor(bg);
        v.setPadding(6);
        v.setBorderColor(GRIS_BORD);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(v);
    }

    private String fmt(Double montant) {
        if (montant == null) return "—";
        return String.format("%,.2f €", montant);
    }
}
