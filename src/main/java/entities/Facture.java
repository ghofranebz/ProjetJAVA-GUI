package entities;

import java.time.LocalDate;

public class Facture {

    private int id_facture;
    private int commande_id;
    private LocalDate date_facture;
    private float montant_total;

    private String nom_receiver;
    private String email_receiver;
    private String telephone_receiver;
    private String adresse_receiver;

    // ✔ constructeur COMPLET
    public Facture(
            int id_facture,
            int commande_id,
            LocalDate date_facture,
            float montant_total,
            String nom_receiver,
            String email_receiver,
            String telephone_receiver,
            String adresse_receiver
    ) {
        this.id_facture = id_facture;
        this.commande_id = commande_id;
        this.date_facture = date_facture;
        this.montant_total = montant_total;
        this.nom_receiver = nom_receiver;
        this.email_receiver = email_receiver;
        this.telephone_receiver = telephone_receiver;
        this.adresse_receiver = adresse_receiver;
    }

    // ✔ getters existants
    public int getId_facture() {
        return id_facture;
    }

    public int getCommande_id() {
        return commande_id;
    }

    public LocalDate getDate_facture() {
        return date_facture;
    }

    public float getMontant_total() {
        return montant_total;
    }

    // ✔ nouveaux getters
    public String getNom_receiver() {
        return nom_receiver;
    }

    public String getEmail_receiver() {
        return email_receiver;
    }

    public String getTelephone_receiver() {
        return telephone_receiver;
    }

    public String getAdresse_receiver() {
        return adresse_receiver;
    }
}