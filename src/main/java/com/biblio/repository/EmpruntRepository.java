package com.biblio.repository;

import com.biblio.model.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Integer> {

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre " +
           "ORDER BY e.dateDebut DESC")
    List<Emprunt> findAllWithDetails();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.livre " +
           "WHERE e.utilisateur.id = :uid ORDER BY e.dateDebut DESC")
    List<Emprunt> findByUtilisateurId(@Param("uid") int utilisateurId);

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre " +
           "WHERE e.retourne = false ORDER BY e.dateFin ASC")
    List<Emprunt> findEnCours();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.livre " +
           "WHERE e.utilisateur.id = :uid AND e.retourne = false")
    List<Emprunt> findEnCoursByUtilisateurId(@Param("uid") int utilisateurId);

    @Query("SELECT COUNT(e) FROM Emprunt e WHERE e.retourne = false")
    long countEnCours();

    @Query("SELECT e FROM Emprunt e JOIN FETCH e.utilisateur JOIN FETCH e.livre " +
           "ORDER BY e.dateDebut DESC")
    List<Emprunt> findDerniers(org.springframework.data.domain.Pageable pageable);
}
