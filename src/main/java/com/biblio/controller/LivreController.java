package com.biblio.controller;

import com.biblio.model.Categorie;
import com.biblio.model.Livre;
import com.biblio.repository.CategorieRepository;
import com.biblio.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * LivreController – supporte 3 modes d'image :
 *   1. Fichier local uploadé (imageFile)
 *   2. URL externe (imageUrl)
 *   3. Aucune image
 */
@RestController
public class LivreController {

    private final LivreRepository livreRepository;
    private final CategorieRepository categorieRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public LivreController(LivreRepository livreRepository,
                           CategorieRepository categorieRepository) {
        this.livreRepository    = livreRepository;
        this.categorieRepository = categorieRepository;
    }

    // ── PUBLICS ──────────────────────────────────────────────────────────────

    @GetMapping("/api/livres")
    public List<Livre> listerTous() {
        return livreRepository.findAllByOrderByTitreAsc();
    }

    @GetMapping("/api/livres/{id}")
    public ResponseEntity<Livre> trouverParId(@PathVariable int id) {
        return livreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/livres/recherche")
    public List<Livre> rechercher(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) Integer cid) {
        if (q != null && !q.isBlank() && cid != null)
            return livreRepository.rechercherCombine("%" + q.toLowerCase() + "%", cid);
        if (q != null && !q.isBlank())
            return livreRepository.rechercherParMotCle("%" + q.toLowerCase() + "%");
        if (cid != null)
            return livreRepository.findByCategorieIdOrderByTitreAsc(cid);
        return livreRepository.findAllByOrderByTitreAsc();
    }

    @GetMapping("/api/livres/categorie/{categorieId}")
    public List<Livre> parCategorie(@PathVariable int categorieId) {
        return livreRepository.findByCategorieIdOrderByTitreAsc(categorieId);
    }

    // ── ADMIN ─────────────────────────────────────────────────────────────────

    @PostMapping("/api/admin/livres")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ajouter(
            @RequestParam String titre,
            @RequestParam String auteur,
            @RequestParam int anneePublication,
            @RequestParam int quantiteStock,
            @RequestParam(required = false) String description,
            @RequestParam int categorieId,
            @RequestParam(required = false) MultipartFile imageFile,  // upload local
            @RequestParam(required = false) String imageUrl)          // URL externe
            throws IOException {

        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));

        // Priorité : fichier uploadé > URL externe
        String cheminImage = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            cheminImage = sauvegarderFichier(imageFile);
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            cheminImage = imageUrl.trim(); // stocker l'URL directement
        }

        Livre l = new Livre(titre, auteur, cheminImage,
                anneePublication, quantiteStock, description, categorie);
        return ResponseEntity.ok(livreRepository.save(l));
    }

    @PutMapping("/api/admin/livres/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modifier(
            @PathVariable int id,
            @RequestParam String titre,
            @RequestParam String auteur,
            @RequestParam int anneePublication,
            @RequestParam int quantiteStock,
            @RequestParam(required = false) String description,
            @RequestParam int categorieId,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String imageUrl)
            throws IOException {

        Livre l = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));

        l.setTitre(titre);
        l.setAuteur(auteur);
        l.setAnneePublication(anneePublication);
        l.setQuantiteStock(quantiteStock);
        l.setDescription(description);
        l.setCategorie(categorie);

        // Mise à jour image : fichier uploadé en priorité, sinon URL, sinon garder l'ancienne
        if (imageFile != null && !imageFile.isEmpty()) {
            supprimerFichierLocal(l.getImage()); // supprimer l'ancien fichier local si existe
            l.setImage(sauvegarderFichier(imageFile));
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            supprimerFichierLocal(l.getImage());
            l.setImage(imageUrl.trim());
        }

        return ResponseEntity.ok(livreRepository.save(l));
    }

    @DeleteMapping("/api/admin/livres/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> supprimer(@PathVariable int id) {
        Livre l = livreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));
        supprimerFichierLocal(l.getImage());
        livreRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    /** Sauvegarde un fichier uploadé → retourne le chemin relatif */
    private String sauvegarderFichier(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        if (original == null) return null;

        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ext.matches("jpg|jpeg|png|gif|webp"))
            throw new IllegalArgumentException("Format non supporté : " + ext);

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String nom = UUID.randomUUID() + "." + ext;
        Files.copy(file.getInputStream(), dir.resolve(nom));

        return "uploads/" + nom; // chemin relatif servi par Spring
    }

    /** Supprime un fichier local (ignore les URLs externes) */
    private void supprimerFichierLocal(String chemin) {
        if (chemin == null || chemin.startsWith("http")) return;
        try { Files.deleteIfExists(Paths.get(chemin)); } catch (IOException ignored) {}
    }
}
