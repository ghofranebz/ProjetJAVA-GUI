package main;

import entities.Produit;
import services.ProduitService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TestProduitCRUD {

    public static void main(String[] args) {
        ProduitService produitService = new ProduitService();

        try {
            // 1) AJOUTER un produit
            Produit p = new Produit(0, "Sneakers Adidas", "Chaussures sport confortables", 120.50f, 15, LocalDate.now());
            p.setUserId(3);
            produitService.add(p);
            System.out.println("Produit ajouté : " + p.getNom() + " | ID = " + p.getId_produit());

            // 2) AFFICHER tous les produits
            List<Produit> produits = produitService.getAll();
            System.out.println("\n Liste des produits :");
            produits.forEach(System.out::println);

            // 3) RÉCUPÉRER par ID
            Produit found = produitService.getById(p.getId_produit());
            System.out.println("\n Produit trouvé : " + found);

            // 4) MODIFIER produit
            if (found != null) {
                found.setPrix(135.99f);
                found.setStock(10);
                produitService.update(found);
                System.out.println("\n Produit après modification : " + produitService.getById(found.getId_produit()));
            }

            // 5) MES PRODUITS
            List<Produit> mesProduits = produitService.getMesProduits(3);
            System.out.println("\n Mes produits :");
            mesProduits.forEach(System.out::println);

            // 6) SUPPRIMER
            produitService.delete(p.getId_produit());
            System.out.println("\n Produit supprimé | ID = " + p.getId_produit());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}