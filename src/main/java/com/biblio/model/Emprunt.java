package com.biblio.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "emprunts")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    @Column(name = "date_debut", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateDebut;

    @Column(name = "date_fin", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateFin;

    @Column(nullable = false)
    private boolean retourne = false;

    // ─── Constructeurs ────────────────────────────────────────────────────────
    public Emprunt() {}

    public Emprunt(Utilisateur utilisateur, Livre livre, Date dateDebut, Date dateFin) {
        this.utilisateur = utilisateur;
        this.livre = livre;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.retourne = false;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public boolean isRetourne() { return retourne; }
    public void setRetourne(boolean retourne) { this.retourne = retourne; }
}
