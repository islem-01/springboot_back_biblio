package com.biblio.controller;

import com.biblio.model.Emprunt;
import com.biblio.model.Livre;
import com.biblio.model.Utilisateur;
import com.biblio.repository.EmpruntRepository;
import com.biblio.repository.LivreRepository;
import com.biblio.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * EmpruntController – API REST pour les emprunts.
 *
 * ADMIN :
 *   GET    /api/admin/emprunts             → tous les emprunts
 *   GET    /api/admin/emprunts/en-cours    → emprunts en cours
 *   POST   /api/admin/emprunts             → affecter un livre
 *   PUT    /api/admin/emprunts/{id}/retour → retourner un livre
 *
 * USER :
 *   GET  /api/user/emprunts          → mes emprunts
 *   GET  /api/user/emprunts/en-cours → mes emprunts en cours
 *   POST /api/user/emprunts          → emprunter un livre
 */
@RestController
public class EmpruntController {

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EmpruntController(EmpruntRepository empruntRepository,
                             LivreRepository livreRepository,
                             UtilisateurRepository utilisateurRepository) {
        this.empruntRepository   = empruntRepository;
        this.livreRepository     = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ─────────────────────────────────────────────────────────────── ADMIN ──

    @GetMapping("/api/admin/emprunts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Emprunt> listerTous(@RequestParam(required = false) String filtre) {
        if ("enCours".equals(filtre)) return empruntRepository.findEnCours();
        return empruntRepository.findAllWithDetails();
    }

    @GetMapping("/api/admin/emprunts/en-cours")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Emprunt> enCours() {
        return empruntRepository.findEnCours();
    }

    /**
     * POST /api/admin/emprunts
     * Affecter manuellement un livre à un utilisateur (admin).
     * Body JSON : { "utilisateurId", "livreId", "dateDebut", "dateFin" }
     */
    @PostMapping("/api/admin/emprunts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> affecter(@RequestBody Map<String, String> body) {
        try {
            int utilisateurId = Integer.parseInt(body.get("utilisateurId"));
            int livreId       = Integer.parseInt(body.get("livreId"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateDebut = sdf.parse(body.get("dateDebut"));
            Date dateFin   = sdf.parse(body.get("dateFin"));

            return ResponseEntity.ok(creerEmprunt(utilisateurId, livreId, dateDebut, dateFin));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/emprunts/{id}/retour
     * Marquer un livre comme retourné (restitue le stock).
     */
    @PutMapping("/api/admin/emprunts/{id}/retour")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> retourner(@PathVariable int id) {
        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Emprunt introuvable"));

        if (emprunt.isRetourne()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Déjà retourné"));
        }

        emprunt.setRetourne(true);
        empruntRepository.save(emprunt);

        Livre livre = emprunt.getLivre();
        livre.setQuantiteStock(livre.getQuantiteStock() + 1);
        livreRepository.save(livre);

        return ResponseEntity.ok(emprunt);
    }

    // ─────────────────────────────────────────────────────────────── USER ──

    /**
     * GET /api/user/emprunts
     * Retourne tous les emprunts de l'utilisateur connecté.
     */
    @GetMapping("/api/user/emprunts")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Emprunt> mesEmprunts(@AuthenticationPrincipal Object principal) {
        Utilisateur user = (Utilisateur) principal;
        return empruntRepository.findByUtilisateurId(user.getId());
    }

    /**
     * GET /api/user/emprunts/en-cours
     */
    @GetMapping("/api/user/emprunts/en-cours")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Emprunt> mesEmpruntsEnCours(@AuthenticationPrincipal Object principal) {
        Utilisateur user = (Utilisateur) principal;
        return empruntRepository.findEnCoursByUtilisateurId(user.getId());
    }

    /**
     * POST /api/user/emprunts
     * L'utilisateur connecté emprunte un livre.
     * Body JSON : { "livreId", "dateDebut", "dateFin" }
     */
    @PostMapping("/api/user/emprunts")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> emprunter(@RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal Object principal) {
        try {
            Utilisateur user = (Utilisateur) principal;
            int livreId      = Integer.parseInt(body.get("livreId"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateDebut = sdf.parse(body.get("dateDebut"));
            Date dateFin   = sdf.parse(body.get("dateFin"));

            return ResponseEntity.ok(creerEmprunt(user.getId(), livreId, dateDebut, dateFin));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────── Méthode partagée ──

    /**
     * Crée un emprunt de façon atomique : vérifie le stock et le réduit.
     */
    private Emprunt creerEmprunt(int utilisateurId, int livreId,
                                  Date dateDebut, Date dateFin) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Livre l = livreRepository.findById(livreId)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));

        if (l.getQuantiteStock() <= 0) {
            throw new IllegalStateException("Stock insuffisant pour : " + l.getTitre());
        }

        l.setQuantiteStock(l.getQuantiteStock() - 1);
        livreRepository.save(l);

        Emprunt e = new Emprunt(u, l, dateDebut, dateFin);
        return empruntRepository.save(e);
    }
}
