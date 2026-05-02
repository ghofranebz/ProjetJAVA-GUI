package services;

import entities.Produit;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements ICrud<Produit> {

    @Override
    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produits (nom_produit, description, prix, stock, date_ajout, user_id, statut) VALUES (?, ?, ?, ?, ?, ?, 'en_attente')";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setFloat(3, p.getPrix());
            pst.setInt(4, p.getStock());
            pst.setDate(5, Date.valueOf(p.getDate_ajout()));
            pst.setInt(6, p.getUserId());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) p.setId_produit(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nom_produit=?, description=?, prix=?, stock=? WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setFloat(3, p.getPrix());
            pst.setInt(4, p.getStock());
            pst.setInt(5, p.getId_produit());
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
        return new Produit(
                rs.getInt("id_produit"),
                rs.getString("nom_produit"),
                rs.getString("description"),
                rs.getFloat("prix"),
                rs.getInt("stock"),
                rs.getDate("date_ajout").toLocalDate()
        );
    }
}