package com.flatmate.app.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes the Firebase Admin SDK on startup.
 *
 * Provide the service-account JSON file in one of two ways:
 *   1. Environment variable:  FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/serviceAccount.json
 *   2. Classpath resource:    src/main/resources/firebase-service-account.json
 *
 * Get your serviceAccount.json from:
 *   Firebase Console → Project Settings → Service accounts → Generate new private key
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized — skipping.");
            return;
        }

        try {
            InputStream serviceAccount = resolveServiceAccount();
            if (serviceAccount == null) {
                log.warn("Firebase service account not found. Push notifications will be mocked. " +
                         "Set FIREBASE_SERVICE_ACCOUNT_PATH or place firebase-service-account.json in resources/.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully.");

        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    private InputStream resolveServiceAccount() throws IOException {
        // 1. Check explicit path from environment / application.properties
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            log.info("Loading Firebase credentials from path: {}", serviceAccountPath);
            return new FileInputStream(serviceAccountPath);
        }

        // 2. Fall back to classpath resource
        InputStream classpathResource = getClass()
                .getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        if (classpathResource != null) {
            log.info("Loading Firebase credentials from classpath: firebase-service-account.json");
        }
        return classpathResource;
    }
}
