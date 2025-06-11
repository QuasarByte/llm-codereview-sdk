package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.db.RuleDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;

import java.util.List;
import java.util.Optional;

public interface RuleService {

    /**
     * Thread-safe upsert operation for rules.
     * If a rule with the given reviewId and code exists, returns its ID.
     * Otherwise, creates a new rule and returns the new ID.
     *
     * @param reviewId the review ID
     * @param rule the rule to upsert
     * @return the ID of the existing or newly created rule
     */
    Long findOrInsertRule(Long reviewId, Rule rule);

    /**
     * Thread-safe upsert operation for RuleDB.
     * If a rule with the given reviewId and code exists, returns its ID.
     * Otherwise, creates a new rule and returns the new ID.
     *
     * @param ruleDB the rule to upsert (reviewId and code must be set)
     * @return the ID of the existing or newly created rule
     */
    Long findOrInsertRuleDB(RuleDB ruleDB);

    /**
     * Check if a rule exists by ID.
     */
    boolean existsById(Long id);

    /**
     * Check if a rule exists by reviewId and code.
     */
    boolean existsByReviewIdAndCode(Long reviewId, String code);

    /**
     * Find a rule by ID.
     */
    Optional<RuleDB> findById(Long id);

    /**
     * Find a rule by reviewId and code.
     */
    Optional<RuleDB> findByReviewIdAndCode(Long reviewId, String code);

    /**
     * Find all rules.
     */
    List<RuleDB> findAll();

    /**
     * Find all rules for a specific review.
     */
    List<RuleDB> findAllByReviewId(Long reviewId);

    /**
     * Search rules by reviewId with non-null description.
     */
    List<RuleDB> searchByReviewIdAndDescription(Long reviewId);

    /**
     * Search rules by non-null description for a specific review.
     */
    List<RuleDB> searchByDescription(Long reviewId);

    /**
     * Search rules by reviewId with non-null description (case-insensitive).
     */
    List<RuleDB> searchByReviewIdAndDescriptionCI(Long reviewId);

    /**
     * Search rules by non-null description for a specific review (case-insensitive).
     */
    List<RuleDB> searchByDescriptionCI(Long reviewId);

    /**
     * Create a new rule.
     */
    Long save(RuleDB ruleDB);

    /**
     * Update an existing rule.
     */
    void update(RuleDB ruleDB);

    /**
     * Update the reviewId of a rule.
     */
    void updateReviewId(Long id, Long reviewId);

    /**
     * Update the code of a rule.
     */
    void updateCode(Long id, String code);

    /**
     * Update the description of a rule.
     */
    void updateDescription(Long id, String description);

    /**
     * Update the severity of a rule.
     */
    void updateSeverity(Long id, RuleSeverityEnum severity);

    /**
     * Delete all rules.
     */
    void deleteAll();

    /**
     * Delete a rule by ID.
     */
    void deleteById(Long id);

    /**
     * Delete all rules for a specific review.
     */
    void deleteByReviewId(Long reviewId);

    /**
     * Delete a rule by reviewId and code.
     */
    void deleteByReviewIdAndCode(Long reviewId, String code);
}