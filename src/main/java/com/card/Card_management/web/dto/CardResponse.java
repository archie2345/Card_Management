package com.card.Card_management.web.dto;

import java.time.Instant;

public class CardResponse {

    private String id;
    private String cardholderName;
    private String maskedPan;
    private Instant createdAt;

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
