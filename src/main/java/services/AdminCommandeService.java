package services;

import entities.Commande;
import main.tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCommandeService implements ICrud<Commande> {

    @Override
    public void add(Commande c) throws SQLException {
        throw new UnsupportedOperationException("L'admin n'ajoute pas de commandes");
    }

    @Override
    public void update(Commande c) throws SQLException {
        String sql = "UPDATE commande SET statut=? WHERE id_commande=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, c.getStatut());
            pst.setInt(2, c.getId_commande());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        throw new UnsupportedOperationException("L'admin ne supprime pas de commandes");
    }

    @Override
    public List<Commande> getAll() throws SQLException {
        String sql = "SELECT * FROM commande";
        List<Commande> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Commande getById(int id) throws SQLException {
        String sql = "SELECT * FROM commande WHERE id_commande=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Commande> getByStatut(String statut) throws SQLException {
        String sql = "SELECT * FROM commande WHERE statut=?";
        List<Commande> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, statut);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void confirmer(int commandeId) throws SQLException {
        updateStatut(commandeId, "confirmée");
    }

    public void livrer(int commandeId) throws SQLException {
        updateStatut(commandeId, "livrée");
    }

    public void annuler(int commandeId) throws SQLException {
        updateStatut(commandeId, "annulée");
    }

    private void updateStatut(int commandeId, String statut) throws SQLException {
        String sql = "UPDATE commande SET statut=? WHERE id_commande=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, statut);
            pst.setInt(2, commandeId);
            pst.executeUpdate();
        }
    }

    private Commande map(ResultSet rs) throws SQLException {
        return new Commande(
                rs.getInt("id_commande"),
                rs.getInt("user_id"),
                rs.getInt("panier_id"),
                rs.getDate("date_commande").toLocalDate(),
                rs.getFloat("montant_total"),
                rs.getString("mode_paiement"),
                rs.getString("statut")
        );
    }
}