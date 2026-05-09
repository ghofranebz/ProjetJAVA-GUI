package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import entities.Commande;
import entities.Facture;
import entities.PanierItem;
import entities.Produit;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FacturePdfService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProduitService produitService = new ProduitService();

    public byte[] generateFacturePdf(
            Facture facture,
            Commande commande,
            List<PanierItem> lignes
    ) throws Exception {

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        // ── Fonts ──
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(14, 57, 96));
        Font headFont  = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(14, 57, 96));
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font tableHead = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font tableBody = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(14, 57, 96));

        // ── HEADER ──
        Paragraph title = new Paragraph("🐾 Purrly", titleFont);
        doc.add(title);

        Paragraph sub = new Paragraph("Votre boutique pour animaux de compagnie", valueFont);
        sub.setSpacingAfter(20);
        doc.add(sub);

        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(14, 57, 96));
        doc.add(new Chunk(line));

        // ── FACTURE TITLE ──
        Paragraph factureTitre = new Paragraph(
                "\nFACTURE #" + facture.getId_facture(), headFont);
        factureTitre.setSpacingAfter(10);
        doc.add(factureTitre);

        // ── INFOS FACTURE ──
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(60);
        infoTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        addInfoRow(infoTable, "Date facture :",
                facture.getDate_facture().format(FMT), labelFont, valueFont);

        addInfoRow(infoTable, "N° commande :",
                "#" + commande.getId_commande(), labelFont, valueFont);

        addInfoRow(infoTable, "Date commande :",
                commande.getDate_commande().format(FMT), labelFont, valueFont);

        addInfoRow(infoTable, "Mode paiement :",
                formatMode(commande.getMode_paiement()), labelFont, valueFont);

        addInfoRow(infoTable, "Statut :",
                commande.getStatut(), labelFont, valueFont);

        infoTable.setSpacingAfter(20);
        doc.add(infoTable);

        // ── 👇 NOUVEAU : INFOS CLIENT (IMPORTANT) ──
        Paragraph clientTitle = new Paragraph("Informations client", headFont);
        clientTitle.setSpacingBefore(10);
        clientTitle.setSpacingAfter(5);
        doc.add(clientTitle);

        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(60);
        clientTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        clientTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        addInfoRow(clientTable, "Nom :", safe(facture.getNom_receiver()), labelFont, valueFont);
        addInfoRow(clientTable, "Email :", safe(facture.getEmail_receiver()), labelFont, valueFont);
        addInfoRow(clientTable, "Téléphone :", safe(facture.getTelephone_receiver()), labelFont, valueFont);
        addInfoRow(clientTable, "Adresse :", safe(facture.getAdresse_receiver()), labelFont, valueFont);

        clientTable.setSpacingAfter(20);
        doc.add(clientTable);

        // ── TABLE PRODUITS ──
        PdfPTable prodTable = new PdfPTable(4);
        prodTable.setWidthPercentage(100);
        prodTable.setWidths(new float[]{5f, 2f, 2f, 2f});

        String[] headers = {"Produit", "Qté", "Prix unitaire", "Total"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, tableHead));
            cell.setBackgroundColor(new BaseColor(14, 57, 96));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            prodTable.addCell(cell);
        }

        for (PanierItem item : lignes) {
            try {
                Produit p = produitService.getById(item.getProduitId());
                String nom = p != null ? p.getNom() : "#" + item.getProduitId();
                float prix = p != null ? p.getPrix() : 0f;
                float total = prix * item.getQuantite();

                addTableRow(prodTable,
                        nom,
                        String.valueOf(item.getQuantite()),
                        formatMoney(prix),
                        formatMoney(total),
                        tableBody);

            } catch (SQLException ignored) {}
        }

        prodTable.setSpacingAfter(16);
        doc.add(prodTable);

        // ── TOTAL ──
        Paragraph totalPara = new Paragraph(
                "TOTAL : " + formatMoney(facture.getMontant_total()), totalFont);
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        totalPara.setSpacingAfter(30);
        doc.add(totalPara);

        // ── FOOTER ──
        doc.add(new Chunk(line));

        Paragraph footer = new Paragraph(
                "\nMerci pour votre confiance ! 🐾\npurrly.com",
                new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private void addInfoRow(PdfPTable t, String label, String value,
                            Font lf, Font vf) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(3);

        PdfPCell v = new PdfPCell(new Phrase(value, vf));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(3);

        t.addCell(l);
        t.addCell(v);
    }

    private void addTableRow(PdfPTable t, String nom, String qty,
                             String prix, String total, Font f) {
        PdfPCell c1 = new PdfPCell(new Phrase(nom, f));
        c1.setPadding(6);

        PdfPCell c2 = new PdfPCell(new Phrase(qty, f));
        c2.setPadding(6);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell c3 = new PdfPCell(new Phrase(prix, f));
        c3.setPadding(6);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell c4 = new PdfPCell(new Phrase(total, f));
        c4.setPadding(6);
        c4.setHorizontalAlignment(Element.ALIGN_RIGHT);

        t.addCell(c1);
        t.addCell(c2);
        t.addCell(c3);
        t.addCell(c4);
    }

    private static String formatMoney(float f) {
        return String.format(Locale.FRENCH, "%.2f TND", f);
    }

    private static String formatMode(String mode) {
        if (mode == null) return "—";
        return switch (mode.trim()) {
            case "livraison" -> "Paiement à la livraison";
            case "en_ligne"  -> "Paiement en ligne";
            default          -> mode;
        };
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}