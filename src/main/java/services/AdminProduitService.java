package services;

import entities.Produit;
import main.tools.Mydb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminProduitService implements ICrud<Produit> {

    @Override
    public void add(Produit p) throws SQLException {
        throw new UnsupportedOperationException("L'admin n'ajoute pas de produits");
    }

    @Override
    public void update(Produit p) throws SQLException {
        throw new UnsupportedOperationException("Utiliser approuver() ou refuser()");
    }

    @Override
    public void delete(int produitId) throws SQLException {
        String sql = "DELETE FROM produits WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Produit> getAll() throws SQLException {
        String sql = "SELECT * FROM produits";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Produit getById(int id) throws SQLException {
        String sql = "SELECT * FROM produits WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Produit> getEnAttente() throws SQLException {
        String sql = "SELECT * FROM produits WHERE statut = 'en_attente'";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════
    //  ✅  APPROUVER — envoie l'email aux abonnés après approbation
    // ══════════════════════════════════════════════════════════════════
    public void approuver(int produitId) throws SQLException {

        // 1. Mettre à jour le statut en DB
        String sql = "UPDATE produits SET statut='approuvé' WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }

        // 2. Récupérer les infos du produit pour l'email
        Produit produit = getById(produitId);
        if (produit == null) return;

        // 3. Envoyer l'email en arrière-plan à tous les abonnés
        new Thread(() -> {
            try {
                EmailService emailService = new EmailService();
                emailService.sendNouveauProduitEmail(
                        produit.getNom(),
                        produit.getDescription(),
                        produit.getPrix()
                );
                System.out.println("[EmailService] Alertes envoyées pour le produit : "
                        + produit.getNom());
            } catch (Exception e) {
                System.err.println("[EmailService] Erreur envoi alerte produit : "
                        + e.getMessage());
            }
        }, "email-alerte-produit").start();
    }
    // ══════════════════════════════════════════════════════════════════

    public void refuser(int produitId) throws SQLException {
        String sql = "UPDATE produits SET statut='refusé' WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    private Produit map(ResultSet rs) throws SQLException {
        Produit p = new Produit(
                rs.getInt("id_produit"),
                rs.getString("nom_produit"),
                rs.getString("description"),
                rs.getFloat("prix"),
                rs.getInt("stock"),
                rs.getDate("date_ajout").toLocalDate()
        );
        try { p.setImagePath(rs.getString("image_path")); } catch (SQLException ignored) {}
        p.setCategorieId(readCategorieId(rs));
        try {
            String st = rs.getString("statut");
            if (st != null) p.setStatut(st);
        } catch (SQLException ignored) {}
        try { p.setUserId(rs.getInt("user_id")); } catch (SQLException ignored) {}
        try {
            String w = rs.getString("seller_wallet_id");
            if (w != null && !w.isBlank()) p.setSellerWalletId(w.trim());
        } catch (SQLException ignored) {}
        return p;
    }

    private static int readCategorieId(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if ("categorie_id".equalsIgnoreCase(md.getColumnLabel(i))) {
                int v = rs.getInt(i);
                return rs.wasNull() ? 0 : v;
            }
        }
        return 0;
    }
}