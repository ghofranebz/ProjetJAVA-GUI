package Controllers;

import entities.Commande;
import entities.Facture;
import entities.Produit;
import services.EmailService;
import services.FacturePdfService;
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
import javafx.scene.control.TextField;
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
import main.tools.Mydb;
import services.AdminCommandeService;
import services.AdminProduitService;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminDashboardController {

    private static final String FILTRE_TOUS     = "Tous";
    private static final String FILTRE_ATTENTE  = "En attente";
    private static final String FILTRE_APPROUVE = "Approuvés";
    private static final String FILTRE_REFUSE   = "Refusés";

    private final AdminProduitService  adminProduitService  = new AdminProduitService();
    private final AdminCommandeService adminCommandeService = new AdminCommandeService();

    private ObservableList<Produit> produitsSource   = FXCollections.observableArrayList();
    private FilteredList<Produit>   produitsFiltered;
    private List<Commande>          commandesCache   = new ArrayList<>();

    // ══ Factures cache ══
    private List<FactureRow> facturesCache = new ArrayList<>();

    private Produit  selectedProduit;
    private VBox     selectedProduitCard;
    private Commande selectedCommande;
    private VBox     selectedCommandeCard;

    // ── FXML existants ──
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
    @FXML private ImageView        catPeek;

    // ── FXML nouveaux (Factures) ──
    @FXML private VBox      panelFactures;
    @FXML private Button    navFactures;
    @FXML private Button    btnRefreshFactures;
    @FXML private VBox      facturesCardsBox;
    @FXML private TextField factureSearchField;

    private EmailService emailService;
    private final FacturePdfService pdfService = new FacturePdfService();

    private EmailService getEmailService() {
        if (emailService == null) {
            try { emailService = new EmailService(); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return emailService;
    }

    // ═══════════════════════════════════════════════════
    // initialize
    // ═══════════════════════════════════════════════════
    @FXML
    public void initialize() {
        produitFiltreCombo.setItems(FXCollections.observableArrayList(
                FILTRE_TOUS, FILTRE_ATTENTE, FILTRE_APPROUVE, FILTRE_REFUSE));
        produitFiltreCombo.setValue(FILTRE_TOUS);
        produitFiltreCombo.valueProperty().addListener((o, a, b) -> applyProduitFilter());

        produitsFiltered = new FilteredList<>(produitsSource, p -> true);

        commandeStatutCombo.setItems(FXCollections.observableArrayList(
                "en_attente", "confirmée", "livrée", "annulée"));

        // navigation
        navProduits.setOnAction(e  -> showSectionProduits());
        navCommandes.setOnAction(e -> showSectionCommandes());
        navFactures.setOnAction(e  -> showSectionFactures());   // ← NOUVEAU

        btnRefreshProduits.setOnAction(e  -> loadProduitsSilently());
        btnRefreshCommandes.setOnAction(e -> loadCommandesSilently());
        btnRefreshFactures.setOnAction(e  -> loadFacturesSilently());  // ← NOUVEAU

        btnApprouver.setOnAction(e        -> approuverSelection());
        btnRefuser.setOnAction(e          -> refuserSelection());
        btnAppliquerStatut.setOnAction(e  -> appliquerStatutCommande());

        commandeStatutCombo.setDisable(true);
        btnAppliquerStatut.setDisable(true);
        commandeDetailLabel.setText("Sélectionnez une commande dans la liste.");

        catPeek.setTranslateY(160);

        showSectionProduits();
    }

    // ═══════════════════════════════════════════════════
    // ══  SECTION FACTURES  ══════════════════════════
    // ═══════════════════════════════════════════════════

    /** Modèle local : une facture + sa commande associée */
    public record FactureRow(Facture facture, Commande commande, String clientEmail) {}

    private void showSectionFactures() {
        panelFactures.toFront();
        navProduits.getStyleClass().remove("admin-nav-active");
        navCommandes.getStyleClass().remove("admin-nav-active");
        navFactures.getStyleClass().remove("admin-nav-active");
        navFactures.getStyleClass().add("admin-nav-active");
        adminSectionTitle.setText("Factures clients");
        adminSectionSubtitle.setText(
                "Consultez et imprimez toutes les factures générées pour les commandes clients.");
        loadFacturesSilently();
        triggerCatPeek();
    }

    private void loadFacturesSilently() {
        try {
            facturesCache = getAllFactureRows();
            applyFactureSearch(factureSearchField == null ? "" : factureSearchField.getText());
        } catch (SQLException ex) {
            alertErreur("Chargement factures", ex);
        }
    }

    @FXML
    private void handleFactureSearch() {
        String txt = factureSearchField == null ? "" : factureSearchField.getText();
        applyFactureSearch(txt);
    }

    @FXML
    private void handleFactureClear() {
        if (factureSearchField != null) factureSearchField.clear();
        applyFactureSearch("");
    }

    private void applyFactureSearch(String raw) {
        String q = raw == null ? "" : raw.trim().toLowerCase(Locale.FRENCH);
        List<FactureRow> filtered = q.isEmpty() ? new ArrayList<>(facturesCache)
                : facturesCache.stream().filter(r ->
                String.valueOf(r.facture().getId_facture()).contains(q)
                        || String.valueOf(r.commande().getId_commande()).contains(q)
                        || String.valueOf(r.commande().getUserId()).contains(q)
                        || (r.clientEmail() != null && r.clientEmail().toLowerCase(Locale.FRENCH).contains(q))
                        || (r.facture().getNom_receiver() != null
                        && r.facture().getNom_receiver().toLowerCase(Locale.FRENCH).contains(q))
        ).collect(Collectors.toList());
        rebuildFactureCards(filtered);
    }

    private void rebuildFactureCards(List<FactureRow> rows) {
        facturesCardsBox.getChildren().clear();
        if (rows.isEmpty()) {
            Label empty = new Label("Aucune facture trouvée.");
            empty.getStyleClass().add("admin-empty");
            facturesCardsBox.getChildren().add(empty);
            return;
        }
        for (FactureRow row : rows)
            facturesCardsBox.getChildren().add(buildFactureCard(row));
    }

    private VBox buildFactureCard(FactureRow row) {
        Facture  f = row.facture();
        Commande c = row.commande();

        VBox card = new VBox(10);
        card.getStyleClass().add("admin-card");
        card.setFillWidth(true);

        // ── Ligne 1 : refs + badge statut ──
        Label facRef = new Label("Facture #" + fmt(f.getId_facture()));
        facRef.getStyleClass().add("admin-card-id");

        Label cmdRef = new Label("· Commande #" + fmt(c.getId_commande()));
        cmdRef.getStyleClass().add("admin-card-meta");

        Label montant = new Label(String.format(Locale.FRANCE, "%.2f TND", f.getMontant_total()));
        montant.getStyleClass().add("admin-card-montant");

        Label badge = new Label(c.getStatut());
        badge.getStyleClass().addAll("admin-badge", badgeStyleForCommandeStatut(c.getStatut()));

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().addAll(facRef, cmdRef, montant, sp1, badge);

        // ── Ligne 2 : infos client ──
        Label meta = new Label(
                "Client #" + c.getUserId()
                        + "  ·  " + safe(f.getNom_receiver())
                        + "  ·  " + safe(row.clientEmail())
                        + "  ·  " + safe(f.getTelephone_receiver())
                        + "  ·  " + safe(f.getAdresse_receiver()));
        meta.getStyleClass().add("admin-card-meta");
        meta.setWrapText(true);

        // ── Ligne 3 : dates ──
        Label dates = new Label(
                "Date facture : " + f.getDate_facture()
                        + "   ·   Date commande : " + c.getDate_commande()
                        + "   ·   Paiement : " + c.getMode_paiement());
        dates.getStyleClass().add("admin-card-desc");

        // ── Bouton Imprimer ──
        Button btnPrint = new Button("🖨  Imprimer PDF");
        btnPrint.getStyleClass().add("btn-primary");
        btnPrint.setOnAction(e -> handlePrint(row, btnPrint));

        HBox actionRow = new HBox(btnPrint);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(row1, meta, dates, actionRow);
        return card;
    }

    private void handlePrint(FactureRow row, Button btn) {
        btn.setDisable(true);
        btn.setText("Génération…");
        new Thread(() -> {
            try {
                byte[] pdf = pdfService.generateFacturePdf(
                        row.facture(), row.commande(), new ArrayList<>());

                // Sauvegarde dans un fichier temporaire
                File tmp = File.createTempFile(
                        "facture_" + row.facture().getId_facture() + "_", ".pdf");
                tmp.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tmp)) {
                    fos.write(pdf);
                }

                // Ouvre avec le lecteur PDF du système (qui permet d'imprimer)
                Desktop.getDesktop().open(tmp);

                Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("🖨  Imprimer PDF");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("❌ Erreur");
                    alertErreur("Impression facture", ex);
                });
            }
        }, "print-facture").start();
    }

    /**
     * Charge toutes les factures jointes avec leurs commandes depuis la DB.
     */
    private List<FactureRow> getAllFactureRows() throws SQLException {
        String sql =
                "SELECT f.id_facture, f.commande_id, f.date_facture, f.montant_total, " +
                        "       f.nom_receiver, f.email_receiver, f.telephone_receiver, f.adresse_receiver, " +
                        "       c.id_commande, c.user_id, c.panier_id, c.date_commande, " +
                        "       c.montant_total AS cmd_montant, c.mode_paiement, c.statut, " +
                        "       u.email AS client_email " +
                        "FROM factures f " +
                        "JOIN commande c ON c.id_commande = f.commande_id " +
                        "LEFT JOIN users u ON u.id = c.user_id " +
                        "ORDER BY f.id_facture DESC";

        List<FactureRow> rows = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Facture facture = new Facture(
                        rs.getInt("id_facture"),
                        rs.getInt("commande_id"),
                        rs.getDate("date_facture").toLocalDate(),
                        rs.getFloat("montant_total"),
                        rs.getString("nom_receiver"),
                        rs.getString("email_receiver"),
                        rs.getString("telephone_receiver"),
                        rs.getString("adresse_receiver")
                );
                Commande commande = new Commande(
                        rs.getInt("id_commande"),
                        rs.getInt("user_id"),
                        rs.getInt("panier_id"),
                        rs.getDate("date_commande").toLocalDate(),
                        rs.getFloat("cmd_montant"),
                        rs.getString("mode_paiement"),
                        rs.getString("statut")
                );
                rows.add(new FactureRow(facture, commande, rs.getString("client_email")));
            }
        }
        return rows;
    }

    // ═══════════════════════════════════════════════════
    // ══  TOUT LE CODE EXISTANT (inchangé) ═══════════
    // ═══════════════════════════════════════════════════

    private void triggerCatPeek() {
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), catPeek);
        slideUp.setToY(0);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), catPeek);
        slideDown.setToY(160);
        new SequentialTransition(slideUp, pause, slideDown).play();
    }

    private void showEnlargedImage(Image image) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Aperçu produit");
        ImageView big = new ImageView(image);
        big.setFitWidth(500); big.setFitHeight(500);
        big.setPreserveRatio(true); big.setSmooth(true);
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

    private void showSectionProduits() {
        panelProduits.toFront();
        clearNavActive();
        navProduits.getStyleClass().add("admin-nav-active");
        adminSectionTitle.setText("Validation des produits");
        adminSectionSubtitle.setText(
                "Approuvez ou refusez les annonces des vendeurs avant publication en boutique.");
        loadProduitsSilently();
        triggerCatPeek();
    }

    private void showSectionCommandes() {
        panelCommandes.toFront();
        clearNavActive();
        navCommandes.getStyleClass().add("admin-nav-active");
        adminSectionTitle.setText("Commandes clients");
        adminSectionSubtitle.setText(
                "Mettez à jour le statut des commandes : en attente, confirmée, livrée ou annulée.");
        loadCommandesSilently();
        triggerCatPeek();
    }

    private void clearNavActive() {
        navProduits.getStyleClass().remove("admin-nav-active");
        navCommandes.getStyleClass().remove("admin-nav-active");
        navFactures.getStyleClass().remove("admin-nav-active");
    }

    // ── Helpers statut ──────────────────────────────────

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

    // ── Produits ────────────────────────────────────────

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
        card.setUserData(p); card.setFillWidth(true);
        card.setOnMouseClicked(e -> selectProduitCard(card, p));

        ImageView thumb = buildProduitThumb(p);
        thumb.getStyleClass().add("admin-produit-thumb");

        Label idLbl  = new Label("#" + p.getId_produit());
        idLbl.getStyleClass().add("admin-card-id");
        Label nomLbl = new Label(p.getNom());
        nomLbl.getStyleClass().add("admin-card-title"); nomLbl.setWrapText(true);
        Label badge  = new Label(statutProduitAffiche(p.getStatut()));
        badge.getStyleClass().addAll("admin-badge", badgeStyleForProduitStatut(p.getStatut()));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox rowTop = new HBox(12); rowTop.setAlignment(Pos.CENTER_LEFT);
        rowTop.getChildren().addAll(thumb, idLbl, nomLbl, spacer, badge);

        Label meta = new Label(String.format(Locale.FRANCE, "%.2f DT", p.getPrix())
                + "  ·  Stock " + p.getStock()
                + "  ·  Vendeur #" + p.getUserId()
                + "  ·  " + p.getDate_ajout());
        meta.getStyleClass().add("admin-card-meta"); meta.setWrapText(true);

        String desc = p.getDescription();
        if (desc != null && desc.length() > 140) desc = desc.substring(0, 137) + "…";
        Label descLbl = new Label(desc == null || desc.isBlank() ? "—" : desc);
        descLbl.getStyleClass().add("admin-card-desc"); descLbl.setWrapText(true);

        card.getChildren().addAll(rowTop, meta, descLbl);
        VBox.setMargin(card, new Insets(0));
        return card;
    }

    private ImageView buildProduitThumb(Produit p) {
        ImageView iv = new ImageView();
        iv.setFitWidth(44); iv.setFitHeight(44);
        iv.setPreserveRatio(true); iv.setSmooth(true);
        Rectangle clip = new Rectangle(44, 44);
        clip.setArcWidth(14); clip.setArcHeight(14);
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
            } catch (Exception ignored) { iv.setImage(null); }
        }
        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseClicked(e -> {
            if (iv.getImage() != null) { e.consume(); showEnlargedImage(iv.getImage()); }
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
        selectedProduitCard = card; selectedProduit = p;
        card.getStyleClass().add("admin-card-selected");
        updateProduitButtons(p);
    }

    private void clearProduitSelectionVisual() {
        if (selectedProduitCard != null)
            selectedProduitCard.getStyleClass().remove("admin-card-selected");
        selectedProduitCard = null; selectedProduit = null;
    }

    private void selectProduitById(int id) {
        for (javafx.scene.Node n : produitsCardsBox.getChildren()) {
            if (n instanceof VBox box && box.getUserData() instanceof Produit pr
                    && pr.getId_produit() == id) {
                selectProduitCard(box, pr); return;
            }
        }
        clearProduitSelectionVisual(); updateProduitButtons(null);
    }

    // ── Commandes ───────────────────────────────────────

    private void rebuildCommandeCards(int keepCommandeId) {
        if (selectedCommandeCard != null)
            selectedCommandeCard.getStyleClass().remove("admin-card-selected");
        selectedCommandeCard = null; selectedCommande = null;
        commandesCardsBox.getChildren().clear();

        if (commandesCache.isEmpty()) {
            Label empty = new Label("Aucune commande en base.");
            empty.getStyleClass().add("admin-empty");
            commandesCardsBox.getChildren().add(empty);
            syncCommandeEditor(null); return;
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
        card.setUserData(c); card.setFillWidth(true);
        card.setOnMouseClicked(e -> selectCommandeCard(card, c));

        Label ref     = new Label("Commande #" + c.getId_commande());
        ref.getStyleClass().add("admin-card-id");
        Label montant = new Label(String.format(Locale.FRANCE, "%.2f DT", c.getMontant_total()));
        montant.getStyleClass().add("admin-card-montant");
        Label badge   = new Label(c.getStatut());
        badge.getStyleClass().addAll("admin-badge", badgeStyleForCommandeStatut(c.getStatut()));

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox row1 = new HBox(12); row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().addAll(ref, montant, sp, badge);

        Label line2 = new Label("Client #" + c.getUserId()
                + "  ·  " + c.getDate_commande()
                + "  ·  Paiement : " + c.getMode_paiement());
        line2.getStyleClass().add("admin-card-meta"); line2.setWrapText(true);

        card.getChildren().addAll(row1, line2);
        return card;
    }

    private void selectCommandeCard(VBox card, Commande c) {
        clearCommandeSelectionVisual();
        selectedCommandeCard = card; selectedCommande = c;
        card.getStyleClass().add("admin-card-selected");
        syncCommandeEditor(c);
    }

    private void clearCommandeSelectionVisual() {
        if (selectedCommandeCard != null)
            selectedCommandeCard.getStyleClass().remove("admin-card-selected");
        selectedCommandeCard = null; selectedCommande = null;
    }

    private void selectCommandeById(int commandeId) {
        for (javafx.scene.Node n : commandesCardsBox.getChildren()) {
            if (n instanceof VBox box && box.getUserData() instanceof Commande cmd
                    && cmd.getId_commande() == commandeId) {
                selectCommandeCard(box, cmd); return;
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

    // ── Load ────────────────────────────────────────────

    private void loadProduitsSilently() {
        try {
            produitsSource.setAll(adminProduitService.getAll());
            applyProduitFilter();
        } catch (SQLException ex) { alertErreur("Chargement produits", ex); }
    }

    private void loadCommandesSilently() {
        int keepId = selectedCommande != null ? selectedCommande.getId_commande() : -1;
        loadCommandesSilentlyPreserving(keepId);
    }

    private void loadCommandesSilentlyPreserving(int keepCommandeId) {
        try {
            commandesCache = adminCommandeService.getAll();
            rebuildCommandeCards(keepCommandeId);
        } catch (SQLException ex) { alertErreur("Chargement commandes", ex); }
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
        } catch (SQLException ex) { alertErreur("Approbation", ex); }
    }

    private void refuserSelection() {
        Produit p = selectedProduit;
        if (p == null || !"en_attente".equalsIgnoreCase(p.getStatut().trim())) return;
        try {
            adminProduitService.refuser(p.getId_produit());
            loadProduitsSilently();
        } catch (SQLException ex) { alertErreur("Refus", ex); }
    }

    private void appliquerStatutCommande() {
        Commande c = selectedCommande;
        String nouveau = commandeStatutCombo.getValue();
        if (c == null || nouveau == null || nouveau.isBlank()) return;
        if (nouveau.equals(c.getStatut())) return;
        try {
            Commande updated = new Commande(c.getId_commande(), c.getUserId(), c.getPanierId(),
                    c.getDate_commande(), c.getMontant_total(), c.getMode_paiement(), nouveau);
            int commandeId = c.getId_commande();
            adminCommandeService.update(updated);
            if (getEmailService() != null)
                getEmailService().sendStatutEmail(c.getUserId(), c);
            loadCommandesSilentlyPreserving(commandeId);
        } catch (SQLException ex) { alertErreur("Mise à jour commande", ex); }
    }

    // ── Utils ────────────────────────────────────────────

    private static String fmt(int id) {
        return String.format("%05d", id);
    }

    private static String safe(String v) {
        return v == null ? "—" : v;
    }

    private static void alertErreur(String titre, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(ex.getMessage());
        a.setContentText(ex.toString()); a.showAndWait();
    }
}