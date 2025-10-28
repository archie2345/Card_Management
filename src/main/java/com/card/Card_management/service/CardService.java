package com.card.Card_management.service;

import com.card.Card_management.model.CardRecord;
import com.card.Card_management.repository.CardRepository;
import com.card.Card_management.web.dto.CardResponse;
import com.card.Card_management.web.dto.CreateCardRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Coordinates persistence, encryption, and retrieval of card records.
 */
@Service
public class CardService {

  private static final Logger log = LoggerFactory.getLogger(CardService.class);

  private final CardRepository cardRepository;
  private final CardEncryptionService encryptionService;
  private final CardHashService hashService;

  /**
   * Creates a service that depends on Firestore and encryption utilities.
   */
  public CardService(
      CardRepository cardRepository,
      CardEncryptionService encryptionService,
      CardHashService hashService) {
    this.cardRepository = cardRepository;
    this.encryptionService = encryptionService;
    this.hashService = hashService;
  }

  /**
   * Stores a new card record after encrypting the supplied PAN and hashing the last four digits.
   *
   * @param request incoming card creation request
   * @return response describing the persisted card
   */
  public CardResponse createCard(CreateCardRequest request) {
    Instant now = Instant.now();
    String pan = request.getPan();
    String panCiphertext = encryptionService.encryptPan(pan);
    String lastFourHash = hashService.hashLastFour(pan.substring(pan.length() - 4));

    CardRecord record =
        new CardRecord(
            null,
            request.getCardholderName().trim(),
            panCiphertext,
            lastFourHash,
            now);

    CardRecord saved = cardRepository.save(record);
    log.debug("Persisted card {} for {}", saved.getId(), saved.getCardholderName());
    return toResponse(saved, pan);
  }

  /**
   * Retrieves card records, optionally filtered by the hash of the final four digits.
   *
   * @param lastFour optional filter containing four digits
   * @return mask-only representations of matching cards
   */
  public List<CardResponse> getCards(String lastFour) {
    if (lastFour == null || lastFour.isBlank()) {
      return fetchAllRecords().stream().map(this::toResponse).toList();
    }

    String trimmed = lastFour.trim();
    if (trimmed.length() != 4 || !trimmed.matches("\\d{4}")) {
      throw new IllegalArgumentException("Last four digits must be exactly 4 numbers");
    }

    String hash = hashService.hashLastFour(trimmed);
    return fetchByLastFourHash(hash).stream().map(this::toResponse).toList();
  }

  /**
   * Loads every document in the card collection.
   */
  private List<CardRecord> fetchAllRecords() {
    return cardRepository.findAll();
  }

  /**
   * Queries Firestore documents by a precomputed last-four hash.
   */
  private List<CardRecord> fetchByLastFourHash(String hash) {
    return cardRepository.findByLastFourHash(hash);
  }

  /**
   * Builds a response from an encrypted record, masking the decrypted PAN.
   */
  private CardResponse toResponse(CardRecord record) {
    String plainPan = decryptPan(record);
    if (plainPan.length() < 4) {
      return new CardResponse(
          record.getId(), record.getCardholderName(), "****", record.getCreatedAt());
    }
    return new CardResponse(
        record.getId(), record.getCardholderName(), maskFromPlain(plainPan), record.getCreatedAt());
  }

  /**
   * Builds a response when the plain PAN has already been provided.
   */
  private CardResponse toResponse(CardRecord record, String plainPan) {
    return new CardResponse(
        record.getId(), record.getCardholderName(), maskFromPlain(plainPan), record.getCreatedAt());
  }

  /**
   * Decrypts the stored PAN when ciphertext is present.
   */
  private String decryptPan(CardRecord record) {
    String ciphertext = record.getPanCiphertext();
    if (ciphertext == null || ciphertext.isBlank()) {
      log.warn("Card {} is missing ciphertext; skipping decryption", record.getId());
      return "";
    }
    return encryptionService.decryptPan(ciphertext);
  }

  /**
   * Produces a masked representation that only exposes the last four digits.
   */
  private String maskFromPlain(String pan) {
    if (pan == null || pan.length() < 4) {
      return "****";
    }
    String lastFour = pan.substring(pan.length() - 4);
    return "**** **** **** " + lastFour;
  }
}
