package main;

import entities.Favoris;
import services.FavorisService;

import java.sql.SQLException;
import java.util.List;

public class TestFavorisCRUD {

    public static void main(String[] args) {
        FavorisService favorisService = new FavorisService();

        int userId = 3;
        int produitId = 3;

        try {
            // 1) AJOUTER un favori
            Favoris f = new Favoris(userId, produitId);
            favorisService.add(f);
            System.out.println("Favori ajouté | userId=" + userId + " | produitId=" + produitId);

            // 2) TOUS les favoris
            List<Favoris> tousLesFavoris = favorisService.getAll();
            System.out.println("\n Tous les favoris (" + tousLesFavoris.size() + ") :");
            tousLesFavoris.forEach(fav -> System.out.println("userId=" + fav.getUserId() + " | produitId=" + fav.getProduitId()));

            // 3) FAVORIS par user
            List<Favoris> mesFavoris = favorisService.getFavorisByUser(userId);
            System.out.println("\n Mes favoris (" + mesFavoris.size() + ") :");
            mesFavoris.forEach(fav -> System.out.println("userId=" + fav.getUserId() + " | produitId=" + fav.getProduitId()));

            // 4) SUPPRIMER le favori
            favorisService.removeFavorite(userId, produitId);
            System.out.println("\n Favori supprimé | userId=" + userId + " | produitId=" + produitId);

            // 5) VERIFIER apres suppression
            List<Favoris> apresSupp = favorisService.getFavorisByUser(userId);
            System.out.println("\n Mes favoris après suppression (" + apresSupp.size() + ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}