package main;
import services.FavorisService;

import java.sql.SQLException;

public class TestFavorisCRUD {
    public static void main(String[] args) {
        FavorisService favorisService = new FavorisService();

        try {

            //favorisService.addFavorite(3, 3);
            //System.out.println("Ajouté aux favoris");

            favorisService.removeFavorite(3, 3);
             System.out.println("Supprimé des favoris");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
