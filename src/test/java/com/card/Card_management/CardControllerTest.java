package com.card.Card_management;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.card.Card_management.service.CardService;
import com.card.Card_management.web.CardController;
import com.card.Card_management.web.dto.CardResponse;
import com.card.Card_management.web.dto.CreateCardRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardController.class)
class CardControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private CardService cardService;

  @Test
  void createCard_returnsCreatedResponse() throws Exception {
    CreateCardRequest request = new CreateCardRequest();
    request.setCardholderName("Jane Doe");
    request.setPan("1234567812345678");

    CardResponse response =
        new CardResponse(
            "abc",
            "Jane Doe",
            "**** **** **** 5678",
            Instant.parse("2024-01-01T00:00:00Z"));
    given(cardService.createCard(any(CreateCardRequest.class))).willReturn(response);

    mockMvc
        .perform(
            post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("abc"))
        .andExpect(jsonPath("$.maskedPan").value("**** **** **** 5678"));

    verify(cardService).createCard(any(CreateCardRequest.class));
  }

  @Test
  void searchByLastFour_returnsResults() throws Exception {
    List<CardResponse> responses =
        List.of(
            new CardResponse(
                "def",
                "John Smith",
                "**** **** **** 4321",
                Instant.parse("2024-01-02T00:00:00Z")));
    given(cardService.getCards("4321")).willReturn(responses);

    mockMvc
        .perform(get("/api/cards/search").param("last4", "4321"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("def"))
        .andExpect(jsonPath("$[0].maskedPan").value("**** **** **** 4321"));

    verify(cardService).getCards("4321");
  }
}
