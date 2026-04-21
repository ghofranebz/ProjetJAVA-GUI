package services;
import tools.Mydb;
import java.sql.*;

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

    public void removeFromCart(int id) throws SQLException {
        String sql = "DELETE FROM panier WHERE id_produit = ?";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
}
