package main;

import entities.User;
import services.UserService;

import java.sql.SQLException;
import java.util.List;

public class TestUserCRUD {
    public static void main(String[] args) {
        UserService userService = new UserService();


        try {
                // 1) Ajouter
                User user = new User(
                        "Ghofrane", "BenZid", "ghofrane@example.com",
                        "password111", "25352124", "ADMIN"
                );
                userService.add(user);
                System.out.println("Ajouté : " + user);

                // 2) Tous les users
                List<User> all = userService.getAll();
                System.out.println("\nTous les utilisateurs :");
                all.forEach(System.out::println);

                // 3) Par ID
                User found = userService.getById(user.getId());
                System.out.println("\nPar ID : " + found);

                // 4) Approuver
                userService.approveUser(user.getId());
                System.out.println("\nApprouvé.");

                // 5) Users APPROVED
                List<User> approved = userService.getUsersByStatus("APPROVED");
                System.out.println("\nUsers APPROVED :");
                approved.forEach(System.out::println);

                // 6) Mettre à jour
                if (found != null) {
                    found.setPhone("0708091011");
                    found.setCity("Tunis");
                    found.setAddress("12 Rue des Roses");
                    userService.update(found);
                    System.out.println("\nMis à jour : "
                            + userService.getById(found.getId()));
                }

                // 7) Supprimer (décommenter si besoin)
                // userService.delete(user.getId());
                // System.out.println("Supprimé.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}