package com.biblio.controller;

import com.biblio.model.Categorie;
import com.biblio.repository.CategorieRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategorieController – API REST pour les catégories.
 *
 * GET    /api/categories         → liste (public)
 * GET    /api/categories/{id}    → détail (public)
 * POST   /api/admin/categories   → créer  (ADMIN)
 * PUT    /api/admin/categories/{id} → modifier (ADMIN)
 * DELETE /api/admin/categories/{id} → supprimer (ADMIN)
 */
@RestController
public class CategorieController {

    private final CategorieRepository categorieRepository;

    public CategorieController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    // ────────────────────────────────────────────────────────────── PUBLICS ──

    @GetMapping("/api/categories")
    public List<Categorie> listerTous() {
        return categorieRepository.findAllByOrderByLabelAsc();
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<Categorie> trouverParId(@PathVariable int id) {
        return categorieRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────── ADMIN ──

    @PostMapping("/api/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ajouter(@RequestBody Categorie categorie) {
        if (categorieRepository.existsByLabel(categorie.getLabel())) {
            return ResponseEntity.badRequest()
                    .body("Une catégorie avec ce libellé existe déjà.");
        }
        return ResponseEntity.ok(categorieRepository.save(categorie));
    }

    @PutMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modifier(@PathVariable int id,
                                       @RequestBody Categorie donnees) {
        return categorieRepository.findById(id).map(c -> {
            c.setLabel(donnees.getLabel());
            c.setDescription(donnees.getDescription());
            return ResponseEntity.ok(categorieRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> supprimer(@PathVariable int id) {
        if (!categorieRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categorieRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
