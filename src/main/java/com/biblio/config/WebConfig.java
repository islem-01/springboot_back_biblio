package com.biblio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * WebConfig – expose le dossier d'uploads comme ressource statique.
 *
 * Les images des couvertures de livres sont accessibles via :
 *   GET http://localhost:8080/uploads/<nom_fichier.jpg>
 *
 * Angular peut donc afficher les images avec :
 *   <img [src]="'http://localhost:8080/' + livre.image" />
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadAbsolute = uploadPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolute);
    }
}
