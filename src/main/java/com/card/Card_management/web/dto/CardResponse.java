package com.card.Card_management.web.dto;

import java.time.Instant;

/**
 * Immutable response payload returned to API clients for card lookups.
 */
public class CardResponse {

  private String id;
  private String cardholderName;
  private String maskedPan;
  private Instant createdAt;

  /**
   * Creates a response with masked card details.
   *
   * @param id unique identifier of the stored card
   * @param cardholderName name associated with the card
   * @param maskedPan masked representation of the primary account number
   * @param createdAt timestamp the record was created
   */
  public CardResponse(String id, String cardholderName, String maskedPan, Instant createdAt) {
    this.id = id;
    this.cardholderName = cardholderName;
    this.maskedPan = maskedPan;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getCardholderName() {
    return cardholderName;
  }

  public String getMaskedPan() {
    return maskedPan;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
