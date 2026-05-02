package main;

import entities.PanierItem;
import services.PanierService;

import java.sql.SQLException;
import java.util.List;

public class TestPanierCRUD {

    public static void main(String[] args) {
        PanierService panierService = new PanierService();

        int userId = 3;
        int produitId = 3;

        try {
            // 1) AJOUTER au panier
            PanierItem item = new PanierItem(userId, produitId, 2);
            panierService.add(item);
            System.out.println("Item ajouté au panier : userId=" + userId + " | produitId=" + produitId);

            // 2) AFFICHER le panier de l'user
            List<PanierItem> panier = panierService.getCartByUser(userId);
            System.out.println("\n Panier de l'user " + userId + " :");
            panier.forEach(i -> System.out.println("produitId=" + i.getProduitId() + " | quantite=" + i.getQuantite()));

            // 3) MODIFIER la quantite
            item.setQuantite(5);
            panierService.update(item);
            System.out.println("\n Quantite modifiée à 5");

            // 4) TOTAL du panier
            float total = panierService.getTotalPanier(userId);
            System.out.println("\n Total panier : " + total + " TND");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}