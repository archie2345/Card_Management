package com.card.Card_management.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  @Value("${firebase.encrypted-credentials-file:}")
  private String encryptedServiceAccountPath;

  @Value("${firebase.project-id:}")
  private String projectId;

  @Value("${card.kms.key-name:}")
  private String kmsKeyName;

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
    if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
      try (InputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
        return GoogleCredentials.fromStream(serviceAccount);
      }
    }

    if (encryptedServiceAccountPath != null && !encryptedServiceAccountPath.isBlank()) {
      return loadFromEncryptedFile();
    }

    log.warn(
        "Neither firebase.credentials-file nor firebase.encrypted-credentials-file are configured; "
            + "attempting to use default application credentials");
    return GoogleCredentials.getApplicationDefault();
  }

  private GoogleCredentials loadFromEncryptedFile() throws IOException {
    if (kmsKeyName == null || kmsKeyName.isBlank()) {
      throw new IllegalStateException(
          "card.kms.key-name must be configured when firebase.encrypted-credentials-file is set");
    }

    Path encryptedPath = Path.of(encryptedServiceAccountPath);
    if (!Files.exists(encryptedPath)) {
      throw new IllegalStateException(
          "Encrypted Firebase credentials file not found at: " + encryptedServiceAccountPath);
    }

    byte[] ciphertextFileBytes = Files.readAllBytes(encryptedPath);
    if (ciphertextFileBytes.length == 0) {
      throw new IllegalStateException(
          "Encrypted Firebase credentials file is empty at: " + encryptedServiceAccountPath);
    }

    byte[] ciphertext = tryDecodeBase64(ciphertextFileBytes);
    CryptoKeyName cryptoKeyName = CryptoKeyName.parse(kmsKeyName);
    log.info("Decrypting Firebase credentials with KMS key {}", cryptoKeyName);

    try (KeyManagementServiceClient kmsClient = KeyManagementServiceClient.create()) {
      DecryptResponse response =
          kmsClient.decrypt(cryptoKeyName, ByteString.copyFrom(ciphertext));
      byte[] plaintext = response.getPlaintext().toByteArray();
      if (plaintext.length == 0) {
        throw new IllegalStateException("KMS returned empty plaintext for Firebase credentials");
      }
      return GoogleCredentials.fromStream(new ByteArrayInputStream(plaintext));
    } catch (IllegalArgumentException ex) {
      throw new IllegalStateException("Invalid KMS key name: " + kmsKeyName, ex);
    }
  }

  private byte[] tryDecodeBase64(byte[] content) {
    String ascii = new String(content, StandardCharsets.US_ASCII);
    String compact = ascii.replaceAll("\\s+", "");

    if (!compact.isEmpty()) {
      try {
        return Base64.getDecoder().decode(compact);
      } catch (IllegalArgumentException ex) {
        log.debug("Primary Base64 decode failed, attempting to extract ciphertext from structured output");
      }
    }

    Pattern base64Pattern = Pattern.compile("([A-Za-z0-9+/=]{16,})");
    Matcher matcher = base64Pattern.matcher(ascii);
    while (matcher.find()) {
      String candidate = matcher.group(1);
      try {
        return Base64.getDecoder().decode(candidate);
      } catch (IllegalArgumentException ignored) {
        // try next candidate
      }
    }

    log.info("Credentials ciphertext is not valid Base64 text; treating file as raw binary");
    return content;
  }
}
