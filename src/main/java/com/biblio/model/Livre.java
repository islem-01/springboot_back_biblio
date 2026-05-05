package com.biblio.model;

import jakarta.persistence.*;

@Entity
@Table(name = "livres")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String auteur;

    private String image;

    @Column(name = "annee_publication")
    private int anneePublication;

    @Column(name = "quantite_stock", nullable = false)
    private int quantiteStock;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    // ─── Constructeurs ────────────────────────────────────────────────────────
    public Livre() {}

    public Livre(String titre, String auteur, String image,
                 int anneePublication, int quantiteStock,
                 String description, Categorie categorie) {
        this.titre = titre;
        this.auteur = auteur;
        this.image = image;
        this.anneePublication = anneePublication;
        this.quantiteStock = quantiteStock;
        this.description = description;
        this.categorie = categorie;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getAnneePublication() { return anneePublication; }
    public void setAnneePublication(int anneePublication) { this.anneePublication = anneePublication; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
}
