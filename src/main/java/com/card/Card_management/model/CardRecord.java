package com.card.Card_management.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the card information that is persisted in Firestore.
 */
public class CardRecord {

    private String id;
    private String cardholderName;
    private String panCiphertext;
    private String lastFourHash;
    private Instant createdAt;

    public CardRecord() {

    }

    public CardRecord(String id, String cardholderName, String panCiphertext, String lastFourHash, Instant createdAt) {
        this.id = id;
        this.cardholderName = cardholderName;
        this.panCiphertext = panCiphertext;
        this.lastFourHash = lastFourHash;
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

    public String getPanCiphertext() {
        return panCiphertext;
    }

    public void setPanCiphertext(String panCiphertext) {
        this.panCiphertext = panCiphertext;
    }

    public String getLastFourHash() {
        return lastFourHash;
    }

    public void setLastFourHash(String lastFourHash) {
        this.lastFourHash = lastFourHash;
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
