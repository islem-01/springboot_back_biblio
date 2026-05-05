package com.biblio.repository;

import com.biblio.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Integer> {

    List<Livre> findAllByOrderByTitreAsc();

    List<Livre> findByCategorieIdOrderByTitreAsc(int categorieId);

    @Query("SELECT l FROM Livre l JOIN FETCH l.categorie " +
           "WHERE LOWER(l.titre) LIKE :mc OR LOWER(l.auteur) LIKE :mc " +
           "ORDER BY l.titre")
    List<Livre> rechercherParMotCle(@Param("mc") String motCle);

    @Query("SELECT l FROM Livre l JOIN FETCH l.categorie " +
           "WHERE (LOWER(l.titre) LIKE :mc OR LOWER(l.auteur) LIKE :mc) " +
           "AND l.categorie.id = :cid ORDER BY l.titre")
    List<Livre> rechercherCombine(@Param("mc") String motCle, @Param("cid") int categorieId);

    long count();
}
