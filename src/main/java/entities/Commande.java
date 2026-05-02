package entities;

import java.time.LocalDate;

public class Commande {
    private int id_commande;
    private int userId;
    private int panierId;
    private LocalDate date_commande;
    private float montant_total;
    private String mode_paiement;
    private String statut;

    public Commande(int id_commande, int userId, int panierId, LocalDate date_commande,
                    float montant_total, String mode_paiement, String statut) {
        this.id_commande = id_commande;
        this.userId = userId;
        this.panierId = panierId;
        this.date_commande = date_commande;
        this.montant_total = montant_total;
        this.mode_paiement = mode_paiement;
        this.statut = statut;
    }

    public int getId_commande() { return id_commande; }
    public int getUserId() { return userId; }
    public int getPanierId() { return panierId; }
    public LocalDate getDate_commande() { return date_commande; }
    public float getMontant_total() { return montant_total; }
    public String getMode_paiement() { return mode_paiement; }
    public String getStatut() { return statut; }
    public void setId_commande(int id) { this.id_commande = id; }
    public void setStatut(String statut) { this.statut = statut; }
}
