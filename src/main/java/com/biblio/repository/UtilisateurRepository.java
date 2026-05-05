package com.biblio.repository;

import com.biblio.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRoleOrderByNomAsc(String role);

    List<Utilisateur> findAllByOrderByNomAsc();

    long count();
}
