package com.biblio.controller;

import com.biblio.model.Utilisateur;
import com.biblio.repository.UtilisateurRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;

    public AuthController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * POST /api/auth/sync
     * Synchronise Firebase → MySQL. Tous les nouveaux comptes sont USER par défaut.
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank())
            return ResponseEntity.badRequest().body(Map.of("erreur", "idToken manquant"));

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid   = decoded.getUid();
            String email = decoded.getEmail();
            String name  = decoded.getName() != null ? decoded.getName() : "";

            Optional<Utilisateur> optUser = utilisateurRepository.findByFirebaseUid(uid);
            if (optUser.isEmpty()) optUser = utilisateurRepository.findByEmail(email);

            Utilisateur user;
            boolean isNew = false;

            if (optUser.isPresent()) {
                user = optUser.get();
                if (user.getFirebaseUid() == null) {
                    user.setFirebaseUid(uid);
                    utilisateurRepository.save(user);
                }
            } else {
                String[] parts = name.split(" ", 2);
                user = new Utilisateur(
                        parts.length > 1 ? parts[1] : "",
                        parts.length > 0 ? parts[0] : "",
                        email, "", "USER", null, null);
                user.setFirebaseUid(uid);
                utilisateurRepository.save(user);
                isNew = true;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("id",      user.getId());
            res.put("nom",     user.getNom());
            res.put("prenom",  user.getPrenom());
            res.put("email",   user.getEmail());
            res.put("role",    user.getRole());
            res.put("nouveau", isNew);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("erreur", "Token Firebase invalide : " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/register — Inscription classique
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String nom    = body.getOrDefault("nom", "").trim();
        String prenom = body.getOrDefault("prenom", "").trim();
        String email  = body.getOrDefault("email", "").trim();
        String mdp    = body.getOrDefault("motDePasse", "").trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erreur", "Champs obligatoires manquants."));

        if (utilisateurRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(Map.of("erreur", "Email déjà utilisé."));

        Utilisateur u = new Utilisateur(nom, prenom, email, mdp, "USER",
                body.get("numeroTelephone"), body.get("adresse"));
        utilisateurRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Inscription réussie."));
    }

    /**
     * POST /api/auth/make-admin  ← NOUVEAU
     * Réservé aux ADMINs existants pour promouvoir un utilisateur.
     * Body : { "email": "user@exemple.com" }
     */
    @PostMapping("/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> makeAdmin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("erreur", "Email requis"));

        return utilisateurRepository.findByEmail(email.trim())
                .map(u -> {
                    u.setRole("ADMIN");
                    utilisateurRepository.save(u);
                    return ResponseEntity.ok(Map.of(
                            "message", u.getPrenom() + " " + u.getNom() + " est maintenant ADMIN"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/auth/me — Utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Object principal) {
        if (principal instanceof Utilisateur user) {
            Map<String, Object> res = new HashMap<>();
            res.put("id",     user.getId());
            res.put("nom",    user.getNom());
            res.put("prenom", user.getPrenom());
            res.put("email",  user.getEmail());
            res.put("role",   user.getRole());
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(401).body(Map.of("erreur", "Non authentifié"));
    }
}
