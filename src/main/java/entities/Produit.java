package entities;

import java.time.LocalDate;

public class Produit {
    private int id_produit;
    private String nom_produit;
    private String description;
    private float prix;
    private int stock;
    private LocalDate date_ajout;
    private int userId;
    private String imagePath;
    /** Référence optionnelle à {@code categories.id_categorie} (0 si non renseigné). */
    private int categorieId;
    /** Valeurs typiques : {@code en_attente}, {@code approuvé}, {@code refusé}. */
    private String statut;
    /** Portefeuille Konnect du vendeur (receiverWalletId) pour les paiements en ligne. */
    private String sellerWalletId;

    public Produit(int id_produit, String nom_produit, String description, float prix, int stock, LocalDate date_ajout) {
        this.id_produit = id_produit;
        this.nom_produit = nom_produit;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.date_ajout = date_ajout;
    }

    public int getId_produit() { return id_produit; }
    public String getNom() { return nom_produit; }
    public String getDescription() { return description; }
    public float getPrix() { return prix; }
    public int getStock() { return stock; }
    public LocalDate getDate_ajout() { return date_ajout; }
    public int getUserId() { return userId; }
    public String getImagePath() { return imagePath; }
    public int getCategorieId() { return categorieId; }
    public String getStatut() { return statut != null ? statut : ""; }
    public String getSellerWalletId() { return sellerWalletId != null ? sellerWalletId : ""; }

    public void setId_produit(int id_produit) { this.id_produit = id_produit; }
    public void setNom(String nom) { this.nom_produit = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setPrix(float prix) { this.prix = prix; }
    public void setStock(int stock) { this.stock = stock; }
    public void setDate_ajout(LocalDate date_ajout) { this.date_ajout = date_ajout; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setSellerWalletId(String sellerWalletId) { this.sellerWalletId = sellerWalletId; }

    @Override
    public String toString() {
        return "Produit{" +
                "id_produit=" + id_produit +
                ", Nom='" + nom_produit + '\'' +
                ", Description='" + description + '\'' +
                ", prix='" + prix + '\'' +
                ", stock='" + stock + '\'' +
                ", date d'ajout='" + date_ajout + '\'' + "}";
    }
}