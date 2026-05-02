package entities;

public class PanierItem {
    private int userId;
    private int produitId;
    private int quantite;

    public PanierItem(int userId, int produitId, int quantite) {
        this.userId = userId;
        this.produitId = produitId;
        this.quantite = quantite;
    }

    public int getUserId() { return userId; }
    public int getProduitId() { return produitId; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}