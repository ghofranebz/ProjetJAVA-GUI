package main;

import controllers.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    // La fenêtre principale (Stage) accessible partout
    public static Stage mainStage;

    @Override
    public void start(Stage stage) {
        mainStage = stage;

        // Titre de la fenêtre
        stage.setTitle("Purrly — Gestion des Animaux");

        // Taille minimale de la fenêtre
        stage.setMinWidth(1000);
        stage.setMinHeight(660);

        // On crée la page de login
        LoginController login = new LoginController(stage);

        // On crée la Scene avec la vue du login
        // Scene(vue, largeur, hauteur)
        Scene scene = new Scene(login.getView(), 1100, 680);

        // On met la Scene dans le Stage
        stage.setScene(scene);

        // On affiche la fenêtre
        stage.show();
    }

    public static void main(String[] args) {
        // Lance JavaFX
        launch(args);
    }
}