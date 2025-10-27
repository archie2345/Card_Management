package com.card.Card_management.service;

import com.card.Card_management.model.CardRecord;
import com.card.Card_management.web.dto.CardResponse;
import com.card.Card_management.web.dto.CreateCardRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    private static final String COLLECTION_NAME = "cards";

    private final Firestore firestore;
    private final CardEncryptionService encryptionService;
    private final CardHashService hashService;

    public CardService(Firestore firestore, CardEncryptionService encryptionService, CardHashService hashService) {
        this.firestore = firestore;
        this.encryptionService = encryptionService;
        this.hashService = hashService;
    }

    public CardResponse createCard(CreateCardRequest request) {
        DocumentReference document = firestore.collection(COLLECTION_NAME).document();
        Instant now = Instant.now();
        String pan = request.getPan();
        String panCiphertext = encryptionService.encryptPan(pan);
        String lastFourHash = hashService.hashLastFour(pan.substring(pan.length() - 4));

        CardRecord record = new CardRecord(
                document.getId(),
                request.getCardholderName().trim(),
                panCiphertext,
                lastFourHash,
                now
        );

        try {
            ApiFuture<WriteResult> writeFuture = document.set(record);
            WriteResult writeResult = writeFuture.get();
            log.debug("Persisted card {} at {}", document.getId(), writeResult.getUpdateTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while storing card information", e);
        } catch (ExecutionException e) {
            log.error("Failed to write card {} to Firestore", document.getId(), e);
            throw new IllegalStateException("Could not store card information", e);
        }

        return toResponse(record);
    }

    public List<CardResponse> getCards(String lastFour) {
        if (lastFour == null || lastFour.isBlank()) {
            return fetchAllRecords().stream()
                    .map(this::toResponse)
                    .toList();
        }

        String trimmed = lastFour.trim();
        if (trimmed.length() != 4 || !trimmed.matches("\\d{4}")) {
            throw new IllegalArgumentException("Last four digits must be exactly 4 numbers");
        }

        String hash = hashService.hashLastFour(trimmed);
        return fetchByLastFourHash(hash).stream()
                .map(this::toResponse)
                .toList();
    }

    private List<CardRecord> fetchAllRecords() {
        try {
            ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            return future.get()
                    .getDocuments()
                    .stream()
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

    private List<CardRecord> fetchByLastFourHash(String hash) {
        try {
            ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("lastFourHash", hash)
                    .get();
            return future.get()
                    .getDocuments()
                    .stream()
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

    private CardResponse toResponse(CardRecord record) {
        String plainPan = decryptPan(record);
        if (plainPan.length() < 4) {
            return new CardResponse(
                    record.getId(),
                    record.getCardholderName(),
                    "****",
                    record.getCreatedAt()
            );
        }
        return new CardResponse(
                record.getId(),
                record.getCardholderName(),
                maskFromPlain(plainPan),
                record.getCreatedAt()
        );
    }

    private CardResponse toResponse(CardRecord record, String plainPan) {
        return new CardResponse(
                record.getId(),
                record.getCardholderName(),
                maskFromPlain(plainPan),
                record.getCreatedAt()
        );
    }

    private String decryptPan(CardRecord record) {
        String ciphertext = record.getPanCiphertext();
        if (ciphertext == null || ciphertext.isBlank()) {
            log.warn("Card {} is missing ciphertext; skipping decryption", record.getId());
            return "";
        }
        return encryptionService.decryptPan(ciphertext);
    }

    private String maskFromPlain(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        String lastFour = pan.substring(pan.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
