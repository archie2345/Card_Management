package com.card.Card_management;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FirebaseSmokeTest {
    @Autowired Firestore firestore;

    @Test
    void canWriteAndRead() throws Exception {
        DocumentReference doc = firestore.collection("Test").document();
        Map<String, Object> payload = Map.of("timestamp", Instant.now().toString());
        doc.set(payload).get();

        Map<String, Object> result = doc.get().get().getData();
        assertNotNull(result);
    }
}
