package com.biblio.repository;

import com.biblio.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
    Optional<Categorie> findByLabel(String label);
    List<Categorie> findAllByOrderByLabelAsc();
    boolean existsByLabel(String label);
}
