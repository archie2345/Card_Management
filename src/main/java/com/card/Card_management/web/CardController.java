package com.card.Card_management.web;

import com.card.Card_management.service.CardService;
import com.card.Card_management.web.dto.CardResponse;
import com.card.Card_management.web.dto.CreateCardRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that shows operations for managing card data.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

  private final CardService cardService;

  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  /**
   * Persists an encrypted card record.
   *
   * @param request payload containing user-supplied card details
   * @return created card descriptor with masked PAN
   */
  @PostMapping
  public ResponseEntity<CardResponse> createCard(
      @Valid @RequestBody CreateCardRequest request) {
    CardResponse response = cardService.createCard(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Returns card records, optionally filtered by the last four digits. */
  @GetMapping
  public ResponseEntity<List<CardResponse>> getCards(
      @RequestParam(value = "last4", required = false) String lastFour) {
    return ResponseEntity.ok(cardService.getCards(lastFour));
  }

  /**
   * Specialized endpoint to search by last four digits.
   *
   * @param lastFour mandatory four-digit filter
   * @return list of cards whose last four digits match the supplied value
   */
  @GetMapping("/search")
  public ResponseEntity<List<CardResponse>> searchByLastFour(
      @RequestParam(value = "last4", required = false) String lastFour) {
    if (lastFour == null || lastFour.isBlank()) {
      return ResponseEntity.badRequest().body(List.of());
    }
    return ResponseEntity.ok(cardService.getCards(lastFour));
  }
}
