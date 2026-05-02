package main;

import entities.Commande;
import services.CommandeService;

import java.sql.SQLException;
import java.util.List;

public class TestCommandeCRUD {

    public static void main(String[] args) {
        CommandeService commandeService = new CommandeService();

        int userId = 3;
        int panierId = 17;

        try {
            // 1) PASSER une commande
            int commandeId = commandeService.passerCommande(userId, panierId, 679.95f, "livraison");
            System.out.println("Commande passée | ID = " + commandeId);

            // 2) MES COMMANDES
            List<Commande> mesCommandes = commandeService.getMesCommandes(userId);
            System.out.println("\n Mes commandes :");
            mesCommandes.forEach(c -> System.out.println(
                    "Commande #" + c.getId_commande()
                            + " | " + c.getDate_commande()
                            + " | " + c.getMontant_total() + " DT"
                            + " | Statut : " + c.getStatut()
                            + " | Paiement : " + c.getMode_paiement()
            ));

            // 3) AFFICHER FACTURE
            System.out.println("\n Facture :");
            commandeService.afficherFacture(commandeId);

            // 4) ANNULER la commande
            commandeService.annulerCommande(commandeId);
            System.out.println("\n Commande annulée | ID = " + commandeId);

            // 5) VERIFIER le statut apres annulation
            Commande apresAnnulation = commandeService.getById(commandeId);
            System.out.println("\n Statut après annulation : " + apresAnnulation.getStatut());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}