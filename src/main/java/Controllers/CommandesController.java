package Controllers;

import entities.Commande;
import entities.Facture;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.CommandeService;
import services.KonnectPaymentService;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandesController {

    private static final DateTimeFormatter DATE_LONG =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_SHORT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    @FXML private VBox      commandesContainer;
    @FXML private Label     emptyCommandesLabel;

    // ══════════════════════════════════════════════
    //  🔍  NOUVEAU : champ de recherche par ID
    // ══════════════════════════════════════════════
    @FXML private TextField searchCommandeField;

    /** Cache de toutes les commandes (non filtrées) */
    private List<Commande> allCommandes = new ArrayList<>();
    // ══════════════════════════════════════════════

    private final CommandeService commandeService = new CommandeService();
    private final int currentUserId = 3;

    private KonnectPaymentService konnect;

    @FXML
    public void initialize() {
        try {
            konnect = new KonnectPaymentService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Écoute en temps réel : filtre à chaque lettre tapée
        if (searchCommandeField != null) {
            searchCommandeField.textProperty().addListener(
                    (obs, oldVal, newVal) -> applySearch(newVal)
            );
        }

        loadCommandes();
    }

    // ══════════════════════════════════════════════
    //  🔍  Handlers boutons recherche
    // ══════════════════════════════════════════════

    @FXML
    private void handleSearchCommande() {
        String text = searchCommandeField == null ? "" : searchCommandeField.getText();
        applySearch(text);
    }

    @FXML
    private void handleClearSearch() {
        if (searchCommandeField != null) {
            searchCommandeField.clear();
        }
        applySearch("");
    }

    /**
     * Filtre {@code allCommandes} selon le texte tapé.
     * Recherche par ID exact ou par préfixe.
     */
    private void applySearch(String text) {
        String trimmed = text == null ? "" : text.trim();

        List<Commande> filtered;

        if (trimmed.isEmpty()) {
            filtered = new ArrayList<>(allCommandes);
        } else {
            // Accepte ID exact ou correspondance partielle du numéro
            filtered = allCommandes.stream()
                    .filter(c -> String.valueOf(c.getId_commande()).contains(trimmed))
                    .collect(Collectors.toList());
        }

        renderCommandes(filtered);
    }
    // ══════════════════════════════════════════════

    private void loadCommandes() {
        try {
            allCommandes = new ArrayList<>(commandeService.getMesCommandes(currentUserId));
            allCommandes.sort(
                    Comparator.comparing(Commande::getDate_commande).reversed()
                            .thenComparing(Comparator.comparingInt(Commande::getId_commande).reversed())
            );
            // Affiche tout au démarrage
            applySearch("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Remplit le VBox avec la liste filtrée. */
    private void renderCommandes(List<Commande> list) {
        commandesContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyCommandesLabel.setVisible(empty);
        emptyCommandesLabel.setManaged(empty);
        if (!empty) {
            for (Commande c : list) {
                commandesContainer.getChildren().add(buildOrderCard(c));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Tout le reste est INCHANGÉ par rapport à l'original
    // ─────────────────────────────────────────────────────────────────

    private VBox buildOrderCard(Commande c) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cmd-order-card");

        String statutRaw = c.getStatut() == null ? "" : c.getStatut().trim();
        boolean annulee   = isAnnulee(statutRaw);
        boolean livree    = isLivree(statutRaw);
        boolean confirmee = isConfirmee(statutRaw);
        boolean enAttente = isEnAttente(statutRaw);

        HBox head = new HBox(20);
        head.setAlignment(Pos.TOP_LEFT);
        head.setPadding(new Insets(0, 0, 18, 0));
        head.getStyleClass().add("cmd-order-head");

        VBox titles = new VBox(6);
        Label ref = new Label("COMMANDE · #" + formatRef(c.getId_commande()));
        ref.getStyleClass().add("cmd-order-ref");
        Label dateLbl = new Label(formatLongDate(c.getDate_commande()));
        dateLbl.getStyleClass().add("cmd-order-date");
        titles.getChildren().addAll(ref, dateLbl);

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        Label badge = new Label(statutLabel(statutRaw));
        badge.getStyleClass().addAll("cmd-status-pill", statusPillClass(statutRaw));

        head.getChildren().addAll(titles, grow, badge);
        card.getChildren().add(head);

        if (annulee) {
            Label banner = new Label("Cette commande a été annulée.");
            banner.getStyleClass().add("cmd-cancel-banner");
            card.getChildren().add(banner);
        } else {
            card.getChildren().add(buildTimeline(livree, confirmee, enAttente));
        }

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("cmd-sep");
        card.getChildren().add(sep1);

        VBox details = new VBox(12);
        details.getStyleClass().add("cmd-details-block");
        details.setPadding(new Insets(18, 0, 12, 0));
        details.getChildren().addAll(
                detailRow("Référence",         "#" + formatRef(c.getId_commande())),
                detailRow("Date de commande",   formatShortDate(c.getDate_commande())),
                detailRow("Mode de paiement",   formatPaiement(c.getMode_paiement())),
                detailRow("Réf. panier",        c.getPanierId() > 0 ? "#" + c.getPanierId() : "—")
        );
        card.getChildren().add(details);

        try {
            Facture facture = commandeService.getFactureByCommandeId(c.getId_commande());
            if (facture != null) {
                Separator sepF = new Separator();
                sepF.getStyleClass().add("cmd-sep");
                card.getChildren().add(sepF);

                VBox fac = new VBox(10);
                fac.getStyleClass().add("cmd-facture-block");
                fac.setPadding(new Insets(16, 0, 8, 0));

                Label facTitle = new Label("FACTURE");
                facTitle.getStyleClass().add("cmd-facture-kicker");

                fac.getChildren().addAll(
                        facTitle,
                        detailRow("N° facture",    "#" + formatRef(facture.getId_facture())),
                        detailRow("Date facture",   formatShortDate(facture.getDate_facture())),
                        detailRow("Montant facturé",formatMoney(facture.getMontant_total())),
                        detailRow("Nom",            facture.getNom_receiver()),
                        detailRow("Email",          facture.getEmail_receiver()),
                        detailRow("Téléphone",      facture.getTelephone_receiver()),
                        detailRow("Adresse",        facture.getAdresse_receiver())
                );
                card.getChildren().add(fac);
            }
        } catch (SQLException ignored) {}

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("cmd-sep-strong");
        card.getChildren().add(sep2);

        HBox totalRow = new HBox(16);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(16, 0, 4, 0));
        totalRow.getStyleClass().add("cmd-total-row");
        Label totalLbl = new Label("TOTAL");
        totalLbl.getStyleClass().add("cmd-total-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label totalVal = new Label(formatMoney(c.getMontant_total()));
        totalVal.getStyleClass().add("cmd-total-value");
        totalRow.getChildren().addAll(totalLbl, spacer, totalVal);
        card.getChildren().add(totalRow);

        boolean paiementEnLigne = "en_ligne".equalsIgnoreCase(
                c.getMode_paiement() == null ? "" : c.getMode_paiement().trim());

        if (enAttente && paiementEnLigne && konnect != null) {
            Separator sepPay = new Separator();
            sepPay.getStyleClass().add("cmd-sep");
            card.getChildren().add(sepPay);

            Button btnPayer = new Button("💳  Payer maintenant");
            btnPayer.setStyle(
                    "-fx-background-color:#0e3960;-fx-text-fill:white;" +
                            "-fx-font-size:14px;-fx-font-weight:bold;" +
                            "-fx-padding:12 32 12 32;-fx-background-radius:24;-fx-cursor:hand;");
            btnPayer.setMaxWidth(Double.MAX_VALUE);
            btnPayer.setOnAction(e -> handlePayer(c, btnPayer));

            VBox payBox = new VBox(12, btnPayer);
            payBox.setPadding(new Insets(16, 0, 8, 0));
            card.getChildren().add(payBox);
        }

        return card;
    }

    private void handlePayer(Commande c, Button btn) {
        btn.setDisable(true);
        btn.setText("Redirection en cours…");
        new Thread(() -> {
            try {
                long millimes   = (long) (c.getMontant_total() * 1000);
                String orderId  = "CMD-" + c.getId_commande();
                String wallet   = konnect.getDefaultSellerWalletId();
                String payUrl   = konnect.initSplitPayment(wallet, millimes,
                        "Commande #" + c.getId_commande() + " — Purrly", orderId);
                Desktop.getDesktop().browse(new URI(payUrl));
                javafx.application.Platform.runLater(() -> {
                    btn.setText("✅ Navigateur ouvert !");
                    btn.setStyle("-fx-background-color:#16a34a;-fx-text-fill:white;" +
                            "-fx-font-size:14px;-fx-padding:12 32 12 32;-fx-background-radius:24;");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    btn.setDisable(false);
                    btn.setText("❌ Erreur — Réessayer");
                    btn.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;" +
                            "-fx-font-size:14px;-fx-padding:12 32 12 32;" +
                            "-fx-background-radius:24;-fx-cursor:hand;");
                });
            }
        }).start();
    }

    // ── Helpers UI ─────────────────────────────────────────────────

    private static HBox detailRow(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("cmd-detail-row");
        Label l = new Label(label);
        l.getStyleClass().add("cmd-detail-key");
        l.setMinWidth(148);
        Region g = new Region();
        HBox.setHgrow(g, Priority.ALWAYS);
        Label v = new Label(value);
        v.getStyleClass().add("cmd-detail-val");
        v.setWrapText(true);
        row.getChildren().addAll(l, g, v);
        return row;
    }

    private HBox buildTimeline(boolean livree, boolean confirmee, boolean enAttente) {
        boolean step2  = !enAttente;
        boolean step34 = livree;
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 18, 0));
        row.getStyleClass().add("cmd-timeline");
        row.getChildren().add(timelineStep("Commande",   "Reçue",                           true));
        row.getChildren().add(timelineConnector(step2));
        row.getChildren().add(timelineStep("Préparation",confirmee||livree?"Confirmée":"En attente", step2));
        row.getChildren().add(timelineConnector(step34));
        row.getChildren().add(timelineStep("Expédition", livree ? "Envoyée" : "À venir",    step34));
        row.getChildren().add(timelineConnector(step34));
        row.getChildren().add(timelineStep("Livraison",  livree ? "Livrée"  : "À venir",    step34));
        return row;
    }

    private static VBox timelineStep(String title, String sub, boolean active) {
        VBox col = new VBox(4);
        col.setAlignment(Pos.CENTER);
        col.setMinWidth(82);
        Label dot = new Label("●");
        dot.getStyleClass().addAll("cmd-timeline-dot", active ? "cmd-dot-on" : "cmd-dot-off");
        Label t = new Label(title);
        t.getStyleClass().add("cmd-timeline-title");
        Label s = new Label(sub);
        s.getStyleClass().add("cmd-timeline-sub");
        if (active) s.getStyleClass().add("cmd-timeline-sub-on");
        col.getChildren().addAll(dot, t, s);
        return col;
    }

    private Region timelineConnector(boolean active) {
        Region r = new Region();
        r.setPrefHeight(2); r.setMinHeight(2); r.setMaxHeight(2); r.setMinWidth(16);
        HBox.setHgrow(r, Priority.ALWAYS);
        r.getStyleClass().addAll("cmd-timeline-line", active ? "cmd-line-on" : "cmd-line-off");
        return r;
    }

    private static String formatRef(int id)       { return String.format(Locale.FRENCH, "%05d", id); }
    private static String formatMoney(float a)     { return String.format(Locale.FRENCH, "%.2f TND", a); }
    private static String formatLongDate(LocalDate d) {
        if (d == null) return "—";
        String s = d.format(DATE_LONG);
        return s.substring(0, 1).toUpperCase(Locale.FRENCH) + s.substring(1);
    }
    private static String formatShortDate(LocalDate d) { return d == null ? "—" : d.format(DATE_SHORT); }
    private static String formatPaiement(String mode) {
        if (mode == null || mode.isBlank()) return "—";
        return switch (mode.trim()) {
            case "livraison" -> "Paiement à la livraison";
            case "en_ligne"  -> "Paiement en ligne";
            default          -> mode;
        };
    }
    private static String statutLabel(String raw) {
        if (raw == null || raw.isBlank()) return "Statut inconnu";
        String n = raw.trim().toLowerCase(Locale.FRENCH);
        if (n.contains("annul"))   return "Annulée";
        if (n.contains("livr"))    return "Livrée";
        if (n.contains("confirm")) return "Confirmée";
        if (n.contains("attente")) return "En traitement";
        return raw;
    }
    private static String statusPillClass(String raw) {
        if (raw == null) return "cmd-pill-default";
        String n = raw.trim().toLowerCase(Locale.FRENCH);
        if (n.contains("annul"))   return "cmd-pill-cancel";
        if (n.contains("livr"))    return "cmd-pill-ok";
        if (n.contains("confirm")) return "cmd-pill-info";
        return "cmd-pill-wait";
    }
    private static boolean isAnnulee(String r)  { return r != null && r.toLowerCase(Locale.FRENCH).contains("annul"); }
    private static boolean isLivree(String r)    { return r != null && r.toLowerCase(Locale.FRENCH).contains("livr"); }
    private static boolean isConfirmee(String r) { return r != null && r.toLowerCase(Locale.FRENCH).contains("confirm"); }
    private static boolean isEnAttente(String r) { return r != null && r.toLowerCase(Locale.FRENCH).contains("attente"); }
}