package services;
import tools.Mydb;

import java.sql.*;


public class FavorisService {
    public void addFavorite(int user_id, int id_produit) throws SQLException {
        String sql = "INSERT INTO favoris (user_id, id_produit) VALUES (?, ?)";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, user_id);
            pst.setInt(2, id_produit);

            pst.executeUpdate();
        }
    }

    public void removeFavorite(int user_id, int id_produit) throws SQLException {
        String sql = "DELETE FROM favoris WHERE user_id=? AND id_produit=?";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, user_id);
            pst.setInt(2, id_produit);

            pst.executeUpdate();
        }
    }

}
