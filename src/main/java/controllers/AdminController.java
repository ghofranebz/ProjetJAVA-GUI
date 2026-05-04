package controllers;

import entities.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String COL_BG   = "#0e3960";
    private static final String ACCENT   = "#bd936b";
    private static final String LIGHT_BG = "#dad8cd";
    private static final String GREEN    = "#2e7d32";
    private static final String ORANGE   = "#e65100";
    private static final String RED      = "#c62828";
    private static final String PURPLE   = "#6a1a6a";

    private final Stage       stage;
    private final User        adminUser;
    private final UserService userService;

    private BorderPane root;
    private StackPane  contentPane;

    private Button btnDashboard, btnTous, btnPending, btnApproved, btnRejected;

    public AdminController(Stage stage, User adminUser) {
        this.stage       = stage;
        this.adminUser   = adminUser;
        this.userService = new UserService();
        buildUI();
    }

    // ─── Root layout ──────────────────────────────────────────────────────────

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + LIGHT_BG + ";");
        root.setLeft(buildSidebar());
        contentPane = new StackPane();
        root.setCenter(contentPane);
        showDashboard();
    }

    // ─── Sidebar ──────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(230);
        sidebar.setStyle("-fx-background-color: " + COL_BG + ";");

        VBox logoBox = new VBox(4);
        logoBox.setPadding(new Insets(28, 20, 24, 20));
        Label logo = new Label("🐾 Purrly");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web(ACCENT));
        Label roleLabel = new Label("Panneau Admin");
        roleLabel.setFont(Font.font("Segoe UI", 11));
        roleLabel.setTextFill(Color.web(LIGHT_BG));
        roleLabel.setOpacity(0.7);
        logoBox.getChildren().addAll(logo, roleLabel);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1a4f7a;");

        Label sectionLabel = new Label("NAVIGATION");
        sectionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        sectionLabel.setTextFill(Color.web(LIGHT_BG));
        sectionLabel.setOpacity(0.5);
        sectionLabel.setPadding(new Insets(16, 16, 4, 16));

        btnDashboard = makeSideBtn("📊  Tableau de bord");
        btnTous      = makeSideBtn("👥  Tous les users");
        btnPending   = makeSideBtn("⏳  En attente");
        btnApproved  = makeSideBtn("✅  Approuvés");
        btnRejected  = makeSideBtn("❌  Rejetés");

        btnDashboard.setOnAction(e -> { setActiveBtn(btnDashboard); showDashboard(); });
        btnTous     .setOnAction(e -> { setActiveBtn(btnTous);      showAllUsers(); });
        btnPending  .setOnAction(e -> { setActiveBtn(btnPending);   showFiltered("PENDING",  "En attente"); });
        btnApproved .setOnAction(e -> { setActiveBtn(btnApproved);  showFiltered("APPROVED", "Approuvés"); });
        btnRejected .setOnAction(e -> { setActiveBtn(btnRejected);  showFiltered("REJECTED", "Rejetés"); });

        setActiveBtn(btnDashboard);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #1a4f7a;");

        Label adminName = new Label("👤 " + adminUser.getFullName());
        adminName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        adminName.setTextFill(Color.web(LIGHT_BG));
        adminName.setPadding(new Insets(0, 16, 4, 16));

        Button btnLogout = makeSideBtn("🚪  Déconnexion");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        btnLogout.setFont(Font.font("Segoe UI", 13));
        btnLogout.setOnAction(e -> {
            LoginController lc = new LoginController(stage);
            stage.getScene().setRoot(lc.getView());
        });

        VBox menu   = new VBox(3, btnDashboard, btnTous, btnPending, btnApproved, btnRejected);
        menu.setPadding(new Insets(4, 8, 0, 8));
        VBox bottom = new VBox(4, sep2, adminName, btnLogout);
        bottom.setPadding(new Insets(0, 8, 20, 8));

        sidebar.getChildren().addAll(logoBox, sep, sectionLabel, menu, spacer, bottom);
        return sidebar;
    }

    private void setActiveBtn(Button active) {
        for (Button b : new Button[]{btnDashboard, btnTous, btnPending, btnApproved, btnRejected}) {
            if (b == null) continue;
            if (b == active) {
                b.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
                b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #dad8cd; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
                b.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            }
        }
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    private void showDashboard() {
        try {
            int total    = userService.countTotal();
            int approved = userService.countByStatus("APPROVED");
            int pending  = userService.countByStatus("PENDING_ADMIN")
                    + userService.countByStatus("PENDING_EMAIL");
            int rejected = userService.countByStatus("REJECTED");
            int suspended= userService.countByStatus("SUSPENDED");

            int clients = userService.countByRole("CLIENT");
            int vets    = userService.countByRole("VETERINAIRE");
            int sitters = userService.countByRole("PETSITTER");
            int salons  = userService.countByRole("SALON_TOILETTAGE");
            int admins  = userService.countByRole("ADMIN");

            ScrollPane sp = new ScrollPane();
            sp.setFitToWidth(true);
            sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            VBox page = new VBox(28);
            page.setPadding(new Insets(32));

            // Header
            VBox headerBox = new VBox(4);
            Label title = new Label("Tableau de bord");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
            title.setTextFill(Color.web(COL_BG));
            Label sub = new Label("Bienvenue, " + adminUser.getFirstName() + " · " + fmt(LocalDateTime.now()));
            sub.setFont(Font.font("Segoe UI", 13));
            sub.setTextFill(Color.GRAY);
            headerBox.getChildren().addAll(title, sub);

            // Status cards
            Label statLabel = sectionTitle("STATISTIQUES PAR STATUT");
            HBox statusCards = new HBox(16);
            statusCards.getChildren().addAll(
                    statCard("Total inscrits",  total,     COL_BG,  "👥"),
                    statCard("Approuvés",       approved,  GREEN,   "✅"),
                    statCard("En attente",      pending,   ORANGE,  "⏳"),
                    statCard("Rejetés",         rejected,  RED,     "❌"),
                    statCard("Suspendus",       suspended, PURPLE,  "🚫")
            );
            for (javafx.scene.Node n : statusCards.getChildren())
                HBox.setHgrow(n, Priority.ALWAYS);

            // Role cards
            Label roleLabel2 = sectionTitle("RÉPARTITION PAR RÔLE");
            HBox roleCards = new HBox(16);
            roleCards.getChildren().addAll(
                    statCard("Clients",      clients, "#1565c0", "🐶"),
                    statCard("Vétérinaires", vets,    "#4a148c", "🩺"),
                    statCard("Petsitters",   sitters, "#00695c", "🏠"),
                    statCard("Salons",       salons,  "#e65100", "✂️"),
                    statCard("Admins",       admins,  "#37474f", "🔑")
            );
            for (javafx.scene.Node n : roleCards.getChildren())
                HBox.setHgrow(n, Priority.ALWAYS);

            // Pending table
            Label pendingTitle = sectionTitle("COMPTES EN ATTENTE DE VALIDATION");
            ObservableList<User> pendingItems = FXCollections.observableArrayList(
                    userService.getAll().stream().filter(u ->
                            u.getStatus() == User.Status.PENDING_EMAIL ||
                                    u.getStatus() == User.Status.PENDING_ADMIN
                    ).toList()
            );
            Label pendingCountLbl = new Label(pendingItems.size() + " utilisateur(s) en attente");
            pendingCountLbl.setFont(Font.font("Segoe UI", 12));
            pendingCountLbl.setTextFill(Color.GRAY);

            TableView<User> pendingTable = buildFullTable();
            pendingTable.setItems(pendingItems);
            pendingTable.setPrefHeight(280);
            wireActionColumns(pendingTable, pendingItems, pendingCountLbl);

            Button approveAll = makeBtn("✅  Approuver tous les comptes en attente", ACCENT);
            approveAll.setPrefHeight(40);
            approveAll.setOnAction(e -> bulkApprove());

            page.getChildren().addAll(
                    headerBox,
                    statLabel,    statusCards,
                    roleLabel2,   roleCards,
                    pendingTitle, pendingCountLbl, pendingTable, approveAll
            );

            sp.setContent(page);
            contentPane.getChildren().setAll(sp);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Impossible de charger les statistiques.");
        }
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        l.setTextFill(Color.GRAY);
        return l;
    }

    private VBox statCard(String label, int value, String color, String icon) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setFont(Font.font(22));
        Label val = new Label(String.valueOf(value));
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        val.setTextFill(Color.web(color));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.GRAY);
        card.getChildren().addAll(ico, val, lbl);
        return card;
    }

    // ─── All Users ────────────────────────────────────────────────────────────

    private void showAllUsers() {
        try {
            ObservableList<User> list = FXCollections.observableArrayList(userService.getAll());
            contentPane.getChildren().setAll(buildTableView("Tous les utilisateurs", list, true));
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Impossible de charger les utilisateurs.");
        }
    }

    // ─── Filtered views ───────────────────────────────────────────────────────

    private void showFiltered(String filterKey, String sectionTitle) {
        try {
            ObservableList<User> all = FXCollections.observableArrayList(userService.getAll());
            ObservableList<User> filtered;
            if ("PENDING".equals(filterKey)) {
                filtered = all.filtered(u ->
                        u.getStatus() == User.Status.PENDING_EMAIL ||
                                u.getStatus() == User.Status.PENDING_ADMIN);
            } else {
                filtered = all.filtered(u -> u.getStatus().name().equals(filterKey));
            }
            contentPane.getChildren().setAll(
                    buildTableView(sectionTitle, filtered, "PENDING".equals(filterKey)));
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Impossible de charger la liste filtrée.");
        }
    }

    // ─── Table page builder ───────────────────────────────────────────────────

    private VBox buildTableView(String sectionTitle, ObservableList<User> items, boolean showBulkApprove) {
        ObservableList<User> masterCache = FXCollections.observableArrayList(items);

        VBox page = new VBox(16);
        page.setPadding(new Insets(30));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label(sectionTitle);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(COL_BG));
        Label countLbl = new Label(items.size() + " utilisateur(s)");
        countLbl.setFont(Font.font("Segoe UI", 13));
        countLbl.setTextFill(Color.GRAY);
        titleBox.getChildren().addAll(title, countLbl);

        Button addBtn = makeBtn("+ Ajouter un utilisateur", COL_BG);
        addBtn.setPrefHeight(38);
        addBtn.setOnAction(e -> openUserForm(null, items, countLbl));
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, addBtn);

        HBox toolbar = buildToolbar(masterCache, items, countLbl);

        TableView<User> table = buildFullTable();
        table.setItems(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        wireActionColumns(table, items, countLbl);

        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(14));
        bottom.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        if (showBulkApprove) {
            Button approveAllBtn = makeBtn("✅  Approuver tous les comptes en attente", ACCENT);
            approveAllBtn.setPrefHeight(38);
            approveAllBtn.setOnAction(e -> bulkApprove());
            bottom.getChildren().add(approveAllBtn);
        }

        page.getChildren().addAll(header, toolbar, table, bottom);
        return page;
    }

    // ─── Toolbar ──────────────────────────────────────────────────────────────

    private HBox buildToolbar(ObservableList<User> masterCache,
                              ObservableList<User> display,
                              Label countLbl) {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(16));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TextField search = new TextField();
        search.setPromptText("🔍  Rechercher par nom ou email...");
        search.setPrefWidth(260);
        search.setPrefHeight(38);
        search.setStyle("-fx-background-color: #f5f4f1; -fx-border-color: #dad8cd; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-font-size: 13px;");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Tous les rôles","CLIENT","VETERINAIRE","PETSITTER","SALON_TOILETTAGE","ADMIN");
        roleBox.setValue("Tous les rôles");
        roleBox.setPrefHeight(38);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Tous les statuts","APPROVED","PENDING_ADMIN","PENDING_EMAIL","REJECTED","SUSPENDED");
        statusBox.setValue("Tous les statuts");
        statusBox.setPrefHeight(38);

        Button refresh = makeBtn("🔄  Actualiser", COL_BG);
        refresh.setPrefHeight(38);
        refresh.setOnAction(e -> {
            search.clear();
            roleBox.setValue("Tous les rôles");
            statusBox.setValue("Tous les statuts");
            display.setAll(masterCache);
            countLbl.setText(masterCache.size() + " utilisateur(s)");
        });

        toolbar.getChildren().addAll(search, roleBox, statusBox, refresh);

        Runnable filter = () -> applyFilter(masterCache, display, search, roleBox, statusBox, countLbl);
        search.textProperty().addListener((o, a, b) -> filter.run());
        roleBox.setOnAction(e -> filter.run());
        statusBox.setOnAction(e -> filter.run());

        return toolbar;
    }

    private void applyFilter(ObservableList<User> source,
                             ObservableList<User> display,
                             TextField search,
                             ComboBox<String> roleBox,
                             ComboBox<String> statusBox,
                             Label countLbl) {
        String s  = search.getText().toLowerCase();
        String r  = roleBox.getValue();
        String st = statusBox.getValue();
        java.util.List<User> filtered = source.stream().filter(u -> {
            boolean ms = s.isEmpty()
                    || u.getFullName().toLowerCase().contains(s)
                    || u.getEmail().toLowerCase().contains(s);
            boolean mr = "Tous les rôles".equals(r)    || u.getRole().name().equals(r);
            boolean mt = "Tous les statuts".equals(st)  || u.getStatus().name().equals(st);
            return ms && mr && mt;
        }).toList();
        display.setAll(filtered);
        countLbl.setText(filtered.size() + " utilisateur(s)");
    }

    // ─── Full table — FIX : SimpleStringProperty au lieu de PropertyValueFactory ──

    private TableView<User> buildFullTable() {
        TableView<User> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Aucun utilisateur trouvé."));
        tv.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e8e6e0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        // ── ID ──────────────────────────────────────────────────────────────
        TableColumn<User, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getId())));
        colId.setMaxWidth(55); colId.setMinWidth(55);
        colId.setStyle("-fx-alignment: CENTER;");

        // ── Prénom ───────────────────────────────────────────────────────────
        TableColumn<User, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getFirstName()));

        // ── Nom ──────────────────────────────────────────────────────────────
        TableColumn<User, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getLastName()));

        // ── Email ─────────────────────────────────────────────────────────────
        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getEmail()));
        colEmail.setMinWidth(170);

        // ── Téléphone ─────────────────────────────────────────────────────────
        TableColumn<User, String> colPhone = new TableColumn<>("Tél.");
        colPhone.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getPhone() != null ? cd.getValue().getPhone() : "—"));

        // ── Ville ─────────────────────────────────────────────────────────────
        TableColumn<User, String> colVille = new TableColumn<>("Ville");
        colVille.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getCity() != null ? cd.getValue().getCity() : "—"));

        // ── Adresse ──────────────────────────────────────────────────────────
        TableColumn<User, String> colAddr = new TableColumn<>("Adresse");
        colAddr.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getAddress() != null ? cd.getValue().getAddress() : "—"));

        // ── Code postal ───────────────────────────────────────────────────────
        TableColumn<User, String> colCP = new TableColumn<>("CP");
        colCP.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getPostalCode() != null ? cd.getValue().getPostalCode() : "—"));
        colCP.setMaxWidth(80);

        // ── Rôle — ComboBox inline ────────────────────────────────────────────
        TableColumn<User, Void> colRole = new TableColumn<>("Rôle");
        colRole.setMinWidth(140);
        colRole.setCellFactory(col2 -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>();
            {
                combo.getItems().addAll("CLIENT","VETERINAIRE","PETSITTER","SALON_TOILETTAGE","ADMIN");
                combo.setStyle("-fx-font-size: 11px; -fx-background-radius: 6;");
                combo.setMaxWidth(Double.MAX_VALUE);
                combo.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u != null && combo.getValue() != null
                            && !combo.getValue().equals(u.getRole().name())) {
                        u.setRole(combo.getValue());
                        try { userService.update(u); }
                        catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                if (u != null) combo.setValue(u.getRole().name());
                setGraphic(combo);
            }
        });

        // ── Statut — badge coloré ─────────────────────────────────────────────
        TableColumn<User, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getStatus() != null ? cd.getValue().getStatus().name() : ""));
        colStatus.setMinWidth(125);
        colStatus.setCellFactory(col2 -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) { setText(null); setGraphic(null); return; }
                String labelTxt = switch (item) {
                    case "APPROVED"      -> "✅ Approuvé";
                    case "PENDING_ADMIN" -> "⏳ En attente";
                    case "PENDING_EMAIL" -> "📧 Email";
                    case "REJECTED"      -> "❌ Rejeté";
                    case "SUSPENDED"     -> "🚫 Suspendu";
                    case "BANNED"        -> "⛔ Banni";
                    default              -> item;
                };
                String bg = switch (item) {
                    case "APPROVED"                       -> GREEN;
                    case "PENDING_ADMIN","PENDING_EMAIL"  -> ORANGE;
                    case "REJECTED"                       -> RED;
                    case "SUSPENDED"                      -> PURPLE;
                    case "BANNED"                         -> "#212121";
                    default                               -> "#607d8b";
                };
                Label badge = new Label(labelTxt);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                badge.setTextFill(Color.WHITE);
                badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;");
                setGraphic(badge); setText(null);
            }
        });

        // ── Date inscription ──────────────────────────────────────────────────
        TableColumn<User, String> colCreated = new TableColumn<>("Inscrit le");
        colCreated.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getCreatedAt() != null ? fmt(cd.getValue().getCreatedAt()) : "—"));
        colCreated.setMinWidth(115);
        colCreated.setCellFactory(col2 -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setFont(Font.font("Segoe UI", 11));
                setTextFill(Color.GRAY);
            }
        });

        // ── Dernière connexion ────────────────────────────────────────────────
        TableColumn<User, String> colLastLogin = new TableColumn<>("Dernière connexion");
        colLastLogin.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getLastLoginAt() != null ? fmt(cd.getValue().getLastLoginAt()) : "Jamais"));
        colLastLogin.setMinWidth(130);
        colLastLogin.setCellFactory(col2 -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setFont(Font.font("Segoe UI", 11));
                setTextFill(Color.GRAY);
            }
        });

        // ── Actions (câblées par wireActionColumns) ───────────────────────────
        TableColumn<User, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(250);

        tv.getColumns().addAll(
                colId, colPrenom, colNom, colEmail, colPhone,
                colVille, colAddr, colCP,
                colRole, colStatus,
                colCreated, colLastLogin,
                colActions
        );

        return tv;
    }

    // ─── Wire action columns ──────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void wireActionColumns(TableView<User> tv,
                                   ObservableList<User> list,
                                   Label countLbl) {
        TableColumn<User, Void> colActions =
                (TableColumn<User, Void>) tv.getColumns().get(tv.getColumns().size() - 1);

        colActions.setCellFactory(col -> new TableCell<>() {
            final Button appBtn  = makeSmallBtn("✅ Approuver", GREEN);
            final Button rejBtn  = makeSmallBtn("❌ Rejeter",   RED);
            final Button editBtn = makeSmallBtn("✏️ Modifier",  "#1565c0");
            final Button delBtn  = makeSmallBtn("🗑 Supprimer", "#757575");
            final HBox box = new HBox(4, appBtn, rejBtn, editBtn, delBtn);
            {
                box.setAlignment(Pos.CENTER);

                appBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == null) return;
                    try {
                        userService.approveUser(u.getId());
                        u.setStatus(User.Status.APPROVED);
                        refreshObservableList(list);
                        countLbl.setText(list.size() + " utilisateur(s)");
                        toast("✅ " + u.getFullName() + " approuvé(e).");
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });

                rejBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == null) return;
                    try {
                        userService.rejectUser(u.getId());
                        u.setStatus(User.Status.REJECTED);
                        refreshObservableList(list);
                        countLbl.setText(list.size() + " utilisateur(s)");
                        toast("❌ " + u.getFullName() + " rejeté(e).");
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });

                editBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u != null) openUserForm(u, list, countLbl);
                });

                delBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u == null) return;
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Supprimer");
                    confirm.setHeaderText("Supprimer " + u.getFullName() + " ?");
                    confirm.setContentText("Cette action est irréversible.");
                    confirm.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK) {
                            try {
                                userService.delete(u.getId());
                                list.remove(u);
                                countLbl.setText(list.size() + " utilisateur(s)");
                            } catch (SQLException ex) { ex.printStackTrace(); }
                        }
                    });
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ─── User form (Add / Edit) ───────────────────────────────────────────────

    private void openUserForm(User existing, ObservableList<User> list, Label countLbl) {
        boolean isEdit = existing != null;

        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.setTitle(isEdit ? "Modifier l'utilisateur" : "Ajouter un utilisateur");
        dialog.setResizable(false);

        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setStyle("-fx-background-color: white;");
        form.setPrefWidth(480);

        Label formTitle = new Label(isEdit ? "✏️ Modifier l'utilisateur" : "➕ Ajouter un utilisateur");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web(COL_BG));

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.getColumnConstraints().addAll(colConstraint(130), colConstraint(Double.MAX_VALUE));

        TextField tfFirst = field("Prénom",      isEdit ? existing.getFirstName() : "");
        TextField tfLast  = field("Nom",         isEdit ? existing.getLastName()  : "");
        TextField tfEmail = field("Email",       isEdit ? existing.getEmail()     : "");
        PasswordField tfPass = new PasswordField();
        tfPass.setPromptText(isEdit ? "Laisser vide pour ne pas changer" : "Mot de passe");
        tfPass.setStyle(inputStyle());
        TextField tfPhone = field("Téléphone",   isEdit && existing.getPhone()      != null ? existing.getPhone()      : "");
        TextField tfCity  = field("Ville",       isEdit && existing.getCity()       != null ? existing.getCity()       : "");
        TextField tfAddr  = field("Adresse",     isEdit && existing.getAddress()    != null ? existing.getAddress()    : "");
        TextField tfCP    = field("Code postal", isEdit && existing.getPostalCode() != null ? existing.getPostalCode() : "");

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("CLIENT","VETERINAIRE","PETSITTER","SALON_TOILETTAGE","ADMIN");
        cbRole.setValue(isEdit ? existing.getRole().name() : "CLIENT");
        cbRole.setMaxWidth(Double.MAX_VALUE); cbRole.setStyle(inputStyle());

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("PENDING_EMAIL","PENDING_ADMIN","APPROVED","REJECTED","SUSPENDED","BANNED");
        cbStatus.setValue(isEdit ? existing.getStatus().name() : "PENDING_EMAIL");
        cbStatus.setMaxWidth(Double.MAX_VALUE); cbStatus.setStyle(inputStyle());

        int row = 0;
        addRow(grid, row++, "Prénom *",     tfFirst);
        addRow(grid, row++, "Nom *",        tfLast);
        addRow(grid, row++, "Email *",      tfEmail);
        addRow(grid, row++, "Mot de passe", tfPass);
        addRow(grid, row++, "Téléphone",    tfPhone);
        addRow(grid, row++, "Ville",        tfCity);
        addRow(grid, row++, "Adresse",      tfAddr);
        addRow(grid, row++, "Code postal",  tfCP);
        addRow(grid, row++, "Rôle",         cbRole);
        addRow(grid, row,   "Statut",       cbStatus);

        Label errLbl = new Label("");
        errLbl.setTextFill(Color.RED);
        errLbl.setFont(Font.font("Segoe UI", 12));

        Button saveBtn   = makeBtn(isEdit ? "💾 Enregistrer" : "➕ Ajouter", ACCENT);
        Button cancelBtn = makeBtn("Annuler", "#607d8b");
        saveBtn.setPrefHeight(40); cancelBtn.setPrefHeight(40);
        HBox btns = new HBox(10, saveBtn, cancelBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        saveBtn.setOnAction(e -> {
            String fn = tfFirst.getText().trim();
            String ln = tfLast.getText().trim();
            String em = tfEmail.getText().trim();
            String pw = tfPass.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || em.isEmpty()) {
                errLbl.setText("Prénom, nom et email sont obligatoires."); return;
            }
            if (!em.contains("@") || !em.contains(".")) {
                errLbl.setText("Format d'email invalide."); return;
            }
            if (!isEdit && pw.isEmpty()) {
                errLbl.setText("Le mot de passe est obligatoire pour un nouvel utilisateur."); return;
            }
            if (!pw.isEmpty() && pw.length() < 6) {
                errLbl.setText("Le mot de passe doit contenir au moins 6 caractères."); return;
            }

            try {
                if (isEdit) {
                    existing.setFirstName(fn);
                    existing.setLastName(ln);
                    existing.setEmail(em);
                    if (!pw.isEmpty()) existing.setPassword(pw);
                    existing.setPhone(tfPhone.getText().trim());
                    existing.setCity(tfCity.getText().trim());
                    existing.setAddress(tfAddr.getText().trim());
                    existing.setPostalCode(tfCP.getText().trim());
                    existing.setRole(cbRole.getValue());
                    existing.setStatus(cbStatus.getValue());
                    existing.setUpdatedAt(LocalDateTime.now());
                    userService.update(existing);
                    refreshObservableList(list);
                    toast("✏️ Utilisateur modifié avec succès.");
                } else {
                    User nu = new User(fn, ln, em, pw, tfPhone.getText().trim(), cbRole.getValue());
                    nu.setCity(tfCity.getText().trim());
                    nu.setAddress(tfAddr.getText().trim());
                    nu.setPostalCode(tfCP.getText().trim());
                    nu.setStatus(cbStatus.getValue());
                    userService.add(nu);
                    list.add(nu);
                    toast("➕ Utilisateur ajouté avec succès.");
                }
                countLbl.setText(list.size() + " utilisateur(s)");
                dialog.close();
            } catch (SQLException ex) {
                String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                errLbl.setText(msg.contains("duplicate") || msg.contains("unique")
                        ? "Cet email est déjà utilisé."
                        : "Erreur technique pendant l'enregistrement.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        form.getChildren().addAll(formTitle, new Separator(), grid, errLbl, btns);
        javafx.scene.Scene sc = new javafx.scene.Scene(form);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ─── Bulk approve ─────────────────────────────────────────────────────────

    private void bulkApprove() {
        try {
            int count = 0;
            for (User u : userService.getUsersByStatus("PENDING_ADMIN")) {
                userService.approveUser(u.getId()); count++;
            }
            for (User u : userService.getUsersByStatus("PENDING_EMAIL")) {
                userService.approveUser(u.getId()); count++;
            }
            toast(count + " compte(s) approuvé(s) avec succès !");
            setActiveBtn(btnDashboard);
            showDashboard();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void refreshObservableList(ObservableList<User> list) {
        try { list.setAll(userService.getAll()); }
        catch (SQLException ex) { ex.printStackTrace(); }
    }

    private String fmt(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(FMT);
    }

    private void toast(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Purrly"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private ColumnConstraints colConstraint(double pref) {
        ColumnConstraints cc = new ColumnConstraints();
        if (pref == Double.MAX_VALUE) cc.setHgrow(Priority.ALWAYS);
        else cc.setPrefWidth(pref);
        return cc;
    }

    private TextField field(String prompt, String val) {
        TextField tf = new TextField(val);
        tf.setPromptText(prompt);
        tf.setStyle(inputStyle());
        return tf;
    }

    private String inputStyle() {
        return "-fx-background-color: #f5f4f1; -fx-border-color: #dad8cd; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-font-size: 13px;";
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node node) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#444"));
        GridPane.setValignment(lbl, VPos.CENTER);
        grid.add(lbl, 0, row);
        grid.add(node, 1, row);
        GridPane.setHgrow(node, Priority.ALWAYS);
    }

    private Button makeBtn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        return b;
    }

    private Button makeSmallBtn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white; " +
                "-fx-font-size: 10px; -fx-background-radius: 6; -fx-padding: 5 8; -fx-cursor: hand;");
        return b;
    }

    private Button makeSideBtn(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(11, 16, 11, 16));
        b.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: #dad8cd; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        return b;
    }

    public BorderPane getView() { return root; }
}
