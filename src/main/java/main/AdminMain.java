package main;

import Controllers.UiAnimations;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Fenêtre d’administration séparée (validation produits, statuts commandes).
 * Lancement : {@code mvn javafx:run@admin} ou classe principale {@link AdminMain}.
 */
public class AdminMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/view/AdminDashboard.fxml"));
        Parent uiRoot = loader.load();
        Scene scene = new Scene(uiRoot, 1120, 760);
        var css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Purrly — Administration");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        stage.show();
        Platform.runLater(() -> UiAnimations.refresh(scene));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
