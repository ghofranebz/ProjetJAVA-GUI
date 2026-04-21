package entities;

import java.time.LocalDate;

public class Produit {
    private int id_produit;
    private String nom_produit;
    private String description;
    private float prix;
    private int stock;
    private LocalDate date_ajout;

    public Produit(int id_produit, String nom_produit, String description, float prix, int stock, LocalDate date_ajout) {
        this.id_produit = id_produit;
        this.nom_produit = nom_produit;
        this.description = description;
        this.prix = 0;
        this.stock = stock;
        this.date_ajout = LocalDate.now();;
    }


    public int getId_produit() {
        return id_produit;
    }

    public String getNom() {
        return nom_produit;
    }

    public String getDescription() {
        return description;
    }

    public float getPrix() {
        return prix;
    }

    public int getStock() {
        return stock;
    }

    public LocalDate getDate_ajout() {
        return date_ajout;
    }

    public void setId_produit(int id_produit) {
        this.id_produit = id_produit;
    }
    public void setNom(String nom) {
        this.nom_produit = nom_produit;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setDate_ajout(LocalDate date_ajout) {
        this.date_ajout = date_ajout;
    }
    @Override
    public String toString() {
        return "Produit{" +
                "id_produit=" + id_produit +
                ", Nom='" + nom_produit + '\'' +
                ", Description='" + description + '\'' +
                ", prix='" + prix + '\'' +
                ", stock='" + stock + '\'' +
                ", date d'ajout='" + date_ajout + '\''+ "}";
    }


}