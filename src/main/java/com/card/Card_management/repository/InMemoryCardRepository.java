package com.card.Card_management.repository;

import com.card.Card_management.model.CardRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;

/**
 * Lightweight repository to support local development without external services.
 */
@Repository
@ConditionalOnMissingBean(FirestoreCardRepository.class)
public class InMemoryCardRepository implements CardRepository {

  private final ConcurrentMap<String, CardRecord> store = new ConcurrentHashMap<>();

  @Override
  public CardRecord save(CardRecord record) {
    String id = record.getId();
    if (id == null || id.isBlank()) {
      id = UUID.randomUUID().toString();
    }

    Instant createdAt = record.getCreatedAt() != null ? record.getCreatedAt() : Instant.now();
    CardRecord persisted =
        new CardRecord(
            id,
            record.getCardholderName(),
            record.getPanCiphertext(),
            record.getLastFourHash(),
            createdAt);

    store.put(persisted.getId(), persisted);
    return persisted;
  }

  @Override
  public List<CardRecord> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<CardRecord> findByLastFourHash(String lastFourHash) {
    return store.values().stream()
        .filter(record -> lastFourHash.equals(record.getLastFourHash()))
        .toList();
  }
}
