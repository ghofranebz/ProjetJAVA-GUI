package services;

import tools.Mydb;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeService {


    public int passerCommande(int userId, int panierId, float montantTotal, String modePaiement) throws SQLException {
        Connection con = Mydb.getInstance().getConnection();

        String sqlCommande = "INSERT INTO commande (user_id, panier_id, date_commande, montant_total, mode_paiement, statut) VALUES (?, ?, ?, ?, ?, ?)";
        int commandeId = -1;

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
            // si en_ligne → payé immédiatement, si livraison → en_attente
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


    public List<String> getMesCommandes(int userId) throws SQLException {
        String sql = "SELECT * FROM commande WHERE user_id=?";
        List<String> list = new ArrayList<>();

        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add("Commande #" + rs.getInt("id_commande")
                            + " | " + rs.getDate("date_commande")
                            + " | " + rs.getFloat("montant_total") + " DT"
                            + " | Statut : " + rs.getString("statut")
                            + " | Paiement : " + rs.getString("mode_paiement"));
                }
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
                    System.out.println("🧾 Facture #" + rs.getInt("id_facture"));
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
}