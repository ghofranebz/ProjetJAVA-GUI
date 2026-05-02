package services;

import entities.Categorie;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCategorieService implements ICrud<Categorie> {

    @Override
    public void add(Categorie c) throws SQLException {
        String sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, c.getDescription());
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Categorie c) throws SQLException {
        String sql = "UPDATE categories SET nom=?, description=? WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, c.getNom());
            pst.setString(2, c.getDescription());
            pst.setInt(3, c.getId_categorie());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int categorieId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, categorieId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Categorie> getAll() throws SQLException {
        String sql = "SELECT * FROM categories";
        List<Categorie> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next())
                list.add(new Categorie(rs.getInt("id_categorie"), rs.getString("nom"), rs.getString("description")));
        }
        return list;
    }

    @Override
    public Categorie getById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id_categorie=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return new Categorie(rs.getInt("id_categorie"), rs.getString("nom"), rs.getString("description"));
            }
        }
        return null;
    }

    public List<Categorie> getProduitsByCategorie(int categorieId) throws SQLException {
        String sql = "SELECT * FROM produits WHERE categorie_id=? AND statut='approuvé'";
        List<Categorie> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, categorieId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    list.add(new Categorie(rs.getInt("id_categorie"), rs.getString("nom"), rs.getString("description")));
            }
        }
        return list;
    }
}