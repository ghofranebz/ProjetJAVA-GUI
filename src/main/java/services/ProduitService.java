package services;

import entities.Produit;
import tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class ProduitService {

    public void add(Produit p, int userId) throws SQLException {
        String sql = "INSERT INTO produits (nom_produit, description, prix, stock, date_ajout,user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setFloat(3, p.getPrix());
            pst.setInt(4, p.getStock());
            pst.setDate(5, Date.valueOf(p.getDate_ajout()));
            pst.setInt(6, userId);

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId_produit(rs.getInt(1));
                }
            }
        }
    }

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
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id_produit=?";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public List<Produit> getAll() throws SQLException {
        String sql = "SELECT * FROM produits";
        List<Produit> list = new ArrayList<>();

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Produit getById(int id) throws SQLException {
        String sql = "SELECT * FROM produits WHERE id_produit = ?";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
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