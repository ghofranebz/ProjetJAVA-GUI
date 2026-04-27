package main;

import services.CommandeService;
import java.sql.SQLException;

public class TestCommande {

    public static void main(String[] args) {
        CommandeService commandeService = new CommandeService();

        try {

            // 1) Passer une commande (à la livraison)
            int commandeId = commandeService.passerCommande(4, 4, 250.99f, "a_la_livraison");
            System.out.println("Commande créée ! ID = " + commandeId);

            // 2) Passer une commande (en ligne)
            int commandeId2 = commandeService.passerCommande(4, 4, 89.50f, "en_ligne");
            System.out.println("Commande en ligne créée ! ID = " + commandeId2);

            // 3) Voir mes commandes
            System.out.println("\nMes commandes :");
            commandeService.getMesCommandes(4).forEach(System.out::println);

            // 4) Voir ma facture
            System.out.println("\nFacture commande #" + commandeId + " :");
            commandeService.afficherFacture(commandeId);

            // 5) Annuler une commande
            commandeService.annulerCommande(commandeId2);
            System.out.println("\nCommande #" + commandeId2 + " annulée !");

            // 6) Vérifier après annulation
            System.out.println("\nMes commandes après annulation :");
            commandeService.getMesCommandes(4).forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}