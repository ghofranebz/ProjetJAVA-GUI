package services;

import entities.Commande;
import main.tools.Mydb;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements ICrud<Commande> {

    @Override
    public void add(Commande c) throws SQLException {
        throw new UnsupportedOperationException("Utiliser passerCommande()");
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
        throw new UnsupportedOperationException("Utiliser annulerCommande()");
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

    public int passerCommande(int userId, int panierId, float montantTotal, String modePaiement) throws SQLException {
        Connection con = Mydb.getInstance().getConnection();
        int commandeId = -1;

        String sqlCommande = "INSERT INTO commande (user_id, panier_id, date_commande, montant_total, mode_paiement, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, userId);
            pst.setInt(2, panierId);
            pst.setDate(3, Date.valueOf(LocalDate.now()));
            pst.setFloat(4, montantTotal);
            pst.setString(5, modePaiement);
            pst.setString(6, "en_attente");
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) commandeId = rs.getInt(1);
            }
        }

        String sqlPaiement = "INSERT INTO paiements (commande_id, methode, statut, date_paiement) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sqlPaiement)) {
            pst.setInt(1, commandeId);
            pst.setString(2, modePaiement);
            pst.setString(3, modePaiement.equals("en_ligne") ? "payé" : "en_attente");
            pst.setDate(4, Date.valueOf(LocalDate.now()));
            pst.executeUpdate();
        }

        String sqlFacture = "INSERT INTO factures (commande_id, date_facture, montant_total) VALUES (?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sqlFacture)) {
            pst.setInt(1, commandeId);
            pst.setDate(2, Date.valueOf(LocalDate.now()));
            pst.setFloat(3, montantTotal);
            pst.executeUpdate();
        }

        return commandeId;
    }

    public List<Commande> getMesCommandes(int userId) throws SQLException {
        String sql = "SELECT * FROM commande WHERE user_id=?";
        List<Commande> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void afficherFacture(int commandeId) throws SQLException {
        String sql = "SELECT * FROM factures WHERE commande_id=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, commandeId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Facture #" + rs.getInt("id_facture"));
                    System.out.println("   Date     : " + rs.getDate("date_facture"));
                    System.out.println("   Montant  : " + rs.getFloat("montant_total") + " DT");
                }
            }
        }
    }

    public void annulerCommande(int commandeId) throws SQLException {
        String sql = "UPDATE commande SET statut='annulée' WHERE id_commande=?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, commandeId);
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