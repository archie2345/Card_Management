package com.card.Card_management.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload supplied to create a new card record.
 */
public class CreateCardRequest {

  @NotBlank(message = "Cardholder name is required")
  private String cardholderName;

  @NotBlank(message = "PAN is required")
  @Pattern(regexp = "\\d{16}", message = "PAN must be exactly 16 digits")
  private String pan;

  /** Empty request for data binding. */
  public CreateCardRequest() {}

  public String getCardholderName() {
    return cardholderName;
  }

  public void setCardholderName(String cardholderName) {
    this.cardholderName = cardholderName;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }
}
