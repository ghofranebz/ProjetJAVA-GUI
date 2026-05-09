package services;

import entities.Commande;
import entities.Facture;
import main.tools.Mydb;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements ICrud<Commande> {

    @Override
    public void add(Commande c) {
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
    public void delete(int id) {
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

    // =========================================================
    // 🔥 PASSER COMMANDE (AVEC INFOS FACTURE COMPLETES)
    // =========================================================
    public int passerCommande(
            int userId,
            int panierId,
            float montantTotal,
            String modePaiement,
            String receiverName,
            String email,
            String phone,
            String address
    ) throws SQLException {

        Connection con = Mydb.getInstance().getConnection();
        int commandeId = -1;

        // 1. INSERT COMMANDE
        String sqlCommande =
                "INSERT INTO commande (user_id, panier_id, date_commande, montant_total, mode_paiement, statut) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

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

        // 2. INSERT PAIEMENT
        String sqlPaiement =
                "INSERT INTO paiements (commande_id, methode, statut, date_paiement) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sqlPaiement)) {
            pst.setInt(1, commandeId);
            pst.setString(2, modePaiement);
            pst.setString(3, modePaiement.equals("en_ligne") ? "payé" : "en_attente");
            pst.setDate(4, Date.valueOf(LocalDate.now()));
            pst.executeUpdate();
        }

        // 3. INSERT FACTURE (🔥 AVEC INFOS CLIENT)
        String sqlFacture =
                "INSERT INTO factures " +
                        "(commande_id, date_facture, montant_total, nom_receiver, email_receiver, telephone_receiver, adresse_receiver) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sqlFacture)) {

            pst.setInt(1, commandeId);
            pst.setDate(2, Date.valueOf(LocalDate.now()));
            pst.setFloat(3, montantTotal);

            pst.setString(4, receiverName);
            pst.setString(5, email);
            pst.setString(6, phone);
            pst.setString(7, address);

            pst.executeUpdate();
        }
        System.out.println("FACTURE SAUVEGARDEE:");
        System.out.println(receiverName);
        System.out.println(email);
        System.out.println(phone);
        System.out.println(address);

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

    public Facture getFactureByCommandeId(int commandeId) throws SQLException {

        String sql =
                "SELECT id_facture, commande_id, date_facture, montant_total, " +
                        "nom_receiver, email_receiver, telephone_receiver, adresse_receiver " +
                        "FROM factures WHERE commande_id = ? ORDER BY id_facture DESC LIMIT 1";

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {

            pst.setInt(1, commandeId);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return new Facture(
                            rs.getInt("id_facture"),
                            rs.getInt("commande_id"),
                            rs.getDate("date_facture").toLocalDate(),
                            rs.getFloat("montant_total"),
                            rs.getString("nom_receiver"),
                            rs.getString("email_receiver"),
                            rs.getString("telephone_receiver"),
                            rs.getString("adresse_receiver")
                    );
                }
            }
        }
        return null;
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