package main;

import entities.Commande;
import entities.Produit;
import services.AdminCommandeService;
import services.AdminProduitService;

import java.sql.SQLException;
import java.util.List;

public class TestAdmin {

    public static void main(String[] args) {
        AdminProduitService adminProduitService = new AdminProduitService();
        AdminCommandeService adminCommandeService = new AdminCommandeService();

        try {
            System.out.println("===== ADMIN PRODUIT =====");

            List<Produit> tousLesProduits = adminProduitService.getAll();
            System.out.println("\n Tous les produits (" + tousLesProduits.size() + ") :");
            tousLesProduits.forEach(System.out::println);

            List<Produit> enAttente = adminProduitService.getEnAttente();
            System.out.println("\n Produits en attente (" + enAttente.size() + ") :");
            enAttente.forEach(System.out::println);

            if (!enAttente.isEmpty()) {
                int produitId = enAttente.get(0).getId_produit();
                adminProduitService.approuver(produitId);
                System.out.println("\n Produit approuvé | ID = " + produitId);
            }

            List<Produit> enAttente2 = adminProduitService.getEnAttente();
            if (!enAttente2.isEmpty()) {
                int produitId = enAttente2.get(0).getId_produit();
                adminProduitService.refuser(produitId);
                System.out.println("\n Produit refusé | ID = " + produitId);
            }

            System.out.println("\n===== ADMIN COMMANDE =====");

            List<Commande> toutesLesCommandes = adminCommandeService.getAll();
            System.out.println("\n Toutes les commandes (" + toutesLesCommandes.size() + ") :");
            toutesLesCommandes.forEach(c -> System.out.println(
                    "Commande #" + c.getId_commande()
                            + " | User : " + c.getUserId()
                            + " | " + c.getMontant_total() + " TND"
                            + " | Statut : " + c.getStatut()
            ));

            List<Commande> commandesEnAttente = adminCommandeService.getByStatut("en_attente");
            System.out.println("\n Commandes en attente : " + commandesEnAttente.size());

            if (!commandesEnAttente.isEmpty()) {
                int commandeId = commandesEnAttente.get(0).getId_commande();
                adminCommandeService.confirmer(commandeId);
                System.out.println("\n Commande confirmée | ID = " + commandeId);
            }

            List<Commande> confirmees = adminCommandeService.getByStatut("confirmée");
            if (!confirmees.isEmpty()) {
                int commandeId = confirmees.get(0).getId_commande();
                adminCommandeService.livrer(commandeId);
                System.out.println("\n Commande livrée | ID = " + commandeId);
            }

            List<Commande> commandesEnAttente2 = adminCommandeService.getByStatut("en_attente");
            if (!commandesEnAttente2.isEmpty()) {
                int commandeId = commandesEnAttente2.get(0).getId_commande();
                adminCommandeService.annuler(commandeId);
                System.out.println("\n Commande annulée | ID = " + commandeId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
