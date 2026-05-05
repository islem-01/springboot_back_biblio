package com.biblio.controller;

import com.biblio.model.Utilisateur;
import com.biblio.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UtilisateurController – gestion des utilisateurs (ADMIN).
 *
 * GET    /api/admin/utilisateurs        → tous
 * GET    /api/admin/utilisateurs/users  → uniquement les USER
 * GET    /api/admin/utilisateurs/{id}   → un utilisateur
 * POST   /api/admin/utilisateurs        → créer
 * PUT    /api/admin/utilisateurs/{id}   → modifier
 * DELETE /api/admin/utilisateurs/{id}   → supprimer
 */
@RestController
@RequestMapping("/api/admin/utilisateurs")
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAllByOrderByNomAsc();
    }

    @GetMapping("/users")
    public List<Utilisateur> listerUsers() {
        return utilisateurRepository.findByRoleOrderByNomAsc("USER");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> trouverParId(@PathVariable int id) {
        return utilisateurRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> creer(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        if (utilisateurRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Cet email est déjà utilisé."));
        }
        Utilisateur u = new Utilisateur(
                body.get("nom"), body.get("prenom"), email,
                body.getOrDefault("motDePasse", ""),
                body.getOrDefault("role", "USER"),
                body.get("numeroTelephone"),
                body.get("adresse"));
        return ResponseEntity.ok(utilisateurRepository.save(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(@PathVariable int id,
                                       @RequestBody Map<String, String> body) {
        return utilisateurRepository.findById(id).map(u -> {
            if (body.containsKey("nom"))              u.setNom(body.get("nom"));
            if (body.containsKey("prenom"))           u.setPrenom(body.get("prenom"));
            if (body.containsKey("role"))             u.setRole(body.get("role"));
            if (body.containsKey("numeroTelephone"))  u.setNumeroTelephone(body.get("numeroTelephone"));
            if (body.containsKey("adresse"))          u.setAdresse(body.get("adresse"));
            if (body.containsKey("motDePasse") &&
                !body.get("motDePasse").isBlank())    u.setMotDePasse(body.get("motDePasse"));
            return ResponseEntity.ok(utilisateurRepository.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(@PathVariable int id) {
        if (!utilisateurRepository.existsById(id)) return ResponseEntity.notFound().build();
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
