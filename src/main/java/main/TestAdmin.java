package main;

import services.AdminCategorieService;
import services.AdminCommandeService;
import services.AdminProduitService;
import entities.Produit;

import java.sql.SQLException;
import java.util.List;

public class TestAdmin {

    public static void main(String[] args) {

        AdminCategorieService categorieService = new AdminCategorieService();
        AdminCommandeService commandeService = new AdminCommandeService();
        AdminProduitService produitService = new AdminProduitService();

        try {

            System.out.println("===== TEST CATEGORIE =====");

            // ADD
            categorieService.add("TestCat", "Description test");

            // GET ALL
            List<String> categories = categorieService.getAll();
            categories.forEach(System.out::println);

            // UPDATE (change l'id selon ta DB)
            categorieService.update(1, "UpdatedCat", "Updated description");

            // GET PRODUITS BY CATEGORIE
            List<String> produitsCat = categorieService.getProduitsByCategorie(1);
            produitsCat.forEach(System.out::println);

            // DELETE (attention ⚠️)
            // categorieService.delete(1);


            System.out.println("\n===== TEST COMMANDE =====");

            // GET ALL
            List<String> commandes = commandeService.getAll();
            commandes.forEach(System.out::println);

            // FILTER BY STATUT
            List<String> commandesConfirmees = commandeService.getByStatut("confirmée");
            commandesConfirmees.forEach(System.out::println);

            // UPDATE STATUT
            commandeService.confirmer(1);
            commandeService.livrer(1);
            commandeService.annuler(1);


            System.out.println("\n===== TEST PRODUIT =====");

            // GET ALL
            List<Produit> produits = produitService.getAll();
            for (Produit p : produits) {
                System.out.println(p.getNom() + " | " + p.getPrix());
            }

            // PRODUITS EN ATTENTE
            List<Produit> enAttente = produitService.getEnAttente();
            for (Produit p : enAttente) {
                System.out.println("EN ATTENTE: " + p.getNom());
            }

            // APPROUVER / REFUSER / DELETE
            produitService.approuver(1);
            produitService.refuser(2);
            // produitService.supprimer(3);


        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}