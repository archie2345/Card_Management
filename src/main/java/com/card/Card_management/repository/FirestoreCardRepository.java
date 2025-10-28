package com.card.Card_management.repository;

import com.card.Card_management.model.CardRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

/**
 * Card repository backed by Google Cloud Firestore.
 */
@Repository
@ConditionalOnBean(Firestore.class)
public class FirestoreCardRepository implements CardRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreCardRepository.class);
  private static final String COLLECTION_NAME = "cards";

  private final Firestore firestore;

  public FirestoreCardRepository(Firestore firestore) {
    this.firestore = firestore;
  }

  @Override
  public CardRecord save(CardRecord record) {
    DocumentReference document = firestore.collection(COLLECTION_NAME).document();
    CardRecord persisted =
        new CardRecord(
            document.getId(),
            record.getCardholderName(),
            record.getPanCiphertext(),
            record.getLastFourHash(),
            record.getCreatedAt() != null ? record.getCreatedAt() : Instant.now());

    try {
      ApiFuture<WriteResult> writeFuture = document.set(persisted);
      WriteResult writeResult = writeFuture.get();
      log.debug("Persisted card {} at {}", persisted.getId(), writeResult.getUpdateTime());
      return persisted;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while storing card information", e);
    } catch (ExecutionException e) {
      log.error("Failed to write card {} to Firestore", document.getId(), e);
      throw new IllegalStateException("Could not store card information", e);
    }
  }

  @Override
  public List<CardRecord> findAll() {
    try {
      ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
      return future.get().getDocuments().stream()
          .map(doc -> doc.toObject(CardRecord.class))
          .toList();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while querying card information", e);
    } catch (ExecutionException e) {
      log.error("Failed to query card collection", e);
      throw new IllegalStateException("Could not query card information", e);
    }
  }

  @Override
  public List<CardRecord> findByLastFourHash(String lastFourHash) {
    try {
      ApiFuture<QuerySnapshot> future =
          firestore.collection(COLLECTION_NAME).whereEqualTo("lastFourHash", lastFourHash).get();
      return future.get().getDocuments().stream()
          .map(doc -> doc.toObject(CardRecord.class))
          .toList();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while querying card information", e);
    } catch (ExecutionException e) {
      log.error("Failed to query card collection by hash {}", lastFourHash, e);
      throw new IllegalStateException("Could not query card information", e);
    }
  }
}
