package services;

import entities.Favoris;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavorisService implements ICrud<Favoris> {

    @Override
    public void add(Favoris f) throws SQLException {
        String sql = "INSERT INTO favoris (user_id, id_produit) VALUES (?, ?)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, f.getUserId());
            pst.setInt(2, f.getProduitId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int produitId) throws SQLException {
        String sql = "DELETE FROM favoris WHERE id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Favoris> getAll() throws SQLException {
        String sql = "SELECT user_id, id_produit FROM favoris";
        List<Favoris> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next())
                list.add(new Favoris(rs.getInt("user_id"), rs.getInt("id_produit")));
        }
        return list;
    }

    @Override
    public Favoris getById(int id) throws SQLException {
        throw new UnsupportedOperationException("Utiliser getFavorisByUser(userId)");
    }

    @Override
    public void update(Favoris f) throws SQLException {
        throw new UnsupportedOperationException("Les favoris ne se modifient pas");
    }

    public List<Favoris> getFavorisByUser(int userId) throws SQLException {
        String sql = "SELECT user_id, id_produit FROM favoris WHERE user_id = ?";
        List<Favoris> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    list.add(new Favoris(rs.getInt("user_id"), rs.getInt("id_produit")));
            }
        }
        return list;
    }

    public void removeFavorite(int userId, int produitId) throws SQLException {
        String sql = "DELETE FROM favoris WHERE user_id = ? AND id_produit = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, produitId);
            pst.executeUpdate();
        }
    }
}