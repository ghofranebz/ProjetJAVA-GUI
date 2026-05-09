package services;

import entities.Commande;
import entities.Facture;
import entities.PanierItem;
import main.tools.Mydb;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailService {

    private final String from;
    private final String password;
    private final String host;
    private final int port;

    private final FacturePdfService pdfService = new FacturePdfService();

    public EmailService() throws IOException {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in == null) throw new IOException("config.properties introuvable");
            p.load(in);
        }
        this.from     = p.getProperty("mail.from");
        this.password = p.getProperty("mail.password");
        this.host     = p.getProperty("mail.smtp.host", "smtp.gmail.com");
        this.port     = Integer.parseInt(p.getProperty("mail.smtp.port", "587"));
    }

    // ─────────────────────────────────────────
    // Email lors de la commande
    // ─────────────────────────────────────────
    public void sendFactureEmail(int userId, Facture facture, Commande commande, List<PanierItem> lignes) {
        new Thread(() -> {
            try {
                String toEmail = getUserEmail(userId);
                if (toEmail == null || toEmail.isBlank()) return;
                byte[] pdf = pdfService.generateFacturePdf(facture, commande, lignes);
                String subject = "🐾 Purrly — Votre facture #" + facture.getId_facture();
                String body = "<html><body style='font-family:Arial;color:#0e3960;'>"
                        + "<h2>🐾 Merci pour votre commande !</h2>"
                        + "<p>Bonjour <b>" + safe(facture.getNom_receiver()) + "</b>,</p>"
                        + "<p>Votre commande <b>#" + commande.getId_commande() + "</b> a bien été enregistrée.</p>"
                        + "<h3>Détails livraison :</h3><ul>"
                        + "<li>Email : " + safe(facture.getEmail_receiver()) + "</li>"
                        + "<li>Téléphone : " + safe(facture.getTelephone_receiver()) + "</li>"
                        + "<li>Adresse : " + safe(facture.getAdresse_receiver()) + "</li>"
                        + "</ul>"
                        + "<p>Veuillez trouver votre facture en pièce jointe.</p>"
                        + "<hr/><p style='font-size:12px;color:gray;'>Purrly — Votre boutique pour animaux de compagnie 🐾</p>"
                        + "</body></html>";
                sendEmailWithPdf(toEmail, subject, body, pdf, "facture_" + facture.getId_facture() + ".pdf");
            } catch (Exception e) { e.printStackTrace(); }
        }, "email-facture").start();
    }

    private String safe(String v) { return v == null ? "" : v; }

    // ─────────────────────────────────────────
    // Email lors du changement de statut
    // ─────────────────────────────────────────
    public void sendStatutEmail(int userId, Commande commande) {
        new Thread(() -> {
            try {
                String toEmail = getUserEmail(userId);
                if (toEmail == null || toEmail.isBlank()) return;
                String statutFr = switch (commande.getStatut()) {
                    case "confirmée" -> "✅ Confirmée";
                    case "livrée"    -> "📦 Livrée";
                    case "annulée"   -> "❌ Annulée";
                    default          -> commande.getStatut();
                };
                String subject = "🐾 Purrly — Commande #" + commande.getId_commande() + " : " + statutFr;
                String body = "<html><body style='font-family:Arial;color:#0e3960;'>"
                        + "<h2>🐾 Mise à jour de votre commande</h2>"
                        + "<p>Votre commande <b>#" + commande.getId_commande() + "</b> a été mise à jour.</p>"
                        + "<p>Nouveau statut : <b style='font-size:16px;'>" + statutFr + "</b></p>"
                        + "<hr/><p style='font-size:12px;color:gray;'>Purrly — Votre boutique pour animaux de compagnie 🐾</p>"
                        + "</body></html>";
                sendEmailWithPdf(toEmail, subject, body, null, null);
            } catch (Exception e) { e.printStackTrace(); }
        }, "email-statut").start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  📧  NOUVEAU : Email "nouveau produit disponible"
    //      Appelé dans GestionProduitController après chaque ajout
    // ══════════════════════════════════════════════════════════════════

    /**
     * Envoie un email de notification à tous les utilisateurs abonnés aux alertes
     * (ceux qui ont coché la checkbox dans la boutique).
     *
     * @param nomProduit      Nom du produit nouvellement ajouté
     * @param descProduit     Description courte
     * @param prixProduit     Prix en TND
     */
    public void sendNouveauProduitEmail(String nomProduit, String descProduit, float prixProduit) {
        new Thread(() -> {
            try {
                List<String> destinataires = getEmailsAbonnes();
                if (destinataires.isEmpty()) return;

                String subject = "🐾 Purrly — Nouveau produit : " + nomProduit;

                String body = "<html><body style='font-family:Arial;color:#0e3960;'>"
                        + "<div style='max-width:520px;margin:auto;'>"
                        + "<h2 style='color:#0e3960;'>🐾 Un nouveau produit vient d'être ajouté !</h2>"
                        + "<table style='width:100%;border-radius:12px;background:#f0f7ff;"
                        +       "padding:20px;border:1px solid #c8dff5;'>"
                        + "  <tr><td>"
                        + "    <h3 style='margin:0 0 8px;color:#0e3960;'>" + safe(nomProduit) + "</h3>"
                        + "    <p style='color:#555;margin:0 0 12px;'>" + safe(descProduit) + "</p>"
                        + "    <p style='font-size:20px;font-weight:bold;color:#1a6fc4;margin:0;'>"
                        +        String.format("%.2f TND", prixProduit)
                        + "    </p>"
                        + "  </td></tr>"
                        + "</table>"
                        + "<p style='margin-top:18px;'>Découvrez-le dès maintenant dans notre boutique !</p>"
                        + "<p style='font-size:11px;color:#aaa;margin-top:24px;'>"
                        + "  Vous recevez cet email car vous avez activé les alertes produit sur Purrly.<br/>"
                        + "  Pour vous désabonner, décochez l'option dans la boutique."
                        + "</p>"
                        + "<hr/><p style='font-size:12px;color:gray;'>Purrly — Votre boutique pour animaux de compagnie 🐾</p>"
                        + "</div></body></html>";

                // Envoi individuel pour ne pas exposer les adresses entre elles
                for (String email : destinataires) {
                    try {
                        sendEmailWithPdf(email, subject, body, null, null);
                    } catch (Exception ex) {
                        System.err.println("Erreur envoi alerte à " + email + " : " + ex.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "email-nouveau-produit").start();
    }

    /**
     * Récupère les emails de tous les utilisateurs abonnés aux alertes.
     * Table requise : alertes_nouveau_produit (user_id INT PRIMARY KEY)
     */
    private List<String> getEmailsAbonnes() throws SQLException {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT u.email FROM users u "
                + "JOIN alertes_nouveau_produit a ON u.id = a.user_id "
                + "WHERE u.email IS NOT NULL AND u.email != ''";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String email = rs.getString("email");
                if (email != null && !email.isBlank()) emails.add(email.trim());
            }
        }
        return emails;
    }
    // ══════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────
    // Core send
    // ─────────────────────────────────────────
    private void sendEmailWithPdf(String to, String subject, String htmlBody,
                                  byte[] pdfBytes, String pdfFileName) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            host);
        props.put("mail.smtp.port",            String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, "Purrly 🐾"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
        multipart.addBodyPart(htmlPart);

        if (pdfBytes != null && pdfFileName != null) {
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.setDataHandler(new DataHandler(
                    new ByteArrayDataSource(pdfBytes, "application/pdf")));
            pdfPart.setFileName(pdfFileName);
            multipart.addBodyPart(pdfPart);
        }

        msg.setContent(multipart);
        Transport.send(msg);
    }

    // ─────────────────────────────────────────
    // Récupère email depuis la table users
    // ─────────────────────────────────────────
    private String getUserEmail(int userId) throws SQLException {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        }
        return null;
    }

    // ─────────────────────────────────────────
    // ByteArrayDataSource helper
    // ─────────────────────────────────────────
    private static class ByteArrayDataSource implements DataSource {
        private final byte[] data;
        private final String type;
        ByteArrayDataSource(byte[] data, String type) { this.data = data; this.type = type; }
        public InputStream getInputStream() { return new java.io.ByteArrayInputStream(data); }
        public java.io.OutputStream getOutputStream() { throw new UnsupportedOperationException(); }
        public String getContentType() { return type; }
        public String getName() { return "data"; }
    }
}