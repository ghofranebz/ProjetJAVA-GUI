package entities;

public class Favoris {
    private int userId;
    private int produitId;

    public Favoris(int userId, int produitId) {
        this.userId = userId;
        this.produitId = produitId;
    }

    public int getUserId() { return userId; }
    public int getProduitId() { return produitId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }
}