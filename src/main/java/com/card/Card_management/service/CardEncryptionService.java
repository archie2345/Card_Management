package com.card.Card_management.service;

import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides encryption and decryption utilities for card PAN values, delegating to either KMS or
 * a local AES implementation based on configuration.
 */
@Service
public class CardEncryptionService {

  private final Encryptor delegate;

  /**
   * Creates a service that prefers KMS when a key name is supplied, otherwise falls back to local
   * AES.
   *
   * @param kmsKeyName fully qualified KMS key identifier
   * @param fallbackKey base64-encoded AES key used when KMS is unavailable
   */
  public CardEncryptionService(
      @Value("${card.kms.key-name:}") String kmsKeyName,
      @Value("${card.encryption.key:}") String fallbackKey) {
    if (kmsKeyName != null && !kmsKeyName.isBlank()) {
      this.delegate = new KmsEncryptor(kmsKeyName);
    } else {
      if (fallbackKey == null || fallbackKey.isBlank()) {
        throw new IllegalStateException(
            "Either card.kms.key-name or card.encryption.key must be configured");
      }
      this.delegate = new LocalAesEncryptor(fallbackKey);
    }
  }

  /**
   * Encrypts the provided PAN using the configured delegate.
   *
   * @param pan primary account number in plain text
   * @return base64-encoded ciphertext
   */
  public String encryptPan(String pan) {
    return delegate.encrypt(pan);
  }

  /**
   * Decrypts a previously encrypted PAN.
   *
   * @param encryptedPan ciphertext encoded in base64
   * @return decrypted primary account number
   */
  public String decryptPan(String encryptedPan) {
    return delegate.decrypt(encryptedPan);
  }

  /** Strategy abstraction to support multiple encryption implementations. */
  private interface Encryptor {
    String encrypt(String plaintext);

    String decrypt(String ciphertext);
  }

  /**
   * Encryptor backed by Google Cloud KMS.
   */
  private static final class KmsEncryptor implements Encryptor {
    private final String keyName;

    private KmsEncryptor(String keyName) {
      this.keyName = keyName;
    }

    @Override
    public String encrypt(String plaintext) {
      try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
        EncryptResponse response =
            client.encrypt(
                keyName, ByteString.copyFrom(plaintext, StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(response.getCiphertext().toByteArray());
      } catch (IOException e) {
        throw new IllegalStateException("Failed to initialise KMS client", e);
      } catch (RuntimeException e) {
        throw new IllegalStateException("Failed to encrypt PAN with KMS", e);
      }
    }

    @Override
    public String decrypt(String ciphertext) {
      try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
        DecryptResponse response =
            client.decrypt(
                keyName, ByteString.copyFrom(Base64.getDecoder().decode(ciphertext)));
        return response.getPlaintext().toStringUtf8();
      } catch (IOException e) {
        throw new IllegalStateException("Failed to initialise KMS client", e);
      } catch (RuntimeException e) {
        throw new IllegalStateException("Failed to decrypt PAN with KMS", e);
      }
    }
  }

  /**
   * Encryptor that performs authenticated encryption locally using AES/GCM.
   */
  private static final class LocalAesEncryptor implements Encryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    private LocalAesEncryptor(String keyBase64) {
      byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
      if (keyBytes.length != 32) {
        throw new IllegalStateException("card.encryption.key must decode to 32 bytes for AES-256");
      }
      this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String encrypt(String plaintext) {
      try {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_LENGTH_BYTES + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH_BYTES);
        System.arraycopy(cipherText, 0, combined, IV_LENGTH_BYTES, cipherText.length);
        return Base64.getEncoder().encodeToString(combined);
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException("Failed to encrypt PAN locally", e);
      }
    }

    @Override
    public String decrypt(String ciphertext) {
      try {
        byte[] combined = Base64.getDecoder().decode(ciphertext);
        if (combined.length <= IV_LENGTH_BYTES) {
          throw new IllegalStateException("Ciphertext is too short");
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTES];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(combined, IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain, StandardCharsets.UTF_8);
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException("Failed to decrypt PAN locally", e);
      }
    }
  }
}
