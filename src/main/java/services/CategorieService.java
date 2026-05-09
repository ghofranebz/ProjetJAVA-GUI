package services;

import entities.Categorie;
import main.tools.Mydb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecture seule des catégories (liste pour la boutique / vendeur).
 */
public final class CategorieService {

    public List<Categorie> getAll() throws SQLException {
        String sql = "SELECT id_categorie, nom, description FROM categories ORDER BY nom";
        List<Categorie> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Categorie(
                        rs.getInt("id_categorie"),
                        rs.getString("nom"),
                        rs.getString("description")));
            }
        }
        return list;
    }

    public Categorie getById(int id) throws SQLException {
        String sql = "SELECT id_categorie, nom, description FROM categories WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Categorie(
                            rs.getInt("id_categorie"),
                            rs.getString("nom"),
                            rs.getString("description"));
                }
            }
        }
        return null;
    }
}
