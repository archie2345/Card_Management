package com.card.Card_management.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that boots Firebase and exposes Firestore access.
 */
@Configuration
public class FirebaseConfig {

  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.credentials-file:}")
  private String serviceAccountPath;

  @Value("${firebase.project-id:}")
  private String projectId;

  /**
   * Initializes the shared {@link FirebaseApp} instance if required.
   *
   * @return initialized Firebase application handle
   */
  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseOptions.Builder builder =
          FirebaseOptions.builder().setCredentials(loadCredentials());
      if (projectId != null && !projectId.isBlank()) {
        builder.setProjectId(projectId);
      }
      FirebaseOptions options = builder.build();
      return FirebaseApp.initializeApp(options);
    }
    log.debug("FirebaseApp already initialized, reusing existing instance");
    return FirebaseApp.getInstance();
  }

  /**
   * Provides a Firestore client bound to the configured Firebase application.
   *
   * @param firebaseApp configured Firebase instance
   * @return Firestore client ready for use
   */
  @Bean
  public Firestore firestore(FirebaseApp firebaseApp) {
    return FirestoreClient.getFirestore(firebaseApp);
  }

  /**
   * Loads Google credentials either from the provided path or the default environment.
   */
  private GoogleCredentials loadCredentials() throws IOException {
    if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
      log.warn(
          "firebase.credentials-file property is not set; attempting to use default application "
              + "credentials");
      return GoogleCredentials.getApplicationDefault();
    }

    try (InputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
      return GoogleCredentials.fromStream(serviceAccount);
    }
  }
}
