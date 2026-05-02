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
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, item.getUserId());
            pst.setInt(2, item.getProduitId());
            pst.setInt(3, item.getQuantite());
            pst.executeUpdate();
        }
    }

    @Override
    public void update(PanierItem item) throws SQLException {
        String sql = "UPDATE panier SET quantite = ? WHERE user_id = ? AND id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, item.getQuantite());
            pst.setInt(2, item.getUserId());
            pst.setInt(3, item.getProduitId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int produitId) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<PanierItem> getAll() throws SQLException {
        String sql = "SELECT user_id, id_produit, quantite FROM panier";
        List<PanierItem> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next())
                list.add(new PanierItem(rs.getInt("user_id"), rs.getInt("id_produit"), rs.getInt("quantite")));
        }
        return list;
    }

    @Override
    public PanierItem getById(int id) throws SQLException {
        throw new UnsupportedOperationException("Utiliser getCartByUser(userId)");
    }

    public List<PanierItem> getCartByUser(int userId) throws SQLException {
        String sql = "SELECT user_id, id_produit, quantite FROM panier WHERE user_id = ?";
        List<PanierItem> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    list.add(new PanierItem(rs.getInt("user_id"), rs.getInt("id_produit"), rs.getInt("quantite")));
            }
        }
        return list;
    }

    public void clearCart(int userId) throws SQLException {
        String sql = "DELETE FROM panier WHERE user_id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.executeUpdate();
        }
    }

    public float getTotalPanier(int userId) throws SQLException {
        String sql = "SELECT SUM(p.prix * pa.quantite) FROM panier pa " +
                "JOIN produits p ON pa.id_produit = p.id_produit WHERE pa.user_id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getFloat(1);
            }
        }
        return 0;
    }

    public int getPanierId(int userId) throws SQLException {
        String sql = "SELECT id_panier FROM panier WHERE user_id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }
}