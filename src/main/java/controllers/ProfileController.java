package controllers;

import entities.User;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;

public class ProfileController {

    private Stage stage;
    private User user;
    private BorderPane root;

    public ProfileController(Stage stage, User user) {
        this.stage = stage;
        this.user  = user;
        buildUI();
    }

    private void buildUI() {

        root = new BorderPane();
        root.setStyle("-fx-background-color: #dad8cd;");

        // ════════════════════════════════════════
        // SIDEBAR
        // ════════════════════════════════════════
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #0e3960;");

        VBox logoBox = new VBox(5);
        logoBox.setPadding(new Insets(28, 20, 28, 20));
        Label logo = new Label("🐾 Purrly");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web("#bd936b"));
        Label roleLabel = new Label(user.getRole().name());
        roleLabel.setFont(Font.font("Segoe UI", 11));
        roleLabel.setTextFill(Color.web("#dad8cd"));
        roleLabel.setOpacity(0.7);
        logoBox.getChildren().addAll(logo, roleLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = new Button("🚪  Déconnexion");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setAlignment(Pos.CENTER_LEFT);
        btnLogout.setPadding(new Insets(12, 16, 12, 16));
        btnLogout.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #ff6b6b;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            """);

        VBox bottomMenu = new VBox(btnLogout);
        bottomMenu.setPadding(new Insets(0, 8, 20, 8));

        sidebar.getChildren().addAll(
                logoBox, new Separator(), spacer, bottomMenu
        );

        // ════════════════════════════════════════
        // CONTENU PRINCIPAL
        // ════════════════════════════════════════
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #dad8cd; -fx-background-color: #dad8cd;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label pageTitle = new Label("Mon Profil");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        pageTitle.setTextFill(Color.web("#0e3960"));

        // ── Carte profil ────────────────────────
        VBox card = new VBox(18);
        card.setPadding(new Insets(30));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            """);

        // Avatar avec initiale
        String initiale = String.valueOf(
                user.getFirstName().charAt(0)
        ).toUpperCase();
        Label avatar = new Label(initiale);
        avatar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        avatar.setTextFill(Color.WHITE);
        avatar.setStyle("""
            -fx-background-color: #bd936b;
            -fx-background-radius: 50;
            -fx-padding: 16 22;
            """);

        Label fullName = new Label(user.getFullName());
        fullName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        fullName.setTextFill(Color.web("#0e3960"));

        Label statusBadge = new Label("● " + user.getStatus().name());
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusBadge.setTextFill(Color.web("#2e7d32"));

        HBox avatarBox = new HBox(16, avatar,
                new VBox(6, fullName, statusBadge)
        );
        avatarBox.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();

        // ── Formulaire modification ─────────────
        Label editTitle = new Label("Modifier mes informations");
        editTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        editTitle.setTextFill(Color.web("#0e3960"));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);

        TextField fnField   = makeField(user.getFirstName());
        TextField lnField   = makeField(user.getLastName());
        TextField emailFld  = makeField(user.getEmail());
        TextField phoneFld  = makeField(user.getPhone()   != null ? user.getPhone()   : "");
        TextField cityFld   = makeField(user.getCity()    != null ? user.getCity()    : "");
        TextField addrFld   = makeField(user.getAddress() != null ? user.getAddress() : "");

        grid.add(makeLabel("Prénom"),    0, 0); grid.add(fnField,  1, 0);
        grid.add(makeLabel("Nom"),       0, 1); grid.add(lnField,  1, 1);
        grid.add(makeLabel("Email"),     0, 2); grid.add(emailFld, 1, 2);
        grid.add(makeLabel("Téléphone"), 0, 3); grid.add(phoneFld, 1, 3);
        grid.add(makeLabel("Ville"),     0, 4); grid.add(cityFld,  1, 4);
        grid.add(makeLabel("Adresse"),   0, 5); grid.add(addrFld,  1, 5);

        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        // Message feedback
        Label msgLbl = new Label("");
        msgLbl.setFont(Font.font("Segoe UI", 12));
        msgLbl.setWrapText(true);

        // Bouton sauvegarder
        Button saveBtn = new Button("💾  Enregistrer les modifications");
        saveBtn.setStyle("""
            -fx-background-color: #bd936b;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 12 24;
            -fx-cursor: hand;
            """);

        saveBtn.setOnAction(e -> {
            user.setFirstName(fnField.getText().trim());
            user.setLastName(lnField.getText().trim());
            user.setEmail(emailFld.getText().trim());
            user.setPhone(phoneFld.getText().trim());
            user.setCity(cityFld.getText().trim());
            user.setAddress(addrFld.getText().trim());

            try {
                new UserService().update(user);
                msgLbl.setTextFill(Color.web("#2e7d32"));
                msgLbl.setText("✅ Profil mis à jour avec succès !");
                fullName.setText(user.getFullName());
            } catch (SQLException ex) {
                msgLbl.setTextFill(Color.RED);
                msgLbl.setText("❌ Erreur lors de la mise à jour.");
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(
                avatarBox, sep, editTitle, grid, msgLbl, saveBtn
        );

        content.getChildren().addAll(pageTitle, card);
        scroll.setContent(content);

        // ── Navigation ──────────────────────────
        btnLogout.setOnAction(e -> {
            LoginController login = new LoginController(stage);
            stage.getScene().setRoot(login.getView());
        });

        root.setLeft(sidebar);
        root.setCenter(scroll);
    }

    public BorderPane getView() { return root; }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        l.setTextFill(Color.web("#0e3960"));
        return l;
    }

    private TextField makeField(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("""
            -fx-background-color: #f5f4f1;
            -fx-border-color: #dad8cd;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 10 14;
            -fx-font-size: 13px;
            """);
        tf.setPrefHeight(40);
        return tf;
    }
}