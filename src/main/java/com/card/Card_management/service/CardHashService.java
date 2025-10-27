package com.card.Card_management.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

/**
 * Hashes card digits to provide deterministic, non-reversible lookups.
 */
@Service
public class CardHashService {

  private static final HexFormat HEX = HexFormat.of();
  private final MessageDigest digest;

  /**
   * Creates a hash service backed by SHA-256.
   */
  public CardHashService() {
    try {
      this.digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Produces a SHA-256 hash of the final four digits of a PAN.
   *
   * @param lastFourDigits four-character numeric string
   * @return lowercase hex-encoded hash
   */
  public String hashLastFour(String lastFourDigits) {
    byte[] input = lastFourDigits.getBytes(StandardCharsets.UTF_8);
    byte[] hashed = digest.digest(input);
    return HEX.formatHex(hashed);
  }
}
