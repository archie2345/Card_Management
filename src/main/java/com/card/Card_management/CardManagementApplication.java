package com.card.Card_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the Spring application that exposes card-management APIs.
 */
@SpringBootApplication
public class CardManagementApplication {

  /**
   * Starts the Spring Boot runtime.
   *
   * @param args command-line arguments forwarded to Spring Boot
   */
  public static void main(String[] args) {
    SpringApplication.run(CardManagementApplication.class, args);
  }
}
