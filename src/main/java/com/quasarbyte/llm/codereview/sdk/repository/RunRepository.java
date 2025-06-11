package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.RunDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RunDB entity operations.
 * This repository handles CRUD operations for RunDB entities, which contain
 * the actual review data associated with ReviewDB entities.
 */
public interface RunRepository {

    /**
     * Saves a new RunDB entity.
     * 
     * @param runDB the RunDB entity to save
     * @return the saved RunDB entity with generated ID
     */
    RunDB save(RunDB runDB);

    /**
     * Saves a new RunDB entity with the specified review ID and review parameter.
     * 
     * @param reviewId the ID of the associated review
     * @param reviewParameter the review parameter data
     * @return the saved RunDB entity with generated ID
     */
    RunDB save(Long reviewId, ReviewParameter reviewParameter);

    /**
     * Finds a RunDB entity by its ID.
     * 
     * @param id the ID of the RunDB entity
     * @return an Optional containing the RunDB entity if found, empty otherwise
     */
    Optional<RunDB> findById(Long id);

    /**
     * Finds all RunDB entities associated with a specific review ID.
     * 
     * @param reviewId the ID of the review
     * @return a List of RunDB entities associated with the review
     */
    List<RunDB> findByReviewId(Long reviewId);

    /**
     * Finds the IDs of all runs associated with a specific review ID.
     * 
     * @param reviewId the ID of the review
     * @return a List of run IDs associated with the review
     */
    List<Long> findRunIdsByReviewId(Long reviewId);

    /**
     * Checks if a RunDB entity exists by its ID.
     * 
     * @param id the ID of the RunDB entity
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Finds all RunDB entities.
     * 
     * @return a List of all RunDB entities
     */
    List<RunDB> findAll();

    /**
     * Updates an existing RunDB entity.
     * 
     * @param runDB the RunDB entity to update
     * @return the updated RunDB entity
     */
    RunDB update(RunDB runDB);

    /**
     * Updates the review parameter of a specific run.
     * 
     * @param runId the ID of the run to update
     * @param reviewParameter the new review parameter
     */
    void updateReviewParameter(Long runId, ReviewParameter reviewParameter);

    /**
     * Deletes a RunDB entity by its ID.
     * 
     * @param id the ID of the RunDB entity to delete
     */
    void deleteById(Long id);

    /**
     * Deletes all RunDB entities associated with a specific review ID.
     * 
     * @param reviewId the ID of the review
     * @return the number of deleted entities
     */
    int deleteByReviewId(Long reviewId);

    /**
     * Counts the total number of RunDB entities.
     * 
     * @return the total count of RunDB entities
     */
    long count();

    /**
     * Counts the number of RunDB entities associated with a specific review ID.
     * 
     * @param reviewId the ID of the review
     * @return the count of RunDB entities associated with the review
     */
    int countByReviewId(Long reviewId);
}
