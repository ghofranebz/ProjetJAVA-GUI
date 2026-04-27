
package services;

import tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCategorieService {


    public void add(String nom, String description) throws SQLException {
        String sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, nom);
            pst.setString(2, description);
            pst.executeUpdate();
        }
    }


    public List<String> getAll() throws SQLException {
        String sql = "SELECT * FROM categories";
        List<String> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add("ID : " + rs.getInt("id_categorie")
                        + " | " + rs.getString("nom")
                        + " | " + rs.getString("description"));
            }
        }
        return list;
    }


    public void update(int categorieId, String nom, String description) throws SQLException {
        String sql = "UPDATE categories SET nom=?, description=? WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, nom);
            pst.setString(2, description);
            pst.setInt(3, categorieId);
            pst.executeUpdate();
        }
    }


    public void delete(int categorieId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, categorieId);
            pst.executeUpdate();
        }
    }


    public List<String> getProduitsByCategorie(int categorieId) throws SQLException {
        String sql = "SELECT * FROM produits WHERE categorie_id=? AND statut='approuvé'";
        List<String> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, categorieId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add("Produit : " + rs.getString("nom")
                            + " | Prix : " + rs.getFloat("prix") + " TND"
                            + " | Stock : " + rs.getInt("stock"));
                }
            }
        }
        return list;
    }
}