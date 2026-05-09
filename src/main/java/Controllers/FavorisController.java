package Controllers;

import entities.Favoris;
import entities.PanierItem;
import entities.Produit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import services.FavorisService;
import services.PanierService;
import services.ProduitService;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class FavorisController {

    private static final double CARD_W = 268;
    private static final double IMG_W = 236;
    private static final double IMG_H = 292;

    @FXML
    private FlowPane favorisPane;

    @FXML
    private Label favorisEmptyLabel;

    private final FavorisService favorisService = new FavorisService();
    private final ProduitService produitService = new ProduitService();
    private final PanierService panierService = new PanierService();

    private final int currentUserId = 3;

    @FXML
    public void initialize() {

        favorisPane.prefWrapLengthProperty().set(1020);

        favorisPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                favorisPane.prefWrapLengthProperty().unbind();
            }
            if (newScene != null && newScene.getWindow() != null) {
                favorisPane.prefWrapLengthProperty().bind(
                        newScene.widthProperty().subtract(140));
            }
        });

        Platform.runLater(() -> {
            if (favorisPane.getScene() != null && favorisPane.getScene().getWindow() != null) {
                favorisPane.prefWrapLengthProperty().bind(
                        favorisPane.getScene().widthProperty().subtract(140));
            }
        });

        loadFavoris();
    }

    private void loadFavoris() {

        try {

            List<Favoris> favorisList =
                    favorisService.getFavorisByUser(currentUserId);

            favorisPane.getChildren().clear();

            if (favorisList.isEmpty()) {

                setEmptyVisible(true);
                return;
            }

            setEmptyVisible(false);

            for (Favoris f : favorisList) {

                Produit p = produitService.getById(f.getProduitId());

                favorisPane.getChildren().add(createWishCard(p, f.getProduitId()));
            }

        } catch (Exception e) {

            showAlert("Erreur", "Impossible de charger les favoris.");
        }
    }

    private void setEmptyVisible(boolean empty) {

        favorisEmptyLabel.setVisible(empty);
        favorisEmptyLabel.setManaged(empty);
    }

    private VBox createWishCard(Produit p, int produitIdFallback) {

        VBox card = new VBox(14);
        card.getStyleClass().add("fashion-wish-card");
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);

        StackPane imgShell = new StackPane();
        imgShell.getStyleClass().add("fashion-wish-image-shell");
        imgShell.setPrefSize(IMG_W, IMG_H);
        imgShell.setMinSize(IMG_W, IMG_H);
        imgShell.setMaxSize(IMG_W, IMG_H);

        Rectangle clip = new Rectangle(IMG_W, IMG_H);
        clip.setArcWidth(8);
        clip.setArcHeight(8);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(IMG_W);
        imageView.setFitHeight(IMG_H);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setClip(clip);

        Label placeholder = new Label("");
        placeholder.getStyleClass().add("fashion-wish-placeholder");
        placeholder.setVisible(false);

        if (p != null && p.getImagePath() != null && !p.getImagePath().isBlank()) {

            try {

                Image image =
                        new Image(
                                "file:" + p.getImagePath().split("[;,]")[0].trim(),
                                IMG_W,
                                IMG_H,
                                true,
                                true
                        );

                imageView.setImage(image);

                image.errorProperty().addListener((obs, w, err) -> placeholder.setVisible(err));

                if (image.isError()) {
                    placeholder.setVisible(true);
                }

            } catch (Exception e) {

                placeholder.setVisible(true);
            }

        } else {

            placeholder.setVisible(true);
        }

        imgShell.getChildren().addAll(imageView, placeholder);

        Button remove = new Button();
        remove.getStyleClass().add("fashion-wish-remove");
        SVGPath trash = new SVGPath();
        trash.setContent(
                "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
        );
        trash.getStyleClass().add("icon-fill-navy");
        trash.setScaleX(0.55);
        trash.setScaleY(0.55);
        remove.setGraphic(trash);
        remove.setOnAction(e -> removeFavorite(produitIdFallback));

        StackPane.setAlignment(remove, Pos.TOP_RIGHT);
        StackPane.setMargin(remove, new Insets(10, 10, 0, 0));

        StackPane layered = new StackPane(imgShell, remove);

        String title =
                p != null ? p.getNom() : "Produit #" + produitIdFallback;

        Label name = new Label(title);
        name.getStyleClass().add("fashion-wish-name");
        name.setWrapText(true);

        float px = p != null ? p.getPrix() : 0f;
        Label price =
                new Label(
                        String.format(Locale.FRENCH, "%.2f TND", px)
                );

        price.getStyleClass().add("fashion-wish-price");

        Button addBag = new Button("AJOUTER AU PANIER");
        addBag.getStyleClass().add("btn-fashion-outline");
        addBag.setMaxWidth(Double.MAX_VALUE);
        addBag.setOnAction(e -> addProductToCart(p));

        card.getChildren().addAll(layered, name, price, addBag);

        return card;
    }

    private void addProductToCart(Produit p) {

        if (p == null) {
            return;
        }

        try {

            if (panierService.exists(currentUserId, p.getId_produit())) {

                panierService.increaseQuantity(currentUserId, p.getId_produit());

            } else {

                panierService.add(
                        new PanierItem(currentUserId, p.getId_produit(), 1)
                );
            }

            showAlert("Succès", p.getNom() + " ajouté au panier.");

        } catch (SQLException ex) {

            showAlert("Erreur", "Impossible d'ajouter au panier.");
        }
    }

    private void removeFavorite(int produitId) {

        try {

            favorisService.removeFavorite(currentUserId, produitId);

            loadFavoris();

        } catch (SQLException ex) {

            showAlert("Erreur", "Suppression impossible");
        }
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("/view/Shop.fxml")
                    );

            Parent root = loader.load();

            UiNavigation.showInMainShell(favorisPane.getScene(), root);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);

        alert.setContentText(message);

        alert.show();
    }
}
