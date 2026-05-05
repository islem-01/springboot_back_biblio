package com.biblio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    @JsonIgnore   // ne jamais exposer le mot de passe dans les réponses API
    private String motDePasse;

    /** Rôle : "ADMIN" ou "USER" */
    @Column(nullable = false)
    private String role;

    @Column(name = "numero_telephone")
    private String numeroTelephone;

    private String adresse;

    /**
     * UID Firebase — stocké lors de la première connexion Firebase.
     * Permet de lier un compte Firebase à un utilisateur MySQL.
     */
    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    // ─── Constructeurs ────────────────────────────────────────────────────────
    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email,
                       String motDePasse, String role,
                       String numeroTelephone, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.numeroTelephone = numeroTelephone;
        this.adresse = adresse;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String tel) { this.numeroTelephone = tel; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
}
