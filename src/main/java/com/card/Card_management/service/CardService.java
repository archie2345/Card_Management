package com.card.Card_management.service;

import com.card.Card_management.model.CardRecord;
import com.card.Card_management.web.dto.CreateCardRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    private static final String COLLECTION_NAME = "cards";

    private final Firestore firestore;

    public CardService(Firestore firestore) {
        this.firestore = firestore;
    }

    public CardRecord createCard(CreateCardRequest request) {
        DocumentReference document = firestore.collection(COLLECTION_NAME).document();
        Instant now = Instant.now();

        CardRecord record = new CardRecord(
                document.getId(),
                request.getCardholderName().trim(),
                request.getPan(),
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

        return record;
    }
}
