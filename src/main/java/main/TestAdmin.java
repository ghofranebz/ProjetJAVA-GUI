package main;

import entities.Categorie;
import entities.Commande;
import entities.Produit;
import services.AdminCategorieService;
import services.AdminCommandeService;
import services.AdminProduitService;

import java.sql.SQLException;
import java.util.List;

public class TestAdmin {

    public static void main(String[] args) {
        AdminProduitService adminProduitService = new AdminProduitService();
        AdminCommandeService adminCommandeService = new AdminCommandeService();
        AdminCategorieService adminCategorieService = new AdminCategorieService();

        try {
            // ========== ADMIN PRODUIT ==========
            System.out.println("===== ADMIN PRODUIT =====");

            // 1) TOUS les produits
            List<Produit> tousLesProduits = adminProduitService.getAll();
            System.out.println("\n Tous les produits (" + tousLesProduits.size() + ") :");
            tousLesProduits.forEach(System.out::println);

            // 2) Produits en attente
            List<Produit> enAttente = adminProduitService.getEnAttente();
            System.out.println("\n Produits en attente (" + enAttente.size() + ") :");
            enAttente.forEach(System.out::println);

            // 3) Approuver le premier produit en attente
            if (!enAttente.isEmpty()) {
                int produitId = enAttente.get(0).getId_produit();
                adminProduitService.approuver(produitId);
                System.out.println("\n Produit approuvé | ID = " + produitId);
            }

            // 4) Refuser le deuxième produit en attente
            List<Produit> enAttente2 = adminProduitService.getEnAttente();
            if (!enAttente2.isEmpty()) {
                int produitId = enAttente2.get(0).getId_produit();
                adminProduitService.refuser(produitId);
                System.out.println("\n Produit refusé | ID = " + produitId);
            }

            // ========== ADMIN COMMANDE ==========
            System.out.println("\n===== ADMIN COMMANDE =====");

            // 5) Toutes les commandes
            List<Commande> toutesLesCommandes = adminCommandeService.getAll();
            System.out.println("\n Toutes les commandes (" + toutesLesCommandes.size() + ") :");
            toutesLesCommandes.forEach(c -> System.out.println(
                    "Commande #" + c.getId_commande()
                            + " | User : " + c.getUserId()
                            + " | " + c.getMontant_total() + " TND"
                            + " | Statut : " + c.getStatut()
            ));

            // 6) Commandes par statut
            List<Commande> commandesEnAttente = adminCommandeService.getByStatut("en_attente");
            System.out.println("\n Commandes en attente : " + commandesEnAttente.size());

            // 7) Confirmer une commande
            if (!commandesEnAttente.isEmpty()) {
                int commandeId = commandesEnAttente.get(0).getId_commande();
                adminCommandeService.confirmer(commandeId);
                System.out.println("\n Commande confirmée | ID = " + commandeId);
            }

            // 8) Livrer une commande confirmée
            List<Commande> confirmees = adminCommandeService.getByStatut("confirmée");
            if (!confirmees.isEmpty()) {
                int commandeId = confirmees.get(0).getId_commande();
                adminCommandeService.livrer(commandeId);
                System.out.println("\n Commande livrée | ID = " + commandeId);
            }

            // 9) Annuler une commande
            List<Commande> commandesEnAttente2 = adminCommandeService.getByStatut("en_attente");
            if (!commandesEnAttente2.isEmpty()) {
                int commandeId = commandesEnAttente2.get(0).getId_commande();
                adminCommandeService.annuler(commandeId);
                System.out.println("\n Commande annulée | ID = " + commandeId);
            }

            // ========== ADMIN CATEGORIE ==========
            System.out.println("\n===== ADMIN CATEGORIE =====");

            // 10) AJOUTER une categorie
            Categorie cat = new Categorie(0, "Electronique", "Appareils électroniques");
            adminCategorieService.add(cat);
            System.out.println("\n Catégorie ajoutée : " + cat.getNom());

            // 11) TOUTES les categories
            List<Categorie> categories = adminCategorieService.getAll();
            System.out.println("\n Toutes les catégories (" + categories.size() + ") :");
            categories.forEach(c -> System.out.println(
                    "ID=" + c.getId_categorie() + " | " + c.getNom() + " | " + c.getDescription()
            ));

            // 12) MODIFIER la derniere categorie
            if (!categories.isEmpty()) {
                Categorie toUpdate = categories.get(categories.size() - 1);
                toUpdate.setNom("Electronique & Tech");
                toUpdate.setDescription("Appareils électroniques et technologie");
                adminCategorieService.update(toUpdate);
                System.out.println("\n Catégorie modifiée | ID = " + toUpdate.getId_categorie());
            }

            // 13) SUPPRIMER la derniere categorie
            List<Categorie> apresUpdate = adminCategorieService.getAll();
            if (!apresUpdate.isEmpty()) {
                int catId = apresUpdate.get(apresUpdate.size() - 1).getId_categorie();
                adminCategorieService.delete(catId);
                System.out.println("\n Catégorie supprimée | ID = " + catId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}