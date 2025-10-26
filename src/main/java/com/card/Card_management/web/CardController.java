package com.card.Card_management.web;

import com.card.Card_management.model.CardRecord;
import com.card.Card_management.service.CardService;
import com.card.Card_management.web.dto.CardResponse;
import com.card.Card_management.web.dto.CreateCardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardRecord record = cardService.createCard(request);
        CardResponse response = new CardResponse(
                record.getId(),
                record.getCardholderName(),
                maskPan(record.getPan()),
                record.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        String lastFour = pan.substring(pan.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
