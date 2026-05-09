package services;

import entities.Produit;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements ICrud<Produit> {

    @Override
    public void add(Produit p) throws SQLException {
        String statut =
                p.getStatut() == null || p.getStatut().isBlank()
                        ? "en_attente"
                        : p.getStatut();

        String sql =
                "INSERT INTO produits (nom_produit, description, prix, stock, date_ajout, user_id, statut, image_path, categorie_id, seller_wallet_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setFloat(3, p.getPrix());
            pst.setInt(4, p.getStock());
            pst.setDate(5, Date.valueOf(p.getDate_ajout()));
            pst.setInt(6, p.getUserId());
            pst.setString(7, statut);
            pst.setString(8, p.getImagePath());

            if (p.getCategorieId() > 0) {
                pst.setInt(9, p.getCategorieId());
            } else {
                pst.setNull(9, Types.INTEGER);
            }

            String sw = p.getSellerWalletId();
            if (sw != null && !sw.isBlank()) {
                pst.setString(10, sw.trim());
            } else {
                pst.setNull(10, Types.VARCHAR);
            }

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId_produit(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Produit p) throws SQLException {
        /* Le statut reste géré par l’admin (approbation / refus). */
        String sql =
                "UPDATE produits SET nom_produit=?, description=?, prix=?, stock=?, image_path=?, categorie_id=?, seller_wallet_id=? "
                        + "WHERE id_produit=?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setFloat(3, p.getPrix());
            pst.setInt(4, p.getStock());
            pst.setString(5, p.getImagePath());

            if (p.getCategorieId() > 0) {
                pst.setInt(6, p.getCategorieId());
            } else {
                pst.setNull(6, Types.INTEGER);
            }

            String sw = p.getSellerWalletId();
            if (sw != null && !sw.isBlank()) {
                pst.setString(7, sw.trim());
            } else {
                pst.setNull(7, Types.VARCHAR);
            }

            pst.setInt(8, p.getId_produit());

            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Produit> getAll() throws SQLException {
        String sql = "SELECT * FROM produits WHERE statut = 'approuvé'";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Produit getById(int id) throws SQLException {
        String sql = "SELECT * FROM produits WHERE id_produit = ? AND statut = 'approuvé'";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Produit> getMesProduits(int userId) throws SQLException {
        String sql = "SELECT * FROM produits WHERE user_id = ?";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
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
        p.setImagePath(rs.getString("image_path"));
        p.setCategorieId(readCategorieId(rs));

        try {

            String st = rs.getString("statut");

            if (st != null) {
                p.setStatut(st);
            }

        } catch (SQLException ignored) {
        }

        try {

            p.setUserId(rs.getInt("user_id"));

        } catch (SQLException ignored) {
        }

        readSellerWalletId(rs, p);

        return p;
    }

    private static void readSellerWalletId(ResultSet rs, Produit p) {
        try {
            String w = rs.getString("seller_wallet_id");
            if (w != null && !w.isBlank()) {
                p.setSellerWalletId(w.trim());
            }
        } catch (SQLException ignored) {
        }
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