package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewDB;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ReviewDB entity operations.
 * This repository handles basic CRUD operations for ReviewDB entities.
 * All additional information is stored in RunDB via RunRepository.
 */
public interface ReviewRepository {

    /**
     * Saves a new ReviewDB entity and returns the generated ID.
     * 
     * @return the generated ID of the saved ReviewDB entity
     */
    Long save();

    /**
     * Saves a ReviewDB entity.
     * 
     * @param reviewDB the ReviewDB entity to save
     * @return the saved ReviewDB entity with generated ID
     */
    ReviewDB save(ReviewDB reviewDB);

    /**
     * Finds a ReviewDB entity by its ID.
     * 
     * @param id the ID of the ReviewDB entity
     * @return an Optional containing the ReviewDB entity if found, empty otherwise
     */
    Optional<ReviewDB> findById(Long id);

    /**
     * Checks if a ReviewDB entity exists by its ID.
     * 
     * @param id the ID of the ReviewDB entity
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Finds all ReviewDB entities.
     * 
     * @return a List of all ReviewDB entities
     */
    List<ReviewDB> findAll();

    /**
     * Deletes a ReviewDB entity by its ID.
     * 
     * @param id the ID of the ReviewDB entity to delete
     */
    void deleteById(Long id);

    /**
     * Counts the total number of ReviewDB entities.
     * 
     * @return the total count of ReviewDB entities
     */
    long count();
}
