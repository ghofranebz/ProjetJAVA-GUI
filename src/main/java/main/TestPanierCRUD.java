package main;
import services.PanierService;

import java.sql.SQLException;

public class TestPanierCRUD {
    public static void main(String[] args) {
        PanierService panierService = new PanierService();

        try {

            //panierService.addToCart(4, 3, 5);
            //System.out.println("Produit ajouté au panier");


             panierService.removeFromCart(3);
             System.out.println("Produit retiré du panier");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
