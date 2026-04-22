package main;
import services.PanierService;

import java.sql.SQLException;

public class TestPanierCRUD {
    public static void main(String[] args) {
        PanierService panierService = new PanierService();

        try {

            panierService.addToCart(4, 3, 2);
            System.out.println("Produit ajouté !");

            // 2) Afficher le panier
            panierService.getCartByUser(4).forEach(item ->
                    System.out.println("Produit ID: " + item[0] + " | Quantité: " + item[1])
            );

            // 3) Modifier quantité
            panierService.updateQuantite(4, 3, 5);
            System.out.println("Quantité modifiée !");

            // 4) Total
            System.out.println("Total : " + panierService.getTotalPanier(4) + " TND");

            // 5) Supprimer un produit
            //panierService.removeFromCart(3);
            //System.out.println("Produit retiré !");

            // 6) Vider le panier
            // panierService.clearCart(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
