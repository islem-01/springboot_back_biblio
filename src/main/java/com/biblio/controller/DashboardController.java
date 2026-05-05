package com.biblio.controller;

import com.biblio.model.Emprunt;
import com.biblio.repository.CategorieRepository;
import com.biblio.repository.EmpruntRepository;
import com.biblio.repository.LivreRepository;
import com.biblio.repository.UtilisateurRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardController – statistiques pour le tableau de bord admin.
 *
 * GET /api/dashboard → { nbLivres, nbUtilisateurs, nbCategories, nbEmpruntsEnCours, derniersEmprunts }
 */
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final EmpruntRepository empruntRepository;

    public DashboardController(LivreRepository livreRepository,
                               UtilisateurRepository utilisateurRepository,
                               CategorieRepository categorieRepository,
                               EmpruntRepository empruntRepository) {
        this.livreRepository       = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.categorieRepository   = categorieRepository;
        this.empruntRepository     = empruntRepository;
    }

    @GetMapping
    public Map<String, Object> stats() {
        List<Emprunt> derniers = empruntRepository
                .findDerniers(PageRequest.of(0, 5));

        Map<String, Object> stats = new HashMap<>();
        stats.put("nbLivres",           livreRepository.count());
        stats.put("nbUtilisateurs",     utilisateurRepository.count());
        stats.put("nbCategories",       categorieRepository.count());
        stats.put("nbEmpruntsEnCours",  empruntRepository.countEnCours());
        stats.put("derniersEmprunts",   derniers);

        return stats;
    }
}
