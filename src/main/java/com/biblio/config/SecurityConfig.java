package com.biblio.config;

import com.biblio.repository.UtilisateurRepository;
import com.biblio.security.FirebaseAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration Spring Security :
 *  - Stateless (pas de session HTTP → compatible API REST + Angular)
 *  - Authentification via Firebase ID Token (Bearer)
 *  - CORS ouvert pour Angular sur localhost:4200
 *  - Routes publiques : GET livres/catégories + endpoint auth/sync
 *  - Routes ADMIN : préfixe /api/admin/**
 *  - Routes USER : /api/user/**
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UtilisateurRepository utilisateurRepository)
            throws Exception {

        http
            // Désactiver CSRF (pas de session, API REST)
            .csrf(csrf -> csrf.disable())

            // CORS configuré ci-dessous
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Pas de session HTTP – chaque requête porte son token Firebase
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Règles d'autorisation
            .authorizeHttpRequests(auth -> auth

                // ── Endpoints publics ─────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/livres/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/sync").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                // Accès aux images statiques
                .requestMatchers("/uploads/**").permitAll()

                // ── Endpoints ADMIN ───────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Endpoints USER (authentifié) ──────────────────────────────
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                // ── Dashboard stats (admin) ───────────────────────────────────
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                // Tout le reste nécessite d'être authentifié
                .anyRequest().authenticated()
            )

            // Ajouter le filtre Firebase avant le filtre d'authentification standard
            .addFilterBefore(
                    new FirebaseAuthFilter(utilisateurRepository),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS pour autoriser Angular (localhost:4200)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
