package main;

import entities.User;
import services.UserService;

import java.sql.SQLException;
import java.util.List;

public class TestUserCRUD {
    public static void main(String[] args) {
        UserService userService = new UserService();

        try {
            // 1) Ajouter un utilisateur
            User user = new User("Sophie", "Martin", "sophie2@example.com", "password123", "0601020304", "CLIENT");
            userService.add(user);
            System.out.println("Utilisateur ajouté : " + user);

            // 2) Afficher tous les utilisateurs
            List<User> allUsers = userService.getAll();
            System.out.println("Tous les utilisateurs :");
            allUsers.forEach(System.out::println);

            // 3) Récupérer par id
            User found = userService.getById(user.getId());
            System.out.println("Utilisateur par id : " + found);

            // 4) Approuver l'utilisateur
            userService.approveUser(user.getId());
            System.out.println("Utilisateur approuvé.");

            // 5) Afficher les utilisateurs APPROVED
            List<User> approvedUsers = userService.getUsersByStatus("APPROVED");
            System.out.println("Utilisateurs APPROVED :");
            approvedUsers.forEach(System.out::println);

            // 6) Rejeter l'utilisateur si nécessaire
            // userService.rejectUser(user.getId());

            // 7) Mettre à jour un utilisateur
            if (found != null) {
                found.setPhone("0708091011");
                found.setStatus("APPROVED");
                userService.update(found);
                System.out.println("Utilisateur mis à jour : " + userService.getById(found.getId()));
            }

            // 8) Supprimer un utilisateur
            // userService.delete(user.getId());
            // System.out.println("Utilisateur supprimé.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}