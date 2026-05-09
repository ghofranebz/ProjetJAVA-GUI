package Controllers;

import entities.Commande;
import entities.Produit;
import services.EmailService;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.AdminCommandeService;
import services.AdminProduitService;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardController {

    private static final String FILTRE_TOUS    = "Tous";
    private static final String FILTRE_ATTENTE = "En attente";
    private static final String FILTRE_APPROUVE = "Approuvés";
    private static final String FILTRE_REFUSE  = "Refusés";

    private final AdminProduitService  adminProduitService  = new AdminProduitService();
    private final AdminCommandeService adminCommandeService = new AdminCommandeService();

    private ObservableList<Produit>  produitsSource   = FXCollections.observableArrayList();
    private FilteredList<Produit>    produitsFiltered;
    private List<Commande>           commandesCache   = new ArrayList<>();

    private Produit selectedProduit;
    private VBox    selectedProduitCard;
    private Commande selectedCommande;
    private VBox    selectedCommandeCard;

    @FXML private ComboBox<String> produitFiltreCombo;
    @FXML private VBox             produitsCardsBox;
    @FXML private Button           btnRefreshProduits;
    @FXML private Button           btnApprouver;
    @FXML private Button           btnRefuser;
    @FXML private VBox             commandesCardsBox;
    @FXML private ComboBox<String> commandeStatutCombo;
    @FXML private Button           btnAppliquerStatut;
    @FXML private Button           btnRefreshCommandes;
    @FXML private Label            commandeDetailLabel;
    @FXML private VBox             panelProduits;
    @FXML private VBox             panelCommandes;
    @FXML private Button           navProduits;
    @FXML private Button           navCommandes;
    @FXML private Label            adminSectionTitle;
    @FXML private Label            adminSectionSubtitle;
    @FXML private ImageView        catPeek;  // 🐱

    private EmailService emailService;

    private EmailService getEmailService() {
        if (emailService == null) {
            try { emailService = new EmailService(); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return emailService;
    }

    @FXML
    public void initialize() {
        produitFiltreCombo.setItems(FXCollections.observableArrayList(
                FILTRE_TOUS, FILTRE_ATTENTE, FILTRE_APPROUVE, FILTRE_REFUSE));
        produitFiltreCombo.setValue(FILTRE_TOUS);
        produitFiltreCombo.valueProperty().addListener((o, a, b) -> applyProduitFilter());

        produitsFiltered = new FilteredList<>(produitsSource, p -> true);

        commandeStatutCombo.setItems(FXCollections.observableArrayList(
                "en_attente", "confirmée", "livrée", "annulée"));

        navProduits.setOnAction(e -> showSectionProduits());
        navCommandes.setOnAction(e -> showSectionCommandes());

        btnRefreshProduits.setOnAction(e -> loadProduitsSilently());
        btnRefreshCommandes.setOnAction(e -> loadCommandesSilently());
        btnApprouver.setOnAction(e -> approuverSelection());
        btnRefuser.setOnAction(e -> refuserSelection());
        btnAppliquerStatut.setOnAction(e -> appliquerStatutCommande());

        commandeStatutCombo.setDisable(true);
        btnAppliquerStatut.setDisable(true);
        commandeDetailLabel.setText("Sélectionnez une commande dans la liste.");

        // Chat caché au démarrage
        catPeek.setTranslateY(160);

        showSectionProduits();
    }

    // ─────────────────────────────────────────
    // 🐱 Cat peek animation
    // ─────────────────────────────────────────
    private void triggerCatPeek() {
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), catPeek);
        slideUp.setToY(0);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), catPeek);
        slideDown.setToY(160);

        new SequentialTransition(slideUp, pause, slideDown).play();
    }

    // ─────────────────────────────────────────
    // 🔊 Meow sound
    // ─────────────────────────────────────────

    // ─────────────────────────────────────────
    // 🖼️ Agrandir image au clic
    // ─────────────────────────────────────────
    private void showEnlargedImage(Image image) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Aperçu produit");

        ImageView big = new ImageView(image);
        big.setFitWidth(500);
        big.setFitHeight(500);
        big.setPreserveRatio(true);
        big.setSmooth(true);

        StackPane root = new StackPane(big);
        root.setStyle("-fx-background-color: #111827; -fx-padding: 24;");
        root.setOnMouseClicked(e -> popup.close());

        Label hint = new Label("Cliquez pour fermer");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        root.getChildren().add(hint);

        popup.setScene(new Scene(root));
        popup.show();
    }

    // ─────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────
    private void showSectionProduits() {
        panelProduits.toFront();
        navProduits.getStyleClass().removeAll("admin-nav-active");
        navCommandes.getStyleClass().removeAll("admin-nav-active");
        navProduits.getStyleClass().add("admin-nav-active");
        adminSectionTitle.setText("Validation des produits");
        adminSectionSubtitle.setText(
                "Approuvez ou refusez les annonces des vendeurs avant publication en boutique.");
        loadProduitsSilently();
        triggerCatPeek(); // 🐱

    }

    private void showSectionCommandes() {
        panelCommandes.toFront();
        navProduits.getStyleClass().removeAll("admin-nav-active");
        navCommandes.getStyleClass().removeAll("admin-nav-active");
        navCommandes.getStyleClass().add("admin-nav-active");
        adminSectionTitle.setText("Commandes clients");
        adminSectionSubtitle.setText(
                "Mettez à jour le statut des commandes : en attente, confirmée, livrée ou annulée.");
        loadCommandesSilently();
        triggerCatPeek(); // 🐱

    }

    // ─────────────────────────────────────────
    // Helpers statut
    // ─────────────────────────────────────────
    private static String statutProduitAffiche(String raw) {
        if (raw == null || raw.isBlank()) return "—";
        return switch (raw.trim()) {
            case "en_attente" -> "En attente";
            case "approuvé"   -> "Approuvé";
            case "refusé"     -> "Refusé";
            default           -> raw;
        };
    }

    private static String badgeStyleForProduitStatut(String raw) {
        if (raw == null || raw.isBlank()) return "admin-badge-muted";
        return switch (raw.trim()) {
            case "en_attente" -> "admin-badge-attente";
            case "approuvé"   -> "admin-badge-ok";
            case "refusé"     -> "admin-badge-refus";
            default           -> "admin-badge-muted";
        };
    }

    private static String badgeStyleForCommandeStatut(String raw) {
        if (raw == null || raw.isBlank()) return "admin-badge-muted";
        return switch (raw.trim()) {
            case "en_attente" -> "admin-badge-attente";
            case "confirmée"  -> "admin-badge-ok";
            case "livrée"     -> "admin-badge-livree";
            case "annulée"    -> "admin-badge-refus";
            default           -> "admin-badge-muted";
        };
    }

    private String detailCommande(Commande c) {
        return "Commande #" + c.getId_commande()
                + " · Client #" + c.getUserId()
                + " · " + String.format(Locale.FRANCE, "%.2f DT", c.getMontant_total())
                + " · Statut actuel : " + c.getStatut();
    }

    // ─────────────────────────────────────────
    // Produits
    // ─────────────────────────────────────────
    private void applyProduitFilter() {
        final String filtre = produitFiltreCombo.getValue() != null
                ? produitFiltreCombo.getValue() : FILTRE_TOUS;
        produitsFiltered.setPredicate(p -> {
            String st = p.getStatut() != null ? p.getStatut().trim() : "";
            return switch (filtre) {
                case FILTRE_ATTENTE  -> "en_attente".equals(st);
                case FILTRE_APPROUVE -> "approuvé".equals(st);
                case FILTRE_REFUSE   -> "refusé".equals(st);
                default -> true;
            };
        });
        rebuildProduitCards();
    }

    private void rebuildProduitCards() {
        Integer idKeep = selectedProduit != null ? selectedProduit.getId_produit() : null;

        if (selectedProduitCard != null)
            selectedProduitCard.getStyleClass().remove("admin-card-selected");
        selectedProduitCard = null;
        selectedProduit = null;
        produitsCardsBox.getChildren().clear();

        if (produitsFiltered.isEmpty()) {
            Label empty = new Label("Aucune annonce pour ce filtre.");
            empty.getStyleClass().add("admin-empty");
            produitsCardsBox.getChildren().add(empty);
            updateProduitButtons(null);
            return;
        }

        for (Produit p : produitsFiltered)
            produitsCardsBox.getChildren().add(buildProduitCard(p));

        if (idKeep != null && produitsFiltered.stream().anyMatch(pr -> pr.getId_produit() == idKeep))
            selectProduitById(idKeep);
        else
            updateProduitButtons(null);

        refreshAdminCardAnimations();
    }

    private VBox buildProduitCard(Produit p) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("admin-card", "admin-produit-card");
        card.setUserData(p);
        card.setFillWidth(true);
        card.setOnMouseClicked(e -> selectProduitCard(card, p));

        ImageView thumb = buildProduitThumb(p);
        thumb.getStyleClass().add("admin-produit-thumb");

        Label idLbl = new Label("#" + p.getId_produit());
        idLbl.getStyleClass().add("admin-card-id");

        Label nomLbl = new Label(p.getNom());
        nomLbl.getStyleClass().add("admin-card-title");
        nomLbl.setWrapText(true);

        Label badge = new Label(statutProduitAffiche(p.getStatut()));
        badge.getStyleClass().addAll("admin-badge", badgeStyleForProduitStatut(p.getStatut()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rowTop = new HBox(12);
        rowTop.setAlignment(Pos.CENTER_LEFT);
        rowTop.getChildren().addAll(thumb, idLbl, nomLbl, spacer, badge);

        Label meta = new Label(String.format(Locale.FRANCE, "%.2f DT", p.getPrix())
                + "  ·  Stock " + p.getStock()
                + "  ·  Vendeur #" + p.getUserId()
                + "  ·  " + p.getDate_ajout());
        meta.getStyleClass().add("admin-card-meta");
        meta.setWrapText(true);

        String desc = p.getDescription();
        if (desc != null && desc.length() > 140) desc = desc.substring(0, 137) + "…";
        Label descLbl = new Label(desc == null || desc.isBlank() ? "—" : desc);
        descLbl.getStyleClass().add("admin-card-desc");
        descLbl.setWrapText(true);

        card.getChildren().addAll(rowTop, meta, descLbl);
        VBox.setMargin(card, new Insets(0));
        return card;
    }

    private ImageView buildProduitThumb(Produit p) {
        ImageView iv = new ImageView();
        iv.setFitWidth(44);
        iv.setFitHeight(44);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        Rectangle clip = new Rectangle(44, 44);
        clip.setArcWidth(14);
        clip.setArcHeight(14);
        iv.setClip(clip);

        String first = firstImagePath(p == null ? null : p.getImagePath());
        if (!first.isEmpty()) {
            try {
                Image img = new Image("file:" + first, 90, 90, true, true, true);
                iv.setImage(img);
                img.errorProperty().addListener((obs, wasErr, err) -> {
                    if (err || img.isError()) iv.setImage(null);
                });
                if (img.isError()) iv.setImage(null);
            } catch (Exception ignored) {
                iv.setImage(null);
            }
        }

        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseClicked(e -> {
            if (iv.getImage() != null) {
                e.consume();
                showEnlargedImage(iv.getImage());
            }
        });

        return iv;
    }

    private static String firstImagePath(String raw) {
        if (raw == null) return "";
        for (String p : raw.split("[;,]")) {
            String s = p == null ? "" : p.trim();
            if (!s.isEmpty()) return s;
        }
        return "";
    }

    private void selectProduitCard(VBox card, Produit p) {
        clearProduitSelectionVisual();
        selectedProduitCard = card;
        selectedProduit = p;
        card.getStyleClass().add("admin-card-selected");
        updateProduitButtons(p);
    }

    private void clearProduitSelectionVisual() {
        if (selectedProduitCard != null)
            selectedProduitCard.getStyleClass().remove("admin-card-selected");
        selectedProduitCard = null;
        selectedProduit = null;
    }

    private void selectProduitById(int id) {
        for (javafx.scene.Node n : produitsCardsBox.getChildren()) {
            if (n instanceof VBox box && box.getUserData() instanceof Produit pr
                    && pr.getId_produit() == id) {
                selectProduitCard(box, pr);
                return;
            }
        }
        clearProduitSelectionVisual();
        updateProduitButtons(null);
    }

    // ─────────────────────────────────────────
    // Commandes
    // ─────────────────────────────────────────
    private void rebuildCommandeCards(int keepCommandeId) {
        if (selectedCommandeCard != null)
            selectedCommandeCard.getStyleClass().remove("admin-card-selected");
        selectedCommandeCard = null;
        selectedCommande = null;
        commandesCardsBox.getChildren().clear();

        if (commandesCache.isEmpty()) {
            Label empty = new Label("Aucune commande en base.");
            empty.getStyleClass().add("admin-empty");
            commandesCardsBox.getChildren().add(empty);
            syncCommandeEditor(null);
            return;
        }

        for (Commande c : commandesCache)
            commandesCardsBox.getChildren().add(buildCommandeCard(c));

        if (keepCommandeId >= 0) selectCommandeById(keepCommandeId);
        else syncCommandeEditor(null);

        refreshAdminCardAnimations();
    }

    private void refreshAdminCardAnimations() {
        Platform.runLater(() -> {
            javafx.scene.Scene sc = panelProduits.getScene();
            if (sc != null) UiAnimations.refresh(sc);
        });
    }

    private VBox buildCommandeCard(Commande c) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("admin-card", "admin-commande-card");
        card.setUserData(c);
        card.setFillWidth(true);
        card.setOnMouseClicked(e -> selectCommandeCard(card, c));

        Label ref = new Label("Commande #" + c.getId_commande());
        ref.getStyleClass().add("admin-card-id");

        Label montant = new Label(String.format(Locale.FRANCE, "%.2f DT", c.getMontant_total()));
        montant.getStyleClass().add("admin-card-montant");

        Label badge = new Label(c.getStatut());
        badge.getStyleClass().addAll("admin-badge", badgeStyleForCommandeStatut(c.getStatut()));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().addAll(ref, montant, sp, badge);

        Label line2 = new Label("Client #" + c.getUserId()
                + "  ·  " + c.getDate_commande()
                + "  ·  Paiement : " + c.getMode_paiement());
        line2.getStyleClass().add("admin-card-meta");
        line2.setWrapText(true);

        card.getChildren().addAll(row1, line2);
        return card;
    }

    private void selectCommandeCard(VBox card, Commande c) {
        clearCommandeSelectionVisual();
        selectedCommandeCard = card;
        selectedCommande = c;
        card.getStyleClass().add("admin-card-selected");
        syncCommandeEditor(c);
    }

    private void clearCommandeSelectionVisual() {
        if (selectedCommandeCard != null)
            selectedCommandeCard.getStyleClass().remove("admin-card-selected");
        selectedCommandeCard = null;
        selectedCommande = null;
    }

    private void selectCommandeById(int commandeId) {
        for (javafx.scene.Node n : commandesCardsBox.getChildren()) {
            if (n instanceof VBox box && box.getUserData() instanceof Commande cmd
                    && cmd.getId_commande() == commandeId) {
                selectCommandeCard(box, cmd);
                return;
            }
        }
        syncCommandeEditor(null);
    }

    private void syncCommandeEditor(Commande c) {
        if (c == null) {
            commandeDetailLabel.setText("Sélectionnez une commande dans la liste.");
            commandeStatutCombo.setDisable(true);
            btnAppliquerStatut.setDisable(true);
        } else {
            commandeDetailLabel.setText(detailCommande(c));
            commandeStatutCombo.setValue(c.getStatut());
            commandeStatutCombo.setDisable(false);
            btnAppliquerStatut.setDisable(false);
        }
    }

    // ─────────────────────────────────────────
    // Load
    // ─────────────────────────────────────────
    private void loadProduitsSilently() {
        try {
            produitsSource.setAll(adminProduitService.getAll());
            applyProduitFilter();
        } catch (SQLException ex) {
            alertErreur("Chargement produits", ex);
        }
    }

    private void loadCommandesSilently() {
        int keepId = selectedCommande != null ? selectedCommande.getId_commande() : -1;
        loadCommandesSilentlyPreserving(keepId);
    }

    private void loadCommandesSilentlyPreserving(int keepCommandeId) {
        try {
            commandesCache = adminCommandeService.getAll();
            rebuildCommandeCards(keepCommandeId);
        } catch (SQLException ex) {
            alertErreur("Chargement commandes", ex);
        }
    }

    private void updateProduitButtons(Produit p) {
        boolean enAttente = p != null && "en_attente".equalsIgnoreCase(p.getStatut().trim());
        btnApprouver.setDisable(!enAttente);
        btnRefuser.setDisable(!enAttente);
    }

    private void approuverSelection() {
        Produit p = selectedProduit;
        if (p == null || !"en_attente".equalsIgnoreCase(p.getStatut().trim())) return;
        try {
            adminProduitService.approuver(p.getId_produit());
            loadProduitsSilently();
        } catch (SQLException ex) {
            alertErreur("Approbation", ex);
        }
    }

    private void refuserSelection() {
        Produit p = selectedProduit;
        if (p == null || !"en_attente".equalsIgnoreCase(p.getStatut().trim())) return;
        try {
            adminProduitService.refuser(p.getId_produit());
            loadProduitsSilently();
        } catch (SQLException ex) {
            alertErreur("Refus", ex);
        }
    }

    private void appliquerStatutCommande() {

        Commande c = selectedCommande;
        String nouveau = commandeStatutCombo.getValue();

        if (c == null || nouveau == null || nouveau.isBlank()) return;
        if (nouveau.equals(c.getStatut())) return;

        try {

            Commande updated = new Commande(
                    c.getId_commande(),
                    c.getUserId(),
                    c.getPanierId(),
                    c.getDate_commande(),
                    c.getMontant_total(),
                    c.getMode_paiement(),
                    nouveau
            );

            int commandeId = c.getId_commande();

            adminCommandeService.update(updated);

            // 📧 Envoi email
            if (getEmailService() != null) {

                getEmailService().sendStatutEmail(
                        c.getUserId(),
                        c
                );
            }

            loadCommandesSilentlyPreserving(commandeId);

        } catch (SQLException ex) {
            alertErreur("Mise à jour commande", ex);
        }
    }

    private static void alertErreur(String titre, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setHeaderText(ex.getMessage());
        a.setContentText(ex.toString());
        a.showAndWait();
    }
}