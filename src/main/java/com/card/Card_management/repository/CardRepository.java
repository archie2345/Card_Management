package com.card.Card_management.repository;

import com.card.Card_management.model.CardRecord;
import java.util.List;

/**
 * Abstraction over card persistence to support multiple storage backends.
 */
public interface CardRepository {

  /**
   * Persists a new card record and returns the stored representation including any generated id.
   *
   * @param record record to save (id may be null prior to persistence)
   * @return saved record with identifier populated
   */
  CardRecord save(CardRecord record);

  /**
   * Returns every stored card record.
   *
   * @return collection of card records
   */
  List<CardRecord> findAll();

  /**
   * Returns stored cards whose last four hash matches the supplied value.
   *
   * @param lastFourHash hashed last four digits
   * @return matching card records
   */
  List<CardRecord> findByLastFourHash(String lastFourHash);
}
