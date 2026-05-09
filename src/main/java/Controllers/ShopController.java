package Controllers;

import entities.Categorie;
import entities.Favoris;
import entities.PanierItem;
import entities.Produit;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import main.tools.Mydb;
import services.CategorieService;
import services.FavorisService;
import services.PanierService;
import services.ProduitService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopController {

    private static final double CARD_WIDTH = 268;
    private static final double IMG_W      = 236;
    private static final double IMG_H      = 180;

    @FXML private FlowPane         productsPane;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private HBox             categoryBar;
    @FXML private Button           favorisHeaderButton;
    @FXML private Button           cartHeaderButton;
    @FXML private Label            cartBadgeLabel;
    @FXML private CheckBox         alerteNouveauProduit;

    private final ProduitService   produitService   = new ProduitService();
    private final PanierService    panierService    = new PanierService();
    private final FavorisService   favorisService   = new FavorisService();
    private final CategorieService categorieService = new CategorieService();

    private final int currentUserId = 3;

    private List<Produit>              productCache   = new ArrayList<>();
    private final Map<Integer, String> categoryLabels = new HashMap<>();
    private Integer selectedCategoryId = null;

    // ─────────────────────────────────────────────────────────────────
    // initialize
    // ─────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        sortCombo.setItems(FXCollections.observableArrayList(
                "Prix : défaut", "Prix : croissant", "Prix : décroissant"));

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, o, n) -> applyFilters());
        searchField.setOnAction(e -> applyFilters());

        productsPane.prefWrapLengthProperty().set(920);
        productsPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) productsPane.prefWrapLengthProperty().unbind();
            if (newScene != null && newScene.getWindow() != null)
                productsPane.prefWrapLengthProperty().bind(
                        newScene.widthProperty().subtract(96));
        });
        Platform.runLater(() -> {
            if (productsPane.getScene() != null && productsPane.getScene().getWindow() != null)
                productsPane.prefWrapLengthProperty().bind(
                        productsPane.getScene().widthProperty().subtract(96));
        });

        try {
            ensureAlerteTableExists();
            loadAlertePreference();
            loadCatalogData();
            buildCategoryBar();
            sortCombo.getSelectionModel().selectFirst();
            applyFilters();
            refreshCartBadge();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la boutique.");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Alerte nouveau produit
    // ─────────────────────────────────────────────────────────────────
    private void ensureAlerteTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS alertes_nouveau_produit (user_id INT PRIMARY KEY)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadAlertePreference() throws SQLException {
        String sql = "SELECT 1 FROM alertes_nouveau_produit WHERE user_id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, currentUserId);
            try (ResultSet rs = pst.executeQuery()) {
                if (alerteNouveauProduit != null)
                    alerteNouveauProduit.setSelected(rs.next());
            }
        }
    }

    @FXML
    private void handleAlerteCheckbox() {
        if (alerteNouveauProduit == null) return;
        boolean checked = alerteNouveauProduit.isSelected();
        new Thread(() -> {
            try {
                String sql = checked
                        ? "INSERT IGNORE INTO alertes_nouveau_produit (user_id) VALUES (?)"
                        : "DELETE FROM alertes_nouveau_produit WHERE user_id = ?";
                try (PreparedStatement pst =
                             Mydb.getInstance().getConnection().prepareStatement(sql)) {
                    pst.setInt(1, currentUserId);
                    pst.executeUpdate();
                }
                Platform.runLater(() -> showAlert(
                        checked ? "✅ Alerte activée" : "🔕 Alerte désactivée",
                        checked ? "Vous recevrez un email dès qu'un nouveau produit sera ajouté."
                                : "Vous ne recevrez plus de notifications par email."));
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Erreur", "Impossible de sauvegarder la préférence."));
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────
    // Catalogue
    // ─────────────────────────────────────────────────────────────────
    private void loadCatalogData() throws SQLException {
        categoryLabels.clear();
        for (Categorie c : categorieService.getAll())
            categoryLabels.put(c.getId_categorie(), c.getNom());
        productCache = produitService.getAll();
    }

    private void buildCategoryBar() {
        categoryBar.getChildren().clear();
        Button allBtn = new Button("Tout");
        allBtn.getStyleClass().addAll("category-pill", "category-pill-active");
        allBtn.setOnAction(e -> { selectedCategoryId = null; styleCategorySelection(allBtn); applyFilters(); });
        categoryBar.getChildren().add(allBtn);

        categoryLabels.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> {
                    int catId = entry.getKey();
                    Button btn = new Button(entry.getValue());
                    btn.getStyleClass().add("category-pill");
                    btn.setOnAction(e -> { selectedCategoryId = catId; styleCategorySelection(btn); applyFilters(); });
                    categoryBar.getChildren().add(btn);
                });
    }

    private void styleCategorySelection(Button active) {
        categoryBar.getChildren().forEach(n -> {
            if (n instanceof Button b) b.getStyleClass().remove("category-pill-active");
        });
        if (!active.getStyleClass().contains("category-pill-active"))
            active.getStyleClass().add("category-pill-active");
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? ""
                : searchField.getText().toLowerCase(Locale.FRENCH).trim();

        List<Produit> filtered = productCache.stream()
                .filter(p -> selectedCategoryId == null || p.getCategorieId() == selectedCategoryId)
                .filter(p -> query.isEmpty()
                        || containsIgnoreCase(p.getNom(), query)
                        || containsIgnoreCase(p.getDescription(), query))
                .collect(Collectors.toCollection(ArrayList::new));

        int idx = sortCombo.getSelectionModel() == null ? 0
                : Math.max(0, sortCombo.getSelectionModel().getSelectedIndex());

        switch (idx) {
            case 1 -> filtered.sort(Comparator.comparingDouble(Produit::getPrix)
                    .thenComparing(Produit::getNom, String.CASE_INSENSITIVE_ORDER));
            case 2 -> filtered.sort(Comparator.comparingDouble(Produit::getPrix).reversed()
                    .thenComparing(Produit::getNom, String.CASE_INSENSITIVE_ORDER));
            default -> filtered.sort(Comparator.comparingInt(Produit::getId_produit));
        }
        displayProducts(filtered);
    }

    @FXML private void handleSearchClick() { applyFilters(); }

    private static boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.FRENCH).contains(query);
    }

    private void displayProducts(List<Produit> produits) {
        productsPane.getChildren().clear();
        for (Produit p : produits)
            productsPane.getChildren().add(createProductCard(p));
    }

    // ─────────────────────────────────────────────────────────────────
    // Carte produit  —  FIX HOVER SHAKE ICI
    // ─────────────────────────────────────────────────────────────────
    private Parent createProductCard(Produit p) {

        VBox card = new VBox(12);
        card.getStyleClass().add("shop-product-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);

        List<String> paths = parseImagePaths(p.getImagePath());
        ImageAreaParts imgParts = buildImageArea(p, paths);

        Label badge = createBadgeIfNeeded(p);
        StackPane layeredTop = new StackPane(imgParts.stack());
        if (badge != null) {
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(10, 0, 0, 10));
            layeredTop.getChildren().add(badge);
        }

        Button heart = buildHeartButton(p);
        StackPane.setAlignment(heart, Pos.TOP_RIGHT);
        StackPane.setMargin(heart, new Insets(8, 8, 0, 0));
        layeredTop.getChildren().add(heart);

        Label hoverHint = new Label("Voir détails");
        hoverHint.getStyleClass().add("shop-hover-hint");
        hoverHint.setMouseTransparent(true);
        StackPane.setAlignment(hoverHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hoverHint, new Insets(0, 0, 8, 0));
        layeredTop.getChildren().add(hoverHint);

        VBox expanded = buildExpandedHoverPanel(p, paths, imgParts.imageView(), imgParts.placeholder());

        String cat = p.getCategorieId() > 0
                ? categoryLabels.getOrDefault(p.getCategorieId(), "Sans catégorie") : "Sans catégorie";
        Label catLbl = new Label(cat.toUpperCase(Locale.FRENCH));
        catLbl.getStyleClass().add("shop-product-category");

        Label nom = new Label(p.getNom());
        nom.getStyleClass().add("shop-product-title");
        nom.setWrapText(true);

        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Label prix = new Label(String.format(Locale.FRENCH, "%.2f TND", p.getPrix()));
        prix.getStyleClass().add("shop-product-price");
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        Button addCart = new Button("AJOUTER");
        addCart.getStyleClass().add("btn-add-cart");
        addCart.setOnAction(e -> { addToPanier(p); e.consume(); });
        bottom.getChildren().addAll(prix, grow, addCart);

        Label stock = new Label("Stock : " + p.getStock());
        stock.getStyleClass().add("shop-product-stock");

        card.getChildren().addAll(layeredTop, expanded, catLbl, nom, bottom, stock);

        VBox slot = new VBox();
        slot.getStyleClass().add("shop-product-card-slot");
        slot.setFillWidth(false);
        slot.getChildren().add(card);

        // ✅ FIX : scale appliqué sur `slot`, pas sur `card`
        //    → la hitbox souris grandit avec la carte → plus de boucle mouseExit/Enter
        ParallelTransition hoverIn  = buildCardHoverIn(slot, expanded, hoverHint);
        ParallelTransition hoverOut = buildCardHoverOut(slot, expanded, hoverHint);

        hoverHint.setVisible(false);
        hoverHint.setOpacity(0);

        slot.setOnMouseEntered(e -> {
            // ✅ FIX : slot.toFront() supprimé → évite le réordonnancement du FlowPane
            hoverOut.stop();
            expanded.setManaged(true);
            expanded.setVisible(true);
            expanded.setOpacity(0);
            hoverHint.setVisible(true);
            hoverHint.setOpacity(0);
            hoverIn.playFromStart();
        });

        slot.setOnMouseExited(e -> {
            hoverIn.stop();
            syncHoverOutAnimations(slot, expanded, hoverHint, hoverOut);
            hoverOut.playFromStart();
        });

        return slot;
    }

    // ✅ FIX : paramètre `slot` (VBox) au lieu de `card`
    private static void syncHoverOutAnimations(
            VBox slot, VBox expanded, Label hoverHint, ParallelTransition hoverOut) {

        for (var anim : hoverOut.getChildren()) {
            if (anim instanceof ScaleTransition st) {
                st.setFromX(Math.max(1, slot.getScaleX()));
                st.setFromY(Math.max(1, slot.getScaleY()));
                st.setToX(1);
                st.setToY(1);
            } else if (anim instanceof FadeTransition ft) {
                if      (ft.getNode() == expanded)   ft.setFromValue(expanded.getOpacity());
                else if (ft.getNode() == hoverHint)  ft.setFromValue(hoverHint.getOpacity());
            }
        }
    }

    // ✅ FIX : cible `slot` — scale 1.04 (plus subtil que 1.06)
    private ParallelTransition buildCardHoverIn(VBox slot, VBox expanded, Label hoverHint) {

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), slot);
        scale.setFromX(1);  scale.setFromY(1);
        scale.setToX(1.04); scale.setToY(1.04);

        FadeTransition fadeHint = new FadeTransition(Duration.millis(160), hoverHint);
        fadeHint.setFromValue(0); fadeHint.setToValue(1);

        FadeTransition fadeExp = new FadeTransition(Duration.millis(220), expanded);
        fadeExp.setFromValue(0); fadeExp.setToValue(1);

        ParallelTransition pt = new ParallelTransition(scale, fadeHint, fadeExp);
        pt.setOnFinished(ev -> { expanded.setOpacity(1); hoverHint.setOpacity(1); });
        return pt;
    }

    // ✅ FIX : cible `slot`
    private ParallelTransition buildCardHoverOut(VBox slot, VBox expanded, Label hoverHint) {

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), slot);
        scale.setToX(1); scale.setToY(1);

        FadeTransition fadeHint = new FadeTransition(Duration.millis(140), hoverHint);
        fadeHint.setFromValue(1); fadeHint.setToValue(0);

        FadeTransition fadeExp = new FadeTransition(Duration.millis(160), expanded);
        fadeExp.setFromValue(1); fadeExp.setToValue(0);

        ParallelTransition pt = new ParallelTransition(scale, fadeHint, fadeExp);
        pt.setOnFinished(ev -> {
            expanded.setManaged(false);
            expanded.setVisible(false);
            expanded.setOpacity(0);
            hoverHint.setVisible(false);
            hoverHint.setOpacity(0);
            slot.setScaleX(1); // ✅ reset slot
            slot.setScaleY(1);
        });
        return pt;
    }

    // ─────────────────────────────────────────────────────────────────
    // Image helpers
    // ─────────────────────────────────────────────────────────────────
    private record ImageAreaParts(StackPane stack, ImageView imageView, Label placeholder) {}

    private ImageAreaParts buildImageArea(Produit p, List<String> paths) {
        StackPane stack = new StackPane();
        stack.setPrefSize(IMG_W, IMG_H); stack.setMinSize(IMG_W, IMG_H); stack.setMaxSize(IMG_W, IMG_H);
        stack.getStyleClass().add("shop-image-shell");

        Rectangle clip = new Rectangle(IMG_W, IMG_H);
        clip.setArcWidth(16); clip.setArcHeight(16);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(IMG_W); imageView.setFitHeight(IMG_H);
        imageView.setPreserveRatio(true); imageView.setSmooth(true);
        imageView.setClip(clip);

        Label placeholder = new Label("📷");
        placeholder.getStyleClass().add("shop-image-placeholder");
        placeholder.setVisible(false);

        if (!paths.isEmpty()) {
            try {
                Image image = new Image("file:" + paths.get(0), IMG_W, IMG_H, true, true);
                imageView.setImage(image);
                image.errorProperty().addListener((obs, wasErr, err) -> placeholder.setVisible(err));
                if (image.isError()) placeholder.setVisible(true);
            } catch (Exception e) { placeholder.setVisible(true); }
        } else { placeholder.setVisible(true); }

        stack.getChildren().addAll(imageView, placeholder);

        if (paths.size() > 1) {
            HBox thumbs = buildThumbnails(paths, imageView, placeholder);
            StackPane.setAlignment(thumbs, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(thumbs, new Insets(0, 8, 8, 0));
            stack.getChildren().add(thumbs);
        }
        return new ImageAreaParts(stack, imageView, placeholder);
    }

    private VBox buildExpandedHoverPanel(
            Produit p, List<String> paths, ImageView mainView, Label placeholder) {

        VBox box = new VBox(10);
        box.getStyleClass().add("shop-card-expanded");
        box.setMaxWidth(CARD_WIDTH);
        box.setOpacity(0); box.setVisible(false); box.setManaged(false);

        if (!paths.isEmpty()) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            int shown = 0;
            for (String path : paths) {
                if (shown >= 8) break;
                ImageView thumb = new ImageView();
                thumb.setFitWidth(56); thumb.setFitHeight(42);
                thumb.setPreserveRatio(true); thumb.setSmooth(true);
                thumb.getStyleClass().add("shop-expanded-thumb");
                try { thumb.setImage(new Image("file:" + path, 112, 84, true, true)); }
                catch (Exception ignored) {}
                final String usePath = path;
                thumb.setOnMouseClicked(ev -> {
                    ev.consume();
                    try {
                        Image img = new Image("file:" + usePath, IMG_W, IMG_H, true, true);
                        mainView.setImage(img); placeholder.setVisible(img.isError());
                    } catch (Exception ex) { placeholder.setVisible(true); }
                });
                row.getChildren().add(thumb); shown++;
            }
            ScrollPane sp = new ScrollPane(row);
            sp.setFitToHeight(true);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sp.setPrefHeight(54); sp.setMaxHeight(54); sp.setMinHeight(54);
            sp.getStyleClass().add("shop-expanded-scroll");
            box.getChildren().add(sp);
        }

        String fullDesc = p.getDescription() == null || p.getDescription().isBlank()
                ? "Pas de description détaillée pour ce produit." : p.getDescription().trim();
        Label desc = new Label(fullDesc);
        desc.setWrapText(true); desc.setMaxWidth(CARD_WIDTH - 4);
        desc.getStyleClass().add("shop-expanded-desc");

        Label meta = new Label(String.format(Locale.FRENCH, "Réf. #%d  ·  Ajout %s  ·  %d photo(s)",
                p.getId_produit(), p.getDate_ajout(), paths.size()));
        meta.getStyleClass().add("shop-expanded-meta"); meta.setWrapText(true);

        box.getChildren().addAll(desc, meta);
        return box;
    }

    private static List<String> parseImagePaths(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> out = new ArrayList<>();
        for (String p : raw.split("[;,]")) {
            String s = p == null ? "" : p.trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private HBox buildThumbnails(List<String> paths, ImageView target, Label placeholder) {
        HBox box = new HBox(6); box.getStyleClass().add("shop-thumbs");
        int limit = Math.min(paths.size(), 4);
        for (int i = 0; i < limit; i++) {
            String p = paths.get(i);
            ImageView thumb = new ImageView();
            thumb.setFitWidth(30); thumb.setFitHeight(22);
            thumb.setPreserveRatio(true); thumb.setSmooth(true);
            thumb.getStyleClass().add("shop-thumb");
            try { thumb.setImage(new Image("file:" + p, 60, 44, true, true)); }
            catch (Exception ignored) {}
            final String usePath = p;
            thumb.setOnMouseEntered(e -> {
                try {
                    Image img = new Image("file:" + usePath, IMG_W, IMG_H, true, true);
                    target.setImage(img); placeholder.setVisible(img.isError());
                } catch (Exception ex) { placeholder.setVisible(true); }
            });
            box.getChildren().add(thumb);
        }
        return box;
    }

    private Label createBadgeIfNeeded(Produit p) {
        LocalDate ajout = p.getDate_ajout();
        if (ajout == null) return null;
        long days = ChronoUnit.DAYS.between(ajout, LocalDate.now());
        if (days >= 0 && days <= 45) {
            Label badge = new Label("Nouveau");
            badge.getStyleClass().add("shop-badge-new");
            return badge;
        }
        return null;
    }

    private Button buildHeartButton(Produit p) {
        Button btn = new Button();
        btn.getStyleClass().add("shop-card-fav-btn");
        btn.setFocusTraversable(false);
        SVGPath heart = new SVGPath();
        heart.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 "
                + "7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 "
                + "19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");
        heart.getStyleClass().add("shop-card-heart-svg");
        heart.setScaleX(0.82); heart.setScaleY(0.82);
        btn.setGraphic(heart);
        btn.setOnAction(e -> { addToFavoris(p); e.consume(); });
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────
    // Panier / Favoris / Navigation
    // ─────────────────────────────────────────────────────────────────
    private void addToPanier(Produit p) {
        try {
            if (panierService.exists(currentUserId, p.getId_produit()))
                panierService.increaseQuantity(currentUserId, p.getId_produit());
            else panierService.add(new PanierItem(currentUserId, p.getId_produit(), 1));
            refreshCartBadge();
            showAlert("Succès", p.getNom() + " ajouté au panier !");
        } catch (SQLException e) { showAlert("Erreur", "Impossible d'ajouter au panier."); }
    }

    private void addToFavoris(Produit p) {
        try {
            favorisService.add(new Favoris(currentUserId, p.getId_produit()));
            showAlert("Succès", p.getNom() + " ajouté aux favoris !");
        } catch (SQLException e) { showAlert("Erreur", "Impossible d'ajouter aux favoris."); }
    }

    private void refreshCartBadge() {
        try {
            int qty = panierService.getTotalQuantityForUser(currentUserId);
            if (cartBadgeLabel != null) {
                cartBadgeLabel.setText(String.valueOf(qty));
                cartBadgeLabel.setVisible(qty > 0);
                cartBadgeLabel.setManaged(qty > 0);
            }
        } catch (SQLException e) {
            if (cartBadgeLabel != null) {
                cartBadgeLabel.setText("0");
                cartBadgeLabel.setVisible(false);
                cartBadgeLabel.setManaged(false);
            }
        }
    }

    @FXML private void goToFavoris() { openPage("Favoris.fxml"); }
    @FXML private void goToPanier()  { openPage("Panier.fxml"); }

    private void openPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent page = loader.load();
            UiNavigation.showInMainShell(productsPane.getScene(), page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message); alert.show();
    }
}