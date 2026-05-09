package services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Paiement Konnect API v2 avec split (commission plateforme sur le même flux).
 */
public final class KonnectPaymentService {

    private static final String INIT_URL =
            "https://api.sandbox.konnect.network/api/v2/payments/init-payment";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final Gson gson = new Gson();

    private final String apiKey;
    private final String platformWalletId;
    private final String defaultSellerWalletId;
    private final double commissionRate;
    private final String successUrl;
    private final String failUrl;
    private final String webhookUrl;

    public KonnectPaymentService() throws IOException {
        Properties p = loadClasspathProperties();
        this.apiKey = requiredEither(p, "konnect.api.key", "KONNECT_API_KEY");
        this.platformWalletId =
                requiredEither(p, "konnect.platform.wallet.id", "MY_PLATFORM_WALLET_ID");
        this.defaultSellerWalletId =
                firstNonBlank(
                        p.getProperty("konnect.default.seller.wallet"),
                        p.getProperty("SELLER_WALLET_ID"),
                        "");
        this.commissionRate = parseRate(
                firstNonBlank(
                        p.getProperty("konnect.commission.rate"),
                        p.getProperty("COMMISSION_RATE"),
                        "0.10"));
        this.successUrl =
                firstNonBlank(
                        p.getProperty("konnect.success.url"),
                        p.getProperty("successUrl"),
                        "http://localhost/success");
        this.failUrl =
                firstNonBlank(
                        p.getProperty("konnect.fail.url"),
                        p.getProperty("failUrl"),
                        "http://localhost/fail");
        this.webhookUrl =
                firstNonBlank(
                        p.getProperty("konnect.webhook.url"),
                        p.getProperty("webhook"),
                        "");
        if (commissionRate < 0 || commissionRate >= 1) {
            throw new IOException(
                    "konnect.commission.rate / COMMISSION_RATE doit être entre 0 et 1 (ex. 0.10).");
        }
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    /** Commission plateforme en millimes (arrondi). */
    public long platformShareMillimes(long totalMillimes) {
        if (totalMillimes <= 0) {
            return 0;
        }
        return Math.min(totalMillimes, (long) Math.floor(totalMillimes * commissionRate));
    }

    public String getDefaultSellerWalletId() {
        return defaultSellerWalletId;
    }

    public String getPlatformWalletId() {
        return platformWalletId;
    }

    private static double parseRate(String raw) {
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.10;
        }
    }

    private static Properties loadClasspathProperties() throws IOException {
        Properties p = new Properties();
        try (InputStream in =
                     KonnectPaymentService.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new IOException("Fichier introuvable : src/main/resources/config.properties");
            }
            p.load(in);
        }
        return p;
    }

    private static String requiredEither(Properties p, String keyA, String keyB) throws IOException {
        String v = firstNonBlank(p.getProperty(keyA), p.getProperty(keyB), "");
        if (v.isBlank()) {
            throw new IOException(
                    "Propriété manquante dans config.properties : " + keyA + " ou " + keyB);
        }
        return v.trim();
    }

    private static String firstNonBlank(String a, String b, String c) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        if (b != null && !b.isBlank()) {
            return b.trim();
        }
        return c == null ? "" : c;
    }

    /**
     * Paiement avec split : le client paie {@code amountMillimes}, la plateforme reçoit
     * {@code (int)(amountMillimes * commissionRate)} via {@code splitPayment}, le vendeur
     * le solde sur {@code sellerReceiverWalletId}.
     */
    public String initSplitPayment(
            String sellerReceiverWalletId,
            long amountMillimes,
            String description,
            String orderId
    ) throws IOException, InterruptedException {
        System.out.println("CLE UTILISEE: [" + apiKey + "]");

        if (sellerReceiverWalletId == null || sellerReceiverWalletId.isBlank()) {
            throw new IOException("Portefeuille vendeur (receiverWalletId) manquant.");
        }

        if (amountMillimes <= 0) {
            throw new IOException("Montant invalide pour Konnect.");
        }

        if (orderId == null || orderId.isBlank()) {
            throw new IOException("orderId Konnect obligatoire.");
        }

        int platformSplit = (int) Math.floor(amountMillimes * commissionRate);
        platformSplit = Math.max(0, Math.min((int) amountMillimes, platformSplit));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("receiverWalletId", sellerReceiverWalletId.trim());
        body.put("token", "TND");
        body.put("amount", amountMillimes);
        body.put("type", "immediate");
        body.put(
                "description",
                description == null || description.isBlank() ? "Commande" : description.trim());
        body.put("acceptedPaymentMethods", List.of("bank_card"));
        body.put("successUrl", successUrl);
        body.put("failUrl", failUrl);
        body.put("orderId", orderId);
        body.put("lifespan", 10);
        body.put("addPaymentFeesToAmount", false);
        body.put("silentWebhook", true);

        if (!webhookUrl.isBlank()) {
            body.put("webhook", webhookUrl);
        }

        List<Map<String, Object>> splitPayment = new ArrayList<>();
        Map<String, Object> platformEntry = new LinkedHashMap<>();
        platformEntry.put("walletId", platformWalletId.trim());
        platformEntry.put("amount", platformSplit);
        splitPayment.add(platformEntry);
        body.put("splitPayment", splitPayment);

        String json = gson.toJson(body);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(INIT_URL))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json")
                        .header("x-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("STATUS: " + response.statusCode());
        System.out.println("BODY: " + response.body());
        System.out.println("URL UTILISEE: " + INIT_URL);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(
                    "Konnect HTTP " + response.statusCode() + " : " + response.body());
        }

        String payUrl = extractPayUrl(response.body());
        if (payUrl == null || payUrl.isBlank()) {
            throw new IOException(
                    "Réponse Konnect sans payUrl exploitable : " + response.body());
        }
        return payUrl;
    }

    private String extractPayUrl(String responseBody) {
        try {
            JsonObject root = gson.fromJson(responseBody, JsonObject.class);
            if (root == null) {
                return null;
            }
            String direct = getString(root, "payUrl");
            if (!direct.isEmpty()) {
                return direct;
            }
            direct = getString(root, "paymentUrl");
            if (!direct.isEmpty()) {
                return direct;
            }
            JsonObject data = root.getAsJsonObject("data");
            if (data != null) {
                String u = getString(data, "payUrl");
                if (!u.isEmpty()) {
                    return u;
                }
                u = getString(data, "paymentUrl");
                if (!u.isEmpty()) {
                    return u;
                }
                if (data.has("payment")) {
                    JsonElement payEl = data.get("payment");
                    if (payEl != null && payEl.isJsonObject()) {
                        JsonObject pay = payEl.getAsJsonObject();
                        u = getString(pay, "payUrl");
                        if (!u.isEmpty()) {
                            return u;
                        }
                        u = getString(pay, "link");
                        if (!u.isEmpty()) {
                            return u;
                        }
                    }
                }
            }
            JsonObject nested = root.getAsJsonObject("result");
            if (nested != null) {
                String u = getString(nested, "payUrl");
                if (!u.isEmpty()) {
                    return u;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getString(JsonObject o, String field) {
        if (o == null || !o.has(field) || o.get(field).isJsonNull()) {
            return "";
        }
        JsonElement e = o.get(field);
        if (e.isJsonPrimitive()) {
            return e.getAsString().trim();
        }
        return "";
    }
}
