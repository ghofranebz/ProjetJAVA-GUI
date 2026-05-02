package entities;

public class Categorie {
    private int id_categorie;
    private String nom;
    private String description;

    public Categorie(int id_categorie, String nom, String description) {
        this.id_categorie = id_categorie;
        this.nom = nom;
        this.description = description;
    }

    public int getId_categorie() { return id_categorie; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public void setId_categorie(int id) { this.id_categorie = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
}