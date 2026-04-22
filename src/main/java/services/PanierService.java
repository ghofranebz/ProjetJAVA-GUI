package services;
import tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PanierService {


    public void addToCart(int userId, int produitId, int quantite) throws SQLException {
        String sql = "INSERT INTO panier (user_id, id_produit, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, produitId);
            pst.setInt(3, quantite);
            pst.executeUpdate();
        }
    }


    public void removeFromCart(int idProduit) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, idProduit);
            pst.executeUpdate();
        }
    }


    public List<int[]> getCartByUser(int userId) throws SQLException {
        String sql = "SELECT id_produit, quantite FROM panier WHERE user_id = ?";
        List<int[]> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new int[]{rs.getInt("id_produit"), rs.getInt("quantite")});
                }
            }
        }
        return list;
    }


    public void updateQuantite(int userId, int produitId, int nouvelleQuantite) throws SQLException {
        String sql = "UPDATE panier SET quantite = ? WHERE user_id = ? AND id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, nouvelleQuantite);
            pst.setInt(2, userId);
            pst.setInt(3, produitId);
            pst.executeUpdate();
        }
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
                "JOIN produits p ON pa.id_produit = p.id_produit " +
                "WHERE pa.user_id = ?";
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
