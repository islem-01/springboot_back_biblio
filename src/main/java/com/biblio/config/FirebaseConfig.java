package com.biblio.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * Initialise le SDK Firebase Admin au démarrage.
 * Le fichier JSON de compte de service est placé dans src/main/resources/
 * et référencé par app.firebase.service-account dans application.properties.
 *
 * Pour l'obtenir :
 *   Console Firebase > Paramètres du projet > Comptes de service >
 *   Générer une nouvelle clé privée
 */
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.service-account}")
    private Resource serviceAccount;

    @PostConstruct
    public void initFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
