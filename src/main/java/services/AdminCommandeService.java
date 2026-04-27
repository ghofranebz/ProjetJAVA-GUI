
package services;

import tools.Mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCommandeService {


    public List<String> getAll() throws SQLException {
        String sql = "SELECT * FROM commande";
        List<String> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add("Commande #" + rs.getInt("id_commande")
                        + " | User : " + rs.getInt("user_id")
                        + " | " + rs.getDate("date_commande")
                        + " | " + rs.getFloat("montant_total") + " TND"
                        + " | Statut : " + rs.getString("statut")
                        + " | Paiement : " + rs.getString("mode_paiement"));
            }
        }
        return list;
    }


    public List<String> getByStatut(String statut) throws SQLException {
        String sql = "SELECT * FROM commande WHERE statut=?";
        List<String> list = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, statut);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add("Commande #" + rs.getInt("id_commande")
                            + " | User : " + rs.getInt("user_id")
                            + " | " + rs.getFloat("montant_total") + " TND"
                            + " | Statut : " + rs.getString("statut"));
                }
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
}