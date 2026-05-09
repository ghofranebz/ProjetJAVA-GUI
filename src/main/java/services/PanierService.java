package services;

import entities.PanierItem;
import main.tools.Mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierService implements ICrud<PanierItem> {

    @Override
    public void add(PanierItem item) throws SQLException {

        String sql = "INSERT INTO panier (user_id, id_produit, quantite) VALUES (?, ?, ?)";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, item.getUserId());
            pst.setInt(2, item.getProduitId());
            pst.setInt(3, item.getQuantite());

            pst.executeUpdate();
        }
    }

    @Override
    public void update(PanierItem item) throws SQLException {

        String sql = "UPDATE panier SET quantite = ? WHERE user_id = ? AND id_produit = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, item.getQuantite());
            pst.setInt(2, item.getUserId());
            pst.setInt(3, item.getProduitId());

            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {

        throw new UnsupportedOperationException(
                "Panier : utiliser removeLine(userId, id_produit)"
        );
    }

    public void removeLine(int userId, int produitId) throws SQLException {

        String sql = "DELETE FROM panier WHERE user_id = ? AND id_produit = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setInt(2, produitId);

            pst.executeUpdate();
        }
    }

    @Override
    public List<PanierItem> getAll() throws SQLException {

        String sql = "SELECT user_id, id_produit, quantite FROM panier";

        List<PanierItem> list = new ArrayList<>();

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                list.add(new PanierItem(
                        rs.getInt("user_id"),
                        rs.getInt("id_produit"),
                        rs.getInt("quantite")
                ));
            }
        }

        return list;
    }

    @Override
    public PanierItem getById(int id) {

        throw new UnsupportedOperationException("Utilise getCartByUser(userId)");
    }

    public List<PanierItem> getCartByUser(int userId) throws SQLException {

        String sql = "SELECT user_id, id_produit, quantite FROM panier WHERE user_id = ?";

        List<PanierItem> list = new ArrayList<>();

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);

            try (ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {

                    list.add(new PanierItem(
                            rs.getInt("user_id"),
                            rs.getInt("id_produit"),
                            rs.getInt("quantite")
                    ));
                }
            }
        }

        return list;
    }

    /** Nombre total d’articles (somme des quantités) pour le badge panier. */
    public int getTotalQuantityForUser(int userId) throws SQLException {

        List<PanierItem> items = getCartByUser(userId);
        int sum = 0;
        for (PanierItem item : items) {
            sum += item.getQuantite();
        }
        return sum;
    }

    public void clearCart(int userId) throws SQLException {

        String sql = "DELETE FROM panier WHERE user_id = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);

            pst.executeUpdate();
        }
    }

    public float getTotalPanier(int userId) throws SQLException {

        String sql =
                "SELECT SUM(p.prix * pa.quantite) " +
                        "FROM panier pa " +
                        "JOIN produits p ON pa.id_produit = p.id_produit " +
                        "WHERE pa.user_id = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return rs.getFloat(1);
                }
            }
        }

        return 0;
    }

    public boolean exists(int userId, int produitId) throws SQLException {

        String sql = "SELECT 1 FROM panier WHERE user_id = ? AND id_produit = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setInt(2, produitId);

            try (ResultSet rs = pst.executeQuery()) {

                return rs.next();
            }
        }
    }

    public void increaseQuantity(int userId, int produitId) throws SQLException {

        String sql =
                "UPDATE panier SET quantite = quantite + 1 WHERE user_id = ? AND id_produit = ?";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setInt(2, produitId);

            pst.executeUpdate();
        }
    }

    public void decreaseQuantity(int userId, int produitId) throws SQLException {

        String sql =
                "UPDATE panier SET quantite = quantite - 1 WHERE user_id = ? AND id_produit = ? AND quantite > 1";

        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, userId);
            pst.setInt(2, produitId);

            pst.executeUpdate();
        }
    }
    public int getPanierIdByUser(int userId) throws SQLException {
        String sql = "SELECT id_panier FROM panier WHERE user_id = ? LIMIT 1";
        try (PreparedStatement pst =
                     Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("id_panier");
            }
        }
        return 0;
    }
}