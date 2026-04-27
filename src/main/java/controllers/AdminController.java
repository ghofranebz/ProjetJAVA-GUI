package controllers;

import entities.User;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;

public class AdminController {

    private Stage stage;
    private User adminUser;
    private BorderPane root;
    private TableView<User> table;
    private UserService userService;
    private Label totalLbl;
    private Button btnTous, btnPending, btnApproved, btnRejected;

    public AdminController(Stage stage, User adminUser) {
        this.stage       = stage;
        this.adminUser   = adminUser;
        this.userService = new UserService();
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #dad8cd;");
        root.setLeft(buildSidebar());
        root.setCenter(buildMainArea());
        refreshTable();
    }

    private VBox buildMainArea() {
        VBox mainArea = new VBox(16);
        mainArea.setPadding(new Insets(30));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label pageTitle = new Label("Gestion des Utilisateurs");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        pageTitle.setTextFill(Color.web("#0e3960"));
        totalLbl = new Label("Chargement...");
        totalLbl.setFont(Font.font("Segoe UI", 13));
        totalLbl.setTextFill(Color.GRAY);
        titleBox.getChildren().addAll(pageTitle, totalLbl);

        Label adminInfo = new Label("Admin : " + adminUser.getFullName());
        adminInfo.setFont(Font.font("Segoe UI", 12));
        adminInfo.setTextFill(Color.GRAY);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, adminInfo);

        HBox toolbar = buildToolbar();
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox bottomBar = buildBottomBar();

        mainArea.getChildren().addAll(header, toolbar, table, bottomBar);
        VBox.setVgrow(table, Priority.ALWAYS);
        return mainArea;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #0e3960;");

