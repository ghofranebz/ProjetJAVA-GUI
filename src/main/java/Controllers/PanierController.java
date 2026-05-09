package Controllers;

import entities.Facture;
import entities.PanierItem;
import entities.Produit;
import entities.Commande;
import services.EmailService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import services.CommandeService;
import services.KonnectPaymentService;
import services.PanierService;
import services.ProduitService;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javafx.scene.control.TextField;

public class PanierController {
    private EmailService emailService;

    private EmailService getEmailService() {
        if (emailService == null) {
            try { emailService = new EmailService(); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return emailService;
    }

    private static final double THUMB = 96;

    @FXML
    private VBox cartContainer;

    @FXML
    private Label totalLabel;

    @FXML
    private ComboBox<String> modePaiementBox;

    @FXML
    private Label cartEmptyLabel;

    @FXML
    private VBox checkoutSection;

    @FXML
    private VBox factureSection;

    @FXML
    private Label factureIdLabel;

    @FXML
    private Label factureCommandeLabel;

    @FXML
    private Label factureDateLabel;

    @FXML
    private Label factureMontantLabel;

    @FXML
    private VBox factureLinesBox;

    @FXML private TextField fieldNomReceiver;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelephone;
    @FXML private TextField fieldAdresse;

    @FXML private Label factureReceiverLabel;
    @FXML private Label factureEmailLabel;
    @FXML private Label factureTelLabel;
    @FXML private Label factureAdresseLabel;

    private final PanierService panierService = new PanierService();
    private final CommandeService commandeService = new CommandeService();
    private final ProduitService produitService = new ProduitService();

    private KonnectPaymentService konnectService;

    private KonnectPaymentService getKonnectService() throws IOException {
        if (konnectService == null) {
            konnectService = new KonnectPaymentService();
        }
        return konnectService;
    }

    private final int currentUserId = 3;

    private static final DateTimeFormatter FACTURE_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {

        modePaiementBox.getItems().setAll(
                "Livraison à domicile",
                "Paiement en ligne"
        );

        hideFacture();

        loadPanier();

        loadUserInfo();
    }

    private void loadPanier() {

        try {

            List<PanierItem> items =
                    panierService.getCartByUser(currentUserId);

            if (!items.isEmpty()) {

                hideFacture();
            }

            cartContainer.getChildren().clear();

            boolean empty = items.isEmpty();

            cartEmptyLabel.setVisible(empty);
            cartEmptyLabel.setManaged(empty);

            checkoutSection.setVisible(!empty);
            checkoutSection.setManaged(!empty);

            if (empty) {

                totalLabel.setText(formatMoney(0f));

                return;
            }

            for (PanierItem item : items) {

                Produit p = produitService.getById(item.getProduitId());

                cartContainer.getChildren().add(
                        createLine(item, p)
                );
            }

            float total =
                    panierService.getTotalPanier(currentUserId);

            totalLabel.setText(formatMoney(total));

        } catch (SQLException e) {

            showAlert("Erreur", "Impossible de charger le panier.");
        }
    }

    private void hideFacture() {

        if (factureSection != null) {

            factureSection.setVisible(false);
            factureSection.setManaged(false);
        }

        if (factureLinesBox != null) {

            factureLinesBox.getChildren().clear();
        }
    }

    private void showFacture(Facture facture, List<PanierItem> lignesPanier) {
        if (factureSection == null || facture == null) return;

        factureIdLabel.setText("#" + facture.getId_facture());
        factureCommandeLabel.setText("#" + facture.getCommande_id());
        factureDateLabel.setText(facture.getDate_facture().format(FACTURE_DATE));
        factureMontantLabel.setText(formatMoney(facture.getMontant_total()));
        factureReceiverLabel.setText(fieldNomReceiver.getText());
        factureEmailLabel.setText(fieldEmail.getText());
        factureTelLabel.setText(fieldTelephone.getText());
        factureAdresseLabel.setText(fieldAdresse.getText());

        // ✅ Nouvelles infos
        factureReceiverLabel.setText(fieldNomReceiver.getText());
        factureEmailLabel.setText(fieldEmail.getText());
        factureTelLabel.setText(fieldTelephone.getText());
        factureAdresseLabel.setText(fieldAdresse.getText());

        factureLinesBox.getChildren().clear();
        for (PanierItem item : lignesPanier) {
            try {
                Produit p = produitService.getById(item.getProduitId());
                String nom = p != null ? p.getNom() : "Produit #" + item.getProduitId();
                float unit = p != null ? p.getPrix() : 0f;
                float ligne = unit * item.getQuantite();

                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("retail-facture-line");

                Label left = new Label(nom + " × " + item.getQuantite());
                left.getStyleClass().add("retail-facture-line-product");
                left.setWrapText(true);

                Region grow = new Region();
                HBox.setHgrow(grow, Priority.ALWAYS);

                Label right = new Label(formatMoney(ligne));
                right.getStyleClass().add("retail-facture-line-price");

                row.getChildren().addAll(left, grow, right);
                factureLinesBox.getChildren().add(row);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        factureSection.setVisible(true);
        factureSection.setManaged(true);
    }

    private static String formatMoney(float amount) {

        return String.format(Locale.FRENCH, "%.2f TND", amount);
    }

    private HBox createLine(PanierItem item, Produit prod) {

        HBox row = new HBox(28);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(18, 0, 18, 0));
        row.getStyleClass().add("cart-line-retail");

        ImageView thumb = buildThumbnail(prod, item.getProduitId());

        VBox copy = new VBox(10);

        String nom =
                prod != null
                        ? prod.getNom()
                        : "Produit #" + item.getProduitId();

        Label name = new Label(nom);
        name.getStyleClass().add("cart-line-name");
        name.setWrapText(true);

        Button remove = new Button("Retirer");
        remove.getStyleClass().add("btn-retail-link");
        remove.setOnAction(e -> {

            try {

                panierService.removeLine(currentUserId, item.getProduitId());

                loadPanier();

            } catch (SQLException ex) {

                showAlert("Erreur", "Suppression impossible");
            }
        });

        copy.getChildren().addAll(name, remove);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox qty = new HBox(0);
        qty.getStyleClass().add("cart-qty-retail");
        qty.setAlignment(Pos.CENTER_LEFT);

        Button minus = new Button("−");
        minus.getStyleClass().add("btn-qty-retail");
        minus.setOnAction(e -> changeQty(item, -1));

        Label qtyLbl =
                new Label(String.valueOf(item.getQuantite()));

        qtyLbl.getStyleClass().add("cart-qty-num");

        Button plus = new Button("+");
        plus.getStyleClass().add("btn-qty-retail");
        plus.setOnAction(e -> changeQty(item, 1));

        qty.getChildren().addAll(minus, qtyLbl, plus);

        float unit = prod != null ? prod.getPrix() : 0f;
        float lineTotal = unit * item.getQuantite();

        Label amount =
                new Label(formatMoney(lineTotal));

        amount.getStyleClass().add("cart-line-amount");

        row.getChildren().addAll(thumb, copy, spacer, qty, amount);

        return row;
    }

    private void changeQty(PanierItem item, int delta) {

        try {

            if (delta < 0) {

                panierService.decreaseQuantity(
                        currentUserId,
                        item.getProduitId()
                );

            } else {

                panierService.increaseQuantity(
                        currentUserId,
                        item.getProduitId()
                );
            }

            loadPanier();

        } catch (SQLException ex) {

            ex.printStackTrace();
        }
    }

    private ImageView buildThumbnail(Produit prod, int produitId) {

        ImageView iv = new ImageView();
        iv.setFitWidth(THUMB);
        iv.setFitHeight(THUMB);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        Rectangle clip = new Rectangle(THUMB, THUMB);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        iv.setClip(clip);

        if (prod != null
                && prod.getImagePath() != null
                && !prod.getImagePath().isBlank()) {

            try {

                String path =
                        prod.getImagePath().split("[;,]")[0].trim();

                Image img =
                        new Image(
                                "file:" + path,
                                THUMB * 2,
                                THUMB * 2,
                                true,
                                true
                        );

                iv.setImage(img);

            } catch (Exception ignored) {
            }
        }

        iv.getStyleClass().add("cart-thumb-retail");

        return iv;
    }

    @FXML
    private void handleVider() {

        try {

            hideFacture();

            panierService.clearCart(currentUserId);

            loadPanier();

        } catch (SQLException e) {

            showAlert("Erreur", "Impossible de vider le panier.");
        }
    }

    @FXML
    private void handleCommander() {

        String nomReceiver = fieldNomReceiver.getText() == null ? "" : fieldNomReceiver.getText().trim();
        String email       = fieldEmail.getText() == null ? "" : fieldEmail.getText().trim();
        String telephone   = fieldTelephone.getText() == null ? "" : fieldTelephone.getText().trim();
        String adresse     = fieldAdresse.getText() == null ? "" : fieldAdresse.getText().trim();

        if (nomReceiver.isEmpty() || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
            showAlert("Champs requis", "Veuillez remplir toutes les informations de livraison.");
            return;
        }

        try {

            String sel = modePaiementBox.getValue();

            String mode =
                    switch (sel == null ? "" : sel) {

                        case "Livraison à domicile" -> "livraison";

                        case "Paiement en ligne" -> "en_ligne";

                        default -> null;

                    };

            if (mode == null) {

                showAlert("Attention", "Choisissez un mode de paiement");
                return;
            }

            float total =
                    panierService.getTotalPanier(currentUserId);

            if (total <= 0) {

                showAlert("Attention", "Votre panier est vide.");
                return;
            }

            List<PanierItem> lignesAvantVente =
                    new ArrayList<>(
                            panierService.getCartByUser(currentUserId)
                    );

            if ("en_ligne".equals(mode)) {

                try {

                    KonnectPaymentService k = getKonnectService();

                    String sellerWallet = resolveSellerWalletForCart(lignesAvantVente, k);

                    long totalMillimes = Math.max(1, Math.round(total * 1000.0));
                    long platformMillimes = k.platformShareMillimes(totalMillimes);
                    long sellerMillimes = totalMillimes - platformMillimes;

                    double commissionPct = k.getCommissionRate() * 100.0;

                    String summary =
                            String.format(
                                    Locale.FRENCH,
                                    "Prix produits (total) : %.2f TND%n"
                                            + "Commission plateforme (%.2f %%) : %.2f TND%n"
                                            + "Vendeur reçoit : %.2f TND%n"
                                            + "Total payé par le client : %.2f TND%n%n"
                                            + "Portefeuille vendeur (receiverWalletId) : %s",
                                    total,
                                    commissionPct,
                                    platformMillimes / 1000.0,
                                    sellerMillimes / 1000.0,
                                    total,
                                    sellerWallet);

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Paiement Konnect");
                    confirm.setHeaderText("Récapitulatif avant redirection");
                    TextArea ta = new TextArea(summary);
                    ta.setEditable(false);
                    ta.setWrapText(true);
                    ta.setPrefRowCount(11);
                    ta.setPrefWidth(460);
                    VBox content = new VBox(10);
                    content.getChildren()
                            .addAll(
                                    new Label(
                                            "Le client paie le montant total. Konnect crédite le vendeur "
                                                    + "et prélève la commission plateforme via split payment."),
                                    ta);
                    content.setPadding(new Insets(8));
                    confirm.getDialogPane().setContent(content);
                    Optional<ButtonType> chosen = confirm.showAndWait();
                    if (chosen.isEmpty() || chosen.get() != ButtonType.OK) {
                        return;
                    }

                    String orderId = "ORD-" + UUID.randomUUID();

                    startKonnectOnlineCheckout(
                            total,
                            lignesAvantVente,
                            sellerWallet,
                            orderId);

                } catch (IOException ioe) {

                    showAlert("Configuration Konnect", ioe.getMessage());

                } catch (IllegalStateException ex) {

                    showAlert("Paiement en ligne", ex.getMessage());

                } catch (SQLException ex) {

                    showAlert("Erreur", ex.getMessage());
                }

                return;
            }

            int idCommande =
                    commandeService.passerCommande(
                            currentUserId,
                            panierService.getPanierIdByUser(currentUserId),
                            total,
                            mode,
                            nomReceiver,
                            email,
                            telephone,
                            adresse
                    );


            Facture facture =
                    commandeService.getFactureByCommandeId(idCommande);

            panierService.clearCart(currentUserId);

            if (facture != null) {

                showFacture(facture, lignesAvantVente);
                // 📧 Envoi email facture
                if (getEmailService() != null && facture != null) {
                    CommandeService commandeService = new CommandeService();
                    Commande cmdEmail = commandeService.getById(idCommande);
                    getEmailService().sendFactureEmail(
                            currentUserId, facture, cmdEmail, lignesAvantVente);
                }

                showAlert(
                        "Succès",
                        "Commande #" + idCommande + " enregistrée — facture affichée ci-dessous."
                );

            } else {

                showAlert(
                        "Attention",
                        "Commande #" + idCommande + " créée, mais la facture n’a pas été retrouvée en base."
                );
            }

            loadPanier();

        } catch (SQLException e) {
            e.printStackTrace();

            showAlert("Erreur", "Commande impossible.");
        }
    }

    /**
     * Tous les articles doivent partager le même {@code receiverWalletId} pour un seul flux Konnect.
     * Sinon : panier multi-vendeurs non géré ici.
     */
    private String resolveSellerWalletForCart(List<PanierItem> lignes, KonnectPaymentService k)
            throws SQLException, IllegalStateException {

        String defaultWallet = k.getDefaultSellerWalletId();

        String chosen = null;

        for (PanierItem item : lignes) {

            Produit pr = produitService.getById(item.getProduitId());

            String w = null;
            if (pr != null && pr.getSellerWalletId() != null && !pr.getSellerWalletId().isBlank()) {
                w = pr.getSellerWalletId().trim();
            }
            if (w == null || w.isBlank()) {
                if (defaultWallet != null && !defaultWallet.isBlank()) {
                    w = defaultWallet.trim();
                }
            }

            if (w == null || w.isBlank()) {

                String nom =
                        pr != null && pr.getNom() != null
                                ? pr.getNom()
                                : "Produit #" + item.getProduitId();

                throw new IllegalStateException(
                        "Le produit « "
                                + nom
                                + " » n’a pas de portefeuille Konnect (seller_wallet_id). "
                                + "Renseignez-le dans « Mes produits » ou définissez SELLER_WALLET_ID / "
                                + "konnect.default.seller.wallet dans config.properties.");
            }

            if (chosen == null) {

                chosen = w;

            } else if (!chosen.equals(w)) {

                throw new IllegalStateException(
                        "Panier multi-vendeurs : les articles n’utilisent pas le même "
                                + "portefeuille Konnect. Retirez des articles ou passez plusieurs commandes.");
            }
        }

        return chosen;
    }

    /**
     * Paiement Konnect avec split (commission), puis ouverture du navigateur et enregistrement commande.
     */
    private void startKonnectOnlineCheckout(
            float totalTnd,
            List<PanierItem> lignesAvantVente,
            String sellerWalletId,
            String orderId
    ) {

        long amountMillimes = Math.max(1, Math.round(totalTnd * 1000.0));

        Thread worker =
                new Thread(
                        () -> {
                            try {

                                KonnectPaymentService k = getKonnectService();

                                String payUrl =
                                        k.initSplitPayment(
                                                sellerWalletId,
                                                amountMillimes,
                                                "Commande Purrly",
                                                orderId);

                                Platform.runLater(
                                        () -> {
                                            try {

                                                if (Desktop.isDesktopSupported()
                                                        && Desktop.getDesktop()
                                                        .isSupported(Desktop.Action.BROWSE)) {

                                                    Desktop.getDesktop().browse(new URI(payUrl));

                                                } else {

                                                    showAlert(
                                                            "Paiement",
                                                            "Ouvrez ce lien dans votre navigateur :\n\n"
                                                                    + payUrl
                                                    );
                                                }

                                            } catch (Exception ex) {

                                                showAlert(
                                                        "Paiement",
                                                        "Impossible d’ouvrir le navigateur. Ouvrez manuellement :\n\n"
                                                                + payUrl
                                                );
                                            }

                                            try {

                                                int idCommande =
                                                        commandeService.passerCommande(
                                                                currentUserId,
                                                                panierService.getPanierIdByUser(currentUserId),
                                                                totalTnd,
                                                                "en_ligne",
                                                                fieldNomReceiver.getText(),
                                                                fieldEmail.getText(),
                                                                fieldTelephone.getText(),
                                                                fieldAdresse.getText()
                                                        );

                                                Facture facture =
                                                        commandeService.getFactureByCommandeId(
                                                                idCommande
                                                        );

                                                panierService.clearCart(currentUserId);

                                                if (facture != null) {

                                                    showFacture(facture, lignesAvantVente);
                                                    // 📧 Envoi email facture Konnect
                                                    if (getEmailService() != null && facture != null) {
                                                        try {
                                                            Commande cmdEmail = commandeService.getById(idCommande);
                                                            getEmailService().sendFactureEmail(
                                                                    currentUserId, facture, cmdEmail, lignesAvantVente);
                                                        } catch (Exception ignored) {}
                                                    }

                                                    showAlert(
                                                            "Paiement",
                                                            "La page de paiement Konnect a été ouverte. "
                                                                  + "Finalisez le règlement dans votre navigateur.\n\n"
                                                                  + "Commande #"
                                                                  + idCommande
                                                                  + " — Konnect orderId : "
                                                                  + orderId
                                                                  + "."
                                                    );

                                                } else {

                                                    showAlert(
                                                            "Attention",
                                                            "Commande #"
                                                                    + idCommande
                                                                    + " créée (paiement en attente), "
                                                                  + "mais facture introuvable en base."
                                                    );
                                                }

                                                loadPanier();

                                            } catch (SQLException sqlEx) {

                                                showAlert(
                                                        "Erreur",
                                                        "Paiement initialisé mais enregistrement commande impossible."
                                                );
                                            }
                                        });

                            } catch (Exception ex) {

                                Platform.runLater(
                                        () ->
                                                showAlert(
                                                        "Konnect",
                                                        "Impossible d’initialiser le paiement :\n"
                                                                + ex.getMessage()
                                                )
                                );
                            }
                        },
                        "konnect-init-payment");

        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("/view/Shop.fxml")
                    );

            Parent root = loader.load();

            UiNavigation.showInMainShell(cartContainer.getScene(), root);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void loadUserInfo() {
        try {
            String sql = "SELECT email FROM users WHERE id = ?";
            try (java.sql.PreparedStatement pst =
                         main.tools.Mydb.getInstance().getConnection().prepareStatement(sql)) {
                pst.setInt(1, currentUserId);
                try (java.sql.ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        fieldEmail.setText(rs.getString("email"));
                        // fieldNomReceiver reste vide → l'utilisateur remplit manuellement
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.show();
    }
}
