package Controllers;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.io.IOException;

public class GestionProduitController {

    // ✅ Instance statique accessible partout
    public static GestionProduitController instance;

    @FXML private StackPane mainContent;
    @FXML private VBox boutiqueSubMenu;
    @FXML private ImageView catPeek;

    private boolean boutiqueOpen = false;

    @FXML
    public void initialize() {
        instance = this; // ✅ enregistre l'instance
        catPeek.setTranslateY(160);
    }

    // 🐱 Cat peek animation — public pour être appelée depuis ailleurs
    public void triggerCatPeek() {
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), catPeek);
        slideUp.setToY(0);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), catPeek);
        slideDown.setToY(160);
        new SequentialTransition(slideUp, pause, slideDown).play();
    }

    // 🔊 Meow — public pour être appelée depuis ailleurs
    public void playMeow() {
        try {
            URL resource = getClass().getResource("/sounds/meow.mp3");
            Media media = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleBoutique() {
        boutiqueOpen = !boutiqueOpen;
        boutiqueSubMenu.setVisible(boutiqueOpen);
        boutiqueSubMenu.setManaged(boutiqueOpen);
    }

    @FXML
    private void handleShop() {
        loadPage("Shop.fxml");
        triggerCatPeek();
        playMeow();
    }

    @FXML
    private void handleMesProduits() {
        loadPage("MesProduits.fxml");
        triggerCatPeek();
        playMeow();
    }

    @FXML
    private void handleCommandes() {
        loadPage("Commandes.fxml");
        triggerCatPeek();
        playMeow();
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/" + fxmlFile)
            );
            Node page = loader.load();
            mainContent.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}