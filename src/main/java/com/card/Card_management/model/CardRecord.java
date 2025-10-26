package com.card.Card_management.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the card information that is persisted in Firestore.
 */
public class CardRecord {

    private String id;
    private String cardholderName;
    private String pan;
    private Instant createdAt;

    public CardRecord() {

    }

    public CardRecord(String id, String cardholderName, String pan, Instant createdAt) {
        this.id = id;
        this.cardholderName = cardholderName;
        this.pan = pan;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;

    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardRecord cardRecord)) return false;
        return Objects.equals(id, cardRecord.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
