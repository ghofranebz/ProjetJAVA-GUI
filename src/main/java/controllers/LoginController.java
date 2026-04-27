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
import java.util.List;

public class LoginController {

    private Stage stage;
    private BorderPane root;  // layout principal de cette page

    // ──────────────────────────────────────────────────
    // CONSTRUCTEUR
    // ──────────────────────────────────────────────────
    public LoginController(Stage stage) {
        this.stage = stage;
        buildUI();  // construit l'interface
    }

    // ──────────────────────────────────────────────────
    // CONSTRUCTION DE L'INTERFACE
    // ──────────────────────────────────────────────────
    private void buildUI() {

        // BorderPane = layout avec zones : Left / Center / Right / Top / Bottom
        root = new BorderPane();
        root.setStyle("-fx-background-color: #dad8cd;");

        // ════════════════════════════════════════
        // PANNEAU GAUCHE — Branding bleu
        // ════════════════════════════════════════
        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(400);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPadding(new Insets(60));
        leftPanel.setStyle("-fx-background-color: #0e3960;");

        Label appName = new Label("🐾 Purrly");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 44));
        appName.setTextFill(Color.web("#bd936b"));

        Label tagline = new Label("All-in-One Pet Services");
        tagline.setFont(Font.font("Segoe UI", 16));
        tagline.setTextFill(Color.web("#dad8cd"));

        Label desc = new Label(
                "Gérez vos animaux,\nconsultez un vétérinaire,\nachetez des produits."
        );
        desc.setFont(Font.font("Segoe UI", 13));
        desc.setTextFill(Color.web("#dad8cd"));
        desc.setOpacity(0.75);
        desc.setTextAlignment(TextAlignment.CENTER);

        leftPanel.getChildren().addAll(appName, tagline, new Separator(), desc);

        // ════════════════════════════════════════
        // PANNEAU DROIT — Formulaire de connexion
        // ════════════════════════════════════════
        VBox rightPanel = new VBox(16);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(60, 80, 60, 80));
        rightPanel.setStyle("-fx-background-color: white;");

        // Titre du formulaire
        Label title = new Label("Connexion");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#0e3960"));

        Label subtitle = new Label("Bienvenue sur Purrly");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.GRAY);

        // Champ Email
        Label emailLbl = makeLabel("Adresse Email");
        TextField emailField = makeTextField("exemple@email.com");

        // Champ Mot de passe
        Label passLbl = makeLabel("Mot de passe");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        styleInput(passField);

        // Message d'erreur (invisible au départ)
        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.RED);
        errorLbl.setFont(Font.font("Segoe UI", 12));

        // Bouton Se connecter
        Button loginBtn = makeButton("Se connecter", "#0e3960");
        loginBtn.setMaxWidth(Double.MAX_VALUE); // prend toute la largeur

        // Lien vers inscription
        Hyperlink registerLink = new Hyperlink("Pas de compte ? S'inscrire");
        registerLink.setTextFill(Color.web("#bd936b"));
        registerLink.setStyle("-fx-border-color: transparent;");

        // ════════════════════════════════════════
        // ACTIONS
        // ════════════════════════════════════════

        // Clic sur "Se connecter"
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passField.getText().trim();

            // Validation basique
            if (email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("⚠ Veuillez remplir tous les champs.");
                return;
            }

            try {
                UserService us = new UserService();
                List<User> users = us.getAll();

                // Chercher l'user avec cet email + mot de passe
                User found = users.stream()
                        .filter(u -> u.getEmail().equals(email)
                                && u.getPassword().equals(pass))
                        .findFirst()
                        .orElse(null);

                if (found == null) {
                    errorLbl.setText("❌ Email ou mot de passe incorrect.");

                } else if (!found.isActive()) {
                    errorLbl.setText("⏳ Compte en attente de validation admin.");

                } else {
                    // Redirection selon le rôle
                    if (found.getRole() == User.Role.ADMIN) {
                        AdminController admin = new AdminController(stage, found);
                        stage.getScene().setRoot(admin.getView());
                    } else {
                        ProfileController profile = new ProfileController(stage, found);
                        stage.getScene().setRoot(profile.getView());
                    }
                }

            } catch (SQLException ex) {
                errorLbl.setText("❌ Erreur de connexion à la base de données.");
                ex.printStackTrace();
            }
        });

        // Clic sur "S'inscrire"
        registerLink.setOnAction(e -> {
            RegisterController reg = new RegisterController(stage);
            stage.getScene().setRoot(reg.getView());
        });

        // Ajouter tous les éléments au panneau droit
        rightPanel.getChildren().addAll(
                title,
                subtitle,
                new VBox(5, emailLbl, emailField),
                new VBox(5, passLbl, passField),
                errorLbl,
                loginBtn,
                registerLink
        );

        // Mettre les deux panneaux dans le BorderPane
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
    }

    // ──────────────────────────────────────────────────
    // RETOURNE LA VUE (utilisé dans MainFX et navigation)
    // ──────────────────────────────────────────────────
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

    // Applique le même style à TextField ET PasswordField
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