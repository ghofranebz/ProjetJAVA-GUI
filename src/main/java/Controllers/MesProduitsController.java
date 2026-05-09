package Controllers;

import entities.Categorie;
import entities.Produit;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import services.CategorieService;
import services.ProduitService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MesProduitsController {

    private static final double CARD_W = 236;
    private static final double THUMB_H = 200;

    private static final String[] STATUT_DB  = {"en_attente", "approuvé", "refusé"};
    private static final String[] STATUT_UI  = {"En attente", "Approuvé", "Refusé"};

    @FXML private FlowPane      productsPane;
    @FXML private TextField     searchField;
    @FXML private Label         formHintLabel;
    @FXML private Label         fieldIdDisplay;
    @FXML private TextField     fieldNom;
    @FXML private TextArea      fieldDescription;
    @FXML private TextField     fieldPrix;
    @FXML private Spinner<Integer> fieldStock;
    @FXML private DatePicker    fieldDateAjout;
    @FXML private TextField     fieldImagePath;
    @FXML private ComboBox<Categorie> fieldCategorie;
    @FXML private TextField     fieldSellerWalletId;
    @FXML private Button        saveButton;

    private final ProduitService   produitService   = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    private final int currentUserId = 3;

    private List<Produit> produitsCache = new ArrayList<>();
    private Produit selectedProduit;
    private VBox    selectedCard;

    @FXML
    public void initialize() {
        configureStockSpinner();
        loadCategoriesCombo();
        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndRender());
        productsPane.prefWrapLengthProperty().set(820);
        productsPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) productsPane.prefWrapLengthProperty().unbind();
            if (newScene != null && newScene.getWindow() != null)
                productsPane.prefWrapLengthProperty().bind(
                        newScene.widthProperty().subtract(520));
        });
        Platform.runLater(() -> {
            if (productsPane.getScene() != null && productsPane.getScene().getWindow() != null)
                productsPane.prefWrapLengthProperty().bind(
                        productsPane.getScene().widthProperty().subtract(520));
        });
        loadMesProduits();
    }

    // ─────────────────────────────────────────
    // 🐾 Success dialog avec long-dog.gif
    // ─────────────────────────────────────────
    private void showSuccessDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Purrly 🐾");
        dialog.setResizable(true);

        ImageView gif = new ImageView(
                new Image(getClass().getResourceAsStream("/images/long-dog.gif"))
        );
        gif.setFitWidth(200);
        gif.setPreserveRatio(true);

        Label msg = new Label(
                "Ton produit a été confié aux pattes de Purrly —\n" +
                        "il sera bientôt visible en boutique !");
        msg.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-alignment: center;" +
                        "-fx-alignment: center;" +
                        "-fx-text-fill: #0e3960;" +
                        "-fx-font-weight: bold;");
        msg.setWrapText(true);
        msg.setMaxWidth(280);

        Button btnOk = new Button("Super ! 🐾");
        btnOk.setStyle(
                "-fx-background-color: #0e3960;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 28 8 28;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-cursor: hand;");
        btnOk.setOnAction(e -> dialog.close());

        VBox root = new VBox(16, gif, msg, btnOk);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");

        dialog.setScene(new Scene(root));
        dialog.show();

        // Fermeture automatique après 4 secondes
        PauseTransition auto = new PauseTransition(Duration.seconds(10));
        auto.setOnFinished(e -> dialog.close());
        auto.play();
    }

    // ─────────────────────────────────────────
    // Spinner
    // ─────────────────────────────────────────
    private void configureStockSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9_999_999, 0);
        fieldStock.setValueFactory(factory);
        fieldStock.setEditable(true);
    }

    // ─────────────────────────────────────────
    // Catégories
    // ─────────────────────────────────────────
    private void loadCategoriesCombo() {
        try {
            List<Categorie> list = categorieService.getAll();
            ObservableList<Categorie> items = FXCollections.observableArrayList();
            items.add(null);
            items.addAll(list);
            fieldCategorie.setItems(items);
            fieldCategorie.setButtonCell(categoryCell());
            fieldCategorie.setCellFactory(lv -> categoryCell());
        } catch (SQLException e) {
            fieldCategorie.setItems(FXCollections.observableArrayList());
        }
    }

    private ListCell<Categorie> categoryCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Categorie c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "— Aucune catégorie —" : c.getNom());
            }
        };
    }

    // ─────────────────────────────────────────
    // Chargement
    // ─────────────────────────────────────────
    private void loadMesProduits() {
        try {
            produitsCache = produitService.getMesProduits(currentUserId);
            applyFilterAndRender();
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement impossible.");
        }
    }

    private void applyFilterAndRender() {
        String q = searchField.getText() == null ? ""
                : searchField.getText().toLowerCase(Locale.FRENCH).trim();
        List<Produit> filtered = produitsCache.stream()
                .filter(p -> q.isEmpty() || contains(p.getNom(), q) || contains(p.getDescription(), q))
                .collect(Collectors.toList());
        productsPane.getChildren().clear();
        for (Produit p : filtered)
            productsPane.getChildren().add(createCard(p));
    }

    private static boolean contains(String text, String q) {
        return text != null && text.toLowerCase(Locale.FRENCH).contains(q);
    }

    // ─────────────────────────────────────────
    // Cards
    // ─────────────────────────────────────────
    private VBox createCard(Produit p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("mes-produit-card");
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);

        StackPane thumbArea = new StackPane();
        thumbArea.getStyleClass().add("mes-produit-thumb-shell");
        thumbArea.setPrefHeight(THUMB_H);
        thumbArea.setMinHeight(THUMB_H);

        Rectangle clip = new Rectangle(CARD_W - 28, THUMB_H - 16);
        clip.setArcWidth(10);
        clip.setArcHeight(10);

        ImageView iv = new ImageView();
        iv.setFitWidth(CARD_W - 28);
        iv.setFitHeight(THUMB_H - 16);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setClip(clip);

        Label ph = new Label("Sans visuel");
        ph.getStyleClass().add("mes-produit-thumb-ph");

        String path = p.getImagePath() == null ? ""
                : p.getImagePath().split("[;,]")[0].trim();

        if (!path.isEmpty()) {
            try {
                Image img = new Image("file:" + path, 320, 280, true, true);
                iv.setImage(img);
                img.errorProperty().addListener((obs, w, err) -> ph.setVisible(err || img.isError()));
                ph.setVisible(img.isError());
            } catch (Exception e) {
                ph.setVisible(true);
            }
        } else {
            ph.setVisible(true);
        }

        thumbArea.getChildren().addAll(iv, ph);
        StackPane.setAlignment(ph, Pos.CENTER);

        Label badge = new Label(formatStatutUi(p.getStatut()));
        badge.getStyleClass().addAll("mes-produit-badge", badgeStyleClass(p.getStatut()));

        StackPane layered = new StackPane(thumbArea, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        layered.setPickOnBounds(false);
        badge.setMouseTransparent(true);

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("mes-produit-nom");
        nom.setWrapText(true);

        Label prix = new Label(String.format(Locale.FRENCH, "%.2f TND", p.getPrix()));
        prix.getStyleClass().add("mes-produit-prix");

        Label stock = new Label("Stock · " + p.getStock());
        stock.getStyleClass().add("mes-produit-meta");

        card.getChildren().addAll(layered, nom, prix, stock);
        card.setOnMouseClicked(e -> selectCard(card, p));
        return card;
    }

    private static String formatStatutUi(String statut) {
        if (statut == null || statut.isBlank()) return "—";
        for (int i = 0; i < STATUT_DB.length; i++)
            if (STATUT_DB[i].equals(statut)) return STATUT_UI[i];
        return statut;
    }

    private static String badgeStyleClass(String statut) {
        if (statut == null) return "mes-badge-default";
        return switch (statut) {
            case "approuvé" -> "mes-badge-approuve";
            case "refusé"   -> "mes-badge-refuse";
            default         -> "mes-badge-attente";
        };
    }

    // ─────────────────────────────────────────
    // Sélection carte
    // ─────────────────────────────────────────
    private void selectCard(VBox card, Produit p) {
        if (selectedCard != null)
            selectedCard.getStyleClass().remove("mes-produit-card-selected");
        selectedCard = card;
        selectedCard.getStyleClass().add("mes-produit-card-selected");
        selectedProduit = p;
        populateForm(p);
        formHintLabel.setText("Modification · #" + p.getId_produit());
        updateSaveButtonText();
    }

    private void clearSelectionVisual() {
        if (selectedCard != null)
            selectedCard.getStyleClass().remove("mes-produit-card-selected");
        selectedCard = null;
        selectedProduit = null;
        updateSaveButtonText();
    }

    // ─────────────────────────────────────────
    // Formulaire
    // ─────────────────────────────────────────
    private void populateForm(Produit p) {
        fieldIdDisplay.setText(String.valueOf(p.getId_produit()));
        fieldNom.setText(p.getNom());
        fieldDescription.setText(p.getDescription() == null ? "" : p.getDescription());
        fieldPrix.setText(String.format(Locale.FRENCH, "%.2f", p.getPrix()));
        fieldStock.getValueFactory().setValue(Math.max(0, p.getStock()));
        fieldDateAjout.setValue(p.getDate_ajout() != null ? p.getDate_ajout() : LocalDate.now());
        fieldImagePath.setText(p.getImagePath() == null ? "" : p.getImagePath());
        fieldSellerWalletId.setText(p.getSellerWalletId());
        selectCategorieById(p.getCategorieId());
    }

    private void selectCategorieById(int categorieId) {
        if (fieldCategorie.getItems() == null) return;
        if (categorieId <= 0) { fieldCategorie.getSelectionModel().selectFirst(); return; }
        for (int i = 0; i < fieldCategorie.getItems().size(); i++) {
            Categorie c = fieldCategorie.getItems().get(i);
            if (c != null && c.getId_categorie() == categorieId) {
                fieldCategorie.getSelectionModel().select(i);
                return;
            }
        }
        fieldCategorie.getSelectionModel().selectFirst();
    }

    private void clearFormForNew() {
        clearSelectionVisual();
        fieldIdDisplay.setText("—");
        fieldNom.clear();
        fieldDescription.clear();
        fieldPrix.clear();
        fieldStock.getValueFactory().setValue(0);
        fieldDateAjout.setValue(LocalDate.now());
        fieldImagePath.clear();
        fieldSellerWalletId.clear();
        fieldCategorie.getSelectionModel().selectFirst();
        formHintLabel.setText("Nouveau produit · renseignez la fiche puis ENREGISTRER.");
        updateSaveButtonText();
    }

    private void updateSaveButtonText() {
        if (saveButton == null) return;
        saveButton.setText(selectedProduit == null ? "ENREGISTRER" : "MODIFIER");
    }

    // ─────────────────────────────────────────
    // Actions FXML
    // ─────────────────────────────────────────
    @FXML
    private void handleNouveau() { clearFormForNew(); }

    @FXML
    private void handleResetForm() {
        if (selectedProduit != null) {
            populateForm(selectedProduit);
            formHintLabel.setText("Modification · #" + selectedProduit.getId_produit());
        } else {
            clearFormForNew();
        }
    }

    @FXML
    private void handleParcourirImage() {
        Window w = fieldNom.getScene() == null ? null : fieldNom.getScene().getWindow();
        if (w == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir des images (multi-sélection)");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        List<File> selected = chooser.showOpenMultipleDialog(w);
        if (selected == null || selected.isEmpty()) return;
        String existing = fieldImagePath.getText() == null ? "" : fieldImagePath.getText().trim();
        List<String> current = existing.isEmpty() ? new ArrayList<>()
                : List.of(existing.split("[;,]")).stream().map(String::trim)
                .filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        for (File f : selected) {
            String path = f == null ? "" : f.getAbsolutePath();
            if (!path.isEmpty() && !current.contains(path)) current.add(path);
        }
        fieldImagePath.setText(String.join(";", current));
    }

    @FXML
    private void handleEnregistrer() {
        String nom = fieldNom.getText() == null ? "" : fieldNom.getText().trim();
        if (nom.isEmpty()) { showAlert("Champs requis", "Le nom du produit est obligatoire."); return; }

        float prix;
        try {
            String raw = fieldPrix.getText() == null ? "0" : fieldPrix.getText().trim().replace(',', '.');
            prix = Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            showAlert("Prix invalide", "Utilisez un nombre (ex. 12,50 ou 12.50).");
            return;
        }

        int stock        = fieldStock.getValue() == null ? 0 : fieldStock.getValue();
        LocalDate date   = fieldDateAjout.getValue() != null ? fieldDateAjout.getValue() : LocalDate.now();
        String desc      = fieldDescription.getText() == null ? "" : fieldDescription.getText().trim();
        String image     = fieldImagePath.getText() == null ? "" : fieldImagePath.getText().trim();
        String wallet    = fieldSellerWalletId.getText() == null ? "" : fieldSellerWalletId.getText().trim();
        Categorie cat    = fieldCategorie.getSelectionModel().getSelectedItem();
        int catId        = cat != null ? cat.getId_categorie() : 0;

        try {
            if (selectedProduit == null) {
                Produit nouveau = new Produit(0, nom, desc, prix, stock, date);
                nouveau.setUserId(currentUserId);
                nouveau.setImagePath(image.isEmpty() ? null : image);
                nouveau.setSellerWalletId(wallet.isEmpty() ? null : wallet);
                nouveau.setCategorieId(catId);
                nouveau.setStatut("en_attente");
                produitService.add(nouveau);
                showSuccessDialog(); // 🐾 dialog animé
            } else {
                selectedProduit.setNom(nom);
                selectedProduit.setDescription(desc);
                selectedProduit.setPrix(prix);
                selectedProduit.setStock(stock);
                selectedProduit.setDate_ajout(date);
                selectedProduit.setImagePath(image.isEmpty() ? null : image);
                selectedProduit.setSellerWalletId(wallet.isEmpty() ? null : wallet);
                selectedProduit.setCategorieId(catId);
                produitService.update(selectedProduit);
                showAlert("Succès", "Produit mis à jour.");
            }
            loadMesProduits();
            clearFormForNew();
        } catch (SQLException e) {
            showAlert("Erreur", "Enregistrement impossible (vérifiez la base SQL).");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedProduit == null) { showAlert("Attention", "Sélectionnez un produit dans la grille."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + selectedProduit.getNom() + " » ?",
                ButtonType.CANCEL,
                new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE));
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || !Objects.equals(res.get().getButtonData(), ButtonBar.ButtonData.OK_DONE)) return;
        try {
            produitService.delete(selectedProduit.getId_produit());
            loadMesProduits();
            clearFormForNew();
            showAlert("Succès", "Produit supprimé.");
        } catch (SQLException e) {
            showAlert("Erreur", "Suppression impossible.");
        }
    }

    // ─────────────────────────────────────────
    // Alert simple
    // ─────────────────────────────────────────
    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.show();
    }
}