        VBox logoBox = new VBox(5);
        logoBox.setPadding(new Insets(28, 20, 28, 20));
        Label logo = new Label("🐾 Purrly");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web("#bd936b"));
        Label roleLabel = new Label("Panneau Admin");
        roleLabel.setFont(Font.font("Segoe UI", 11));
        roleLabel.setTextFill(Color.web("#dad8cd"));
        roleLabel.setOpacity(0.7);
        logoBox.getChildren().addAll(logo, roleLabel);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1a4f7a;");

        btnTous     = makeSideBtn("👥  Tous les users", true);
        btnPending  = makeSideBtn("⏳  En attente",      false);
        btnApproved = makeSideBtn("✅  Approuvés",       false);
        btnRejected = makeSideBtn("❌  Rejetés",         false);

        btnTous.setOnAction(e -> {
            setActiveSideBtn(btnTous);
            refreshTable();
        });

        btnPending.setOnAction(e -> {
            setActiveSideBtn(btnPending);
            try {
                ObservableList<User> all = FXCollections.observableArrayList(userService.getAll());
                ObservableList<User> pending = all.filtered(u ->
                        u.getStatus() == User.Status.PENDING_EMAIL ||
                                u.getStatus() == User.Status.PENDING_ADMIN
                );
                table.setItems(pending);
                totalLbl.setText(pending.size() + " utilisateur(s) en attente");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        btnApproved.setOnAction(e -> {
            setActiveSideBtn(btnApproved);
            try {
                ObservableList<User> list = FXCollections.observableArrayList(
                        userService.getUsersByStatus("APPROVED")
                );
                table.setItems(list);
                totalLbl.setText(list.size() + " utilisateur(s) approuvé(s)");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        btnRejected.setOnAction(e -> {
            setActiveSideBtn(btnRejected);
            try {
                ObservableList<User> list = FXCollections.observableArrayList(
                        userService.getUsersByStatus("REJECTED")
                );
                table.setItems(list);
                totalLbl.setText(list.size() + " utilisateur(s) rejeté(s)");
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = new Button("🚪  Déconnexion");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setAlignment(Pos.CENTER_LEFT);
        btnLogout.setPadding(new Insets(12, 16, 12, 16));
        btnLogout.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #ff6b6b;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-font-size: 13px;
            """);
        btnLogout.setOnAction(e -> {
            LoginController login = new LoginController(stage);
            stage.getScene().setRoot(login.getView());
        });

        VBox menuItems = new VBox(4, btnTous, btnPending, btnApproved, btnRejected);
        menuItems.setPadding(new Insets(12, 8, 0, 8));
        VBox bottomMenu = new VBox(btnLogout);
        bottomMenu.setPadding(new Insets(0, 8, 20, 8));

        sidebar.getChildren().addAll(logoBox, sep, menuItems, spacer, bottomMenu);
        return sidebar;
    }

    private void setActiveSideBtn(Button active) {
        for (Button b : new Button[]{btnTous, btnPending, btnApproved, btnRejected}) {
            if (b == active) {
                b.setStyle("-fx-background-color: #bd936b; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
                b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            } else {
                b.setStyle("-fx-background-color: transparent; -fx-text-fill: #dad8cd; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
                b.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            }
        }
    }

    private HBox buildToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(16));
        toolbar.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            """);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Rechercher par nom ou email...");
        searchField.setPrefWidth(260);
        searchField.setPrefHeight(38);
        searchField.setStyle("""
            -fx-background-color: #f5f4f1;
            -fx-border-color: #dad8cd;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-font-size: 13px;
            """);

        ComboBox<String> filterRole = new ComboBox<>();
        filterRole.getItems().addAll(
                "Tous les rôles","CLIENT","VETERINAIRE",
                "PETSITTER","SALON_TOILETTAGE","ADMIN"
        );
        filterRole.setValue("Tous les rôles");
        filterRole.setPrefHeight(38);

        ComboBox<String> filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll(
                "Tous les statuts","APPROVED",
                "PENDING_ADMIN","PENDING_EMAIL",
                "REJECTED","SUSPENDED"
        );
        filterStatus.setValue("Tous les statuts");
        filterStatus.setPrefHeight(38);

        Button refreshBtn = makeActionBtn("🔄  Actualiser", "#0e3960");
        refreshBtn.setPrefHeight(38);
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            filterRole.setValue("Tous les rôles");
            filterStatus.setValue("Tous les statuts");
            setActiveSideBtn(btnTous);
            refreshTable();
        });

        toolbar.getChildren().addAll(searchField, filterRole, filterStatus, refreshBtn);

        searchField.textProperty().addListener((obs, o, n) ->
                applyFilter(n, filterRole.getValue(), filterStatus.getValue()));
        filterRole.setOnAction(e ->
                applyFilter(searchField.getText(), filterRole.getValue(), filterStatus.getValue()));
        filterStatus.setOnAction(e ->
                applyFilter(searchField.getText(), filterRole.getValue(), filterStatus.getValue()));

        return toolbar;
    }

    private TableView<User> buildTable() {
        TableView<User> tv = new TableView<>();
        tv.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            """);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("Aucun utilisateur trouvé."));

        TableColumn<User, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setMaxWidth(50); colId.setMinWidth(50);

        TableColumn<User, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<User, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> colPhone = new TableColumn<>("Téléphone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Colonne Rôle avec ComboBox pour modifier directement
        TableColumn<User, Void> colRole = new TableColumn<>("Rôle");
        colRole.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>();
            {
                combo.getItems().addAll(
                        "CLIENT","VETERINAIRE","PETSITTER","SALON_TOILETTAGE","ADMIN"
                );
                combo.setStyle("-fx-font-size: 11px;");
                combo.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u != null && combo.getValue() != null
                            && !combo.getValue().equals(u.getRole().name())) {
                        u.setRole(combo.getValue());
                        try {
                            userService.update(u);
                            showAlert(Alert.AlertType.INFORMATION,
                                    "Rôle de " + u.getFullName() +
                                            " changé en " + combo.getValue());
                            refreshTable();
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableView().getItems().get(getIndex());
                if (u != null) combo.setValue(u.getRole().name());
                setGraphic(combo);
            }
        });

        // Colonne Statut colorée
        TableColumn<User, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "APPROVED"       -> setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    case "PENDING_ADMIN",
                         "PENDING_EMAIL" -> setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold;");
                    case "REJECTED"      -> setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    case "SUSPENDED"     -> setStyle("-fx-text-fill: #6a1a6a; -fx-font-weight: bold;");
                    default              -> setStyle("");
                }
            }
        });

        // Colonne Actions
        TableColumn<User, Void> colActions = new TableColumn<>("Actions");
        colActions.setMinWidth(220);
        colActions.setCellFactory(col -> new TableCell<>() {
            Button approveBtn = makeActionBtn("✅ Approuver", "#2e7d32");
            Button rejectBtn  = makeActionBtn("❌ Rejeter",   "#c62828");
            Button deleteBtn  = makeActionBtn("🗑 Supprimer", "#757575");
            HBox box = new HBox(6, approveBtn, rejectBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER);
                approveBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    try {
                        userService.approveUser(u.getId());
                        showAlert(Alert.AlertType.INFORMATION,
                                "✅ " + u.getFullName() + " a été approuvé.");
                        refreshTable();
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });
                rejectBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    try {
                        userService.rejectUser(u.getId());
                        showAlert(Alert.AlertType.INFORMATION,
                                "❌ " + u.getFullName() + " a été rejeté.");
                        refreshTable();
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });
                deleteBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmer");
                    confirm.setHeaderText("Supprimer " + u.getFullName() + " ?");
                    confirm.setContentText("Cette action est irréversible.");
                    confirm.showAndWait().ifPresent(resp -> {
                        if (resp == ButtonType.OK) {
                            try {
                                userService.delete(u.getId());
                                refreshTable();
                            } catch (SQLException ex) { ex.printStackTrace(); }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tv.getColumns().addAll(
                colId, colPrenom, colNom, colEmail,
                colPhone, colRole, colStatus, colActions
        );
        return tv;
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14));
        bar.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            """);

        Button approveAllBtn = makeActionBtn(
                "✅  Approuver tous les comptes en attente", "#bd936b"
        );
        approveAllBtn.setPrefHeight(38);
        approveAllBtn.setOnAction(e -> {
            try {
                int count = 0;
                for (User u : userService.getUsersByStatus("PENDING_ADMIN")) {
                    userService.approveUser(u.getId()); count++;
                }
                for (User u : userService.getUsersByStatus("PENDING_EMAIL")) {
                    userService.approveUser(u.getId()); count++;
                }
                showAlert(Alert.AlertType.INFORMATION,
                        count + " compte(s) approuvé(s) avec succès !");
                refreshTable();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        bar.getChildren().add(approveAllBtn);
        return bar;
    }

    private void refreshTable() {
        try {
            ObservableList<User> list = FXCollections.observableArrayList(
                    userService.getAll()
            );
            table.setItems(list);
            totalLbl.setText(list.size() + " utilisateur(s) au total");
        } catch (SQLException e) {
            e.printStackTrace();
            totalLbl.setText("Erreur de chargement");
        }
    }

    private void applyFilter(String search, String role, String status) {
        try {
            ObservableList<User> all = FXCollections.observableArrayList(userService.getAll());
            ObservableList<User> filtered = all.filtered(u -> {
                boolean matchSearch = search == null || search.isEmpty()
                        || u.getFullName().toLowerCase().contains(search.toLowerCase())
                        || u.getEmail().toLowerCase().contains(search.toLowerCase());
                boolean matchRole = "Tous les rôles".equals(role)
                        || u.getRole().name().equals(role);
                boolean matchStatus = "Tous les statuts".equals(status)
                        || u.getStatus().name().equals(status);
                return matchSearch && matchRole && matchStatus;
            });
            table.setItems(filtered);
            totalLbl.setText(filtered.size() + " utilisateur(s) trouvé(s)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setTitle("Purrly");
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public BorderPane getView() { return root; }

    private Button makeSideBtn(String text, boolean active) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(12, 16, 12, 16));
        b.setFont(Font.font("Segoe UI",
                active ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        b.setStyle(active
                ? "-fx-background-color: #bd936b; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: #dad8cd; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        return b;
    }

    private Button makeActionBtn(String text, String bg) {
        Button b = new Button(text);
        b.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-background-radius: 6;
            -fx-padding: 6 10;
            -fx-cursor: hand;
            """, bg));
        return b;
    }
}