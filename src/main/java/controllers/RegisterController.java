package controllers;

import entities.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import services.UserService;

import java.sql.SQLException;

public class RegisterController {

    private Stage stage;
    private BorderPane root;

    public RegisterController(Stage stage) {
        this.stage = stage;
        buildUI();
    }

    private void buildUI() {

        root = new BorderPane();
        root.setStyle("-fx-background-color: #dad8cd;");

        // ════════════════════════════════════════
        // PANNEAU GAUCHE — Branding bleu
        // ════════════════════════════════════════
        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(380);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPadding(new Insets(60));
        leftPanel.setStyle("-fx-background-color: #0e3960;");

        Label appName = new Label("🐾 Purrly");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        appName.setTextFill(Color.web("#bd936b"));

        Label sub = new Label("Créez votre compte\net rejoignez\nla communauté Purrly");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setTextFill(Color.web("#dad8cd"));
        sub.setOpacity(0.85);
        sub.setTextAlignment(TextAlignment.CENTER);

        Label info = new Label("ℹ Votre compte sera activé\naprès validation par un admin.");
        info.setFont(Font.font("Segoe UI", 12));
        info.setTextFill(Color.web("#bd936b"));
        info.setTextAlignment(TextAlignment.CENTER);

        leftPanel.getChildren().addAll(appName, new Separator(), sub, info);

        // ════════════════════════════════════════
        // PANNEAU DROIT — Formulaire inscription
        // ════════════════════════════════════════
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("""
            -fx-background: white;
            -fx-background-color: white;
            -fx-border-color: transparent;
            """);

        VBox form = new VBox(14);
        form.setAlignment(Pos.TOP_LEFT);
        form.setPadding(new Insets(50, 80, 50, 80));
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Inscription");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#0e3960"));

        Label subtitle = new Label("Remplissez le formulaire ci-dessous");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.GRAY);

        TextField firstNameField = makeTextField("Ex: Ghofrane");
        TextField lastNameField  = makeTextField("Ex: Ben Zid");
        TextField emailField     = makeTextField("exemple@email.com");
        PasswordField passField  = new PasswordField();
        passField.setPromptText("Minimum 6 caractères");
        styleInput(passField);
        TextField phoneField   = makeTextField("Ex: 25352124");
        TextField cityField    = makeTextField("Ex: Tunis");
        TextField addressField = makeTextField("Ex: 12 Rue des Roses");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("CLIENT", "VETERINAIRE", "PETSITTER", "SALON_TOILETTAGE");
        roleBox.setValue("CLIENT");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(42);
        roleBox.setStyle("""
            -fx-background-color: #f5f4f1;
            -fx-border-color: #dad8cd;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-font-size: 13px;
            """);

        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.RED);
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);

        Label successLbl = new Label("");
        successLbl.setTextFill(Color.web("#2e7d32"));
        successLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        successLbl.setWrapText(true);

        Button registerBtn = makeButton("Créer mon compte", "#bd936b");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink loginLink = new Hyperlink("Déjà un compte ? Se connecter");
        loginLink.setTextFill(Color.web("#0e3960"));
        loginLink.setStyle("-fx-border-color: transparent;");

        // ════════════════════════════════════════
        // ACTIONS
        // ════════════════════════════════════════

        registerBtn.setOnAction(e -> {
            String fn    = firstNameField.getText().trim();
            String ln    = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = passField.getText().trim();
            String phone = phoneField.getText().trim();
            String city  = cityField.getText().trim();
            String addr  = addressField.getText().trim();
            String role  = roleBox.getValue();

            // Validation champs obligatoires
            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()
                    || pass.isEmpty() || phone.isEmpty()) {
                errorLbl.setText("⚠ Prénom, nom, email, mot de passe et téléphone sont obligatoires.");
                successLbl.setText("");
                return;
            }

            // Validation email inline
            if (!email.contains("@") || !email.contains(".")) {
                errorLbl.setText("⚠ Adresse email invalide.");
                successLbl.setText("");
                return;
            }

            // Validation mot de passe
            if (pass.length() < 6) {
                errorLbl.setText("⚠ Le mot de passe doit contenir au moins 6 caractères.");
                successLbl.setText("");
                return;
            }

            // Validation téléphone inline (8 chiffres)
            if (!phone.matches("\\d{8}")) {
                errorLbl.setText("⚠ Numéro de téléphone invalide (8 chiffres requis).");
                successLbl.setText("");
                return;
            }

            try {
                User user = new User(fn, ln, email, pass, phone, role);
                user.setCity(city);
                user.setAddress(addr);

                UserService us = new UserService();
                us.add(user);

                successLbl.setText("✅ Compte créé avec succès ! En attente de validation admin.");
                errorLbl.setText("");

                firstNameField.clear();
                lastNameField.clear();
                emailField.clear();
                passField.clear();
                phoneField.clear();
                cityField.clear();
                addressField.clear();
                roleBox.setValue("CLIENT");

            } catch (SQLException ex) {
                String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                if (message.contains("duplicate") || message.contains("unique")) {
                    errorLbl.setText("❌ Cet email est déjà utilisé.");
                } else {
                    errorLbl.setText("❌ Erreur technique, veuillez réessayer.");
                }
                successLbl.setText("");
            }
        });

        loginLink.setOnAction(e -> {
            LoginController login = new LoginController(stage);
            stage.getScene().setRoot(login.getView());
        });

        form.getChildren().addAll(
                title,
                subtitle,
                new Separator(),
                makeLabel("Prénom *"),        firstNameField,
                makeLabel("Nom *"),           lastNameField,
                makeLabel("Email *"),         emailField,
                makeLabel("Mot de passe *"),  passField,
                makeLabel("Téléphone *"),     phoneField,
                makeLabel("Ville"),           cityField,
                makeLabel("Adresse"),         addressField,
                makeLabel("Je suis un(e) *"), roleBox,
                errorLbl,
                successLbl,
                registerBtn,
                loginLink
        );

        scroll.setContent(form);
        root.setLeft(leftPanel);
        root.setCenter(scroll);
    }

    public BorderPane getView() {
        return root;
    }

    // ──────────────────────────────────────────────────
    // MÉTHODES UTILITAIRES DE STYLE
    // ──────────────────────────────────────────────────
    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        l.setTextFill(Color.web("#0e3960"));
        return l;
    }

    private TextField makeTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        styleInput(tf);
        return tf;
    }

    private void styleInput(Control c) {
        c.setStyle("""
            -fx-background-color: #f5f4f1;
            -fx-border-color: #dad8cd;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 10 14;
            -fx-font-size: 13px;
            """);
        c.setPrefHeight(42);
    }

    private Button makeButton(String text, String couleur) {
        Button b = new Button(text);
        b.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 12 24;
            -fx-cursor: hand;
            """, couleur));
        b.setPrefHeight(46);
        return b;
    }
}
