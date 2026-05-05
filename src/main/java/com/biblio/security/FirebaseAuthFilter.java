package com.biblio.security;

import com.biblio.model.Utilisateur;
import com.biblio.repository.UtilisateurRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filtre Spring Security qui intercepte chaque requête HTTP.
 *
 * Angular envoie l'ID Token Firebase dans l'en-tête :
 *   Authorization: Bearer <firebase_id_token>
 *
 * Ce filtre :
 *   1. Extrait le token Bearer
 *   2. Le vérifie via Firebase Admin SDK
 *   3. Cherche l'utilisateur correspondant en base MySQL (par firebase_uid ou email)
 *   4. Injecte l'authentification Spring Security dans le contexte
 */
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UtilisateurRepository utilisateurRepository;

    public FirebaseAuthFilter(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authHeader.substring(7);

        try {
            // Vérification du token Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid   = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Chercher l'utilisateur en base (par uid d'abord, puis par email)
            Optional<Utilisateur> optUser = utilisateurRepository.findByFirebaseUid(uid);

            if (optUser.isEmpty()) {
                optUser = utilisateurRepository.findByEmail(email);
                // Lier le firebase_uid à l'utilisateur existant
                if (optUser.isPresent()) {
                    Utilisateur u = optUser.get();
                    u.setFirebaseUid(uid);
                    utilisateurRepository.save(u);
                }
            }

            if (optUser.isPresent()) {
                Utilisateur user = optUser.get();
                String role = "ROLE_" + user.getRole(); // → ROLE_ADMIN ou ROLE_USER

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority(role)));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Token invalide ou expiré → on laisse passer sans authentification
            // Spring Security refusera l'accès aux routes protégées
            logger.warn("Firebase token invalide : " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
