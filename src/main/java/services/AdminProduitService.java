package services;

import entities.Produit;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminProduitService implements ICrud<Produit> {

    @Override
    public void add(Produit p) throws SQLException {
        throw new UnsupportedOperationException("L'admin n'ajoute pas de produits");
    }

    @Override
    public void update(Produit p) throws SQLException {
        throw new UnsupportedOperationException("Utiliser approuver() ou refuser()");
    }

    @Override
    public void delete(int produitId) throws SQLException {
        String sql = "DELETE FROM produits WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Produit> getAll() throws SQLException {
        String sql = "SELECT * FROM produits";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Produit getById(int id) throws SQLException {
        String sql = "SELECT * FROM produits WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Produit> getEnAttente() throws SQLException {
        String sql = "SELECT * FROM produits WHERE statut = 'en_attente'";
        List<Produit> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void approuver(int produitId) throws SQLException {
        String sql = "UPDATE produits SET statut='approuvé' WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
    }

    public void refuser(int produitId) throws SQLException {
        String sql = "UPDATE produits SET statut='refusé' WHERE id_produit=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, produitId);
            pst.executeUpdate();
        }
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