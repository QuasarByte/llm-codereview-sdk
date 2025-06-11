package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.PromptDB;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PromptRepository implementation using JDBCTemplate.
 */
public class PromptRepositoryImpl implements PromptRepository {

    private static final Logger logger = LoggerFactory.getLogger(PromptRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public PromptRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save(PromptDB prompt) {
        Objects.requireNonNull(prompt, "prompt must not be null");
        logger.debug("Saving prompt for review ID: {}", prompt.getReviewId());

        String reviewPromptAsString = dbPojoJsonConvertor.convertToString(prompt.getReviewPrompt());

        String sql = "INSERT INTO prompt (review_id, review_prompt) VALUES (:reviewId, :reviewPrompt)";
        Long promptId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of("reviewId", prompt.getReviewId(), "reviewPrompt", reviewPromptAsString),
                Long.class);

        logger.info("Saved prompt with ID: {} for review ID: {}", promptId, prompt.getReviewId());
        return promptId;
    }

    @Override
    public Optional<PromptDB> findById(Long promptId) {
        logger.debug("Finding prompt by ID: {}", promptId);
        String sql = "SELECT id, review_id, review_prompt FROM prompt WHERE id = ?";
        Optional<PromptDB> prompt = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            PromptDB p = new PromptDB();
            p.setId(rs.getLong("id"));
            p.setReviewId(rs.getLong("review_id"));
            String reviewPromptJson = rs.getString("review_prompt");
            if (reviewPromptJson != null) {
                p.setReviewPrompt(dbPojoJsonConvertor.convertToPojo(reviewPromptJson, ReviewPromptJson.class));
            }
            return p;
        }, promptId);

        logger.debug("Found prompt with ID: {}", promptId);
        return prompt;
    }

    @Override
    public List<PromptDB> findByReviewId(Long reviewId) {
        logger.debug("Finding prompts for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, review_prompt FROM prompt WHERE review_id = ? ORDER BY id";
        List<PromptDB> prompts = jdbcTemplate.query(sql, (rs, rowNum) -> {
            PromptDB prompt = new PromptDB();
            prompt.setId(rs.getLong("id"));
            prompt.setReviewId(rs.getLong("review_id"));
            String reviewPromptJson = rs.getString("review_prompt");
            if (reviewPromptJson != null) {
                prompt.setReviewPrompt(dbPojoJsonConvertor.convertToPojo(reviewPromptJson, ReviewPromptJson.class));
            }
            return prompt;
        }, reviewId);

        logger.debug("Found {} prompts for review ID: {}", prompts.size(), reviewId);
        return prompts;
    }

    @Override
    public List<Long> findPromptIdsByReviewId(Long reviewId) {
        logger.debug("Finding prompt IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM prompt WHERE review_id = ? ORDER BY id";
        List<Long> promptIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} prompt IDs for review ID: {}", promptIds.size(), reviewId);
        return promptIds;
    }

    @Override
    public List<PromptDB> findNotFinishedPromptsByReviewId(Long reviewId) {
        logger.debug("Finding not finished prompts for review ID: {}", reviewId);

        String sql = "SELECT DISTINCT p.id, p.review_id, p.review_prompt " +
                "FROM prompt p " +
                "WHERE p.review_id = ? " +
                "AND p.id NOT IN (" +
                "    SELECT DISTINCT i.prompt_id " +
                "    FROM inference i " +
                "    WHERE i.prompt_id = p.id AND i.status = ?" +
                ") " +
                "ORDER BY p.id";

        List<PromptDB> notFinishedPrompts = jdbcTemplate.query(sql, (rs, rowNum) -> {
            PromptDB prompt = new PromptDB();
            prompt.setId(rs.getLong("id"));
            prompt.setReviewId(rs.getLong("review_id"));
            String reviewPromptJson = rs.getString("review_prompt");
            if (reviewPromptJson != null) {
                prompt.setReviewPrompt(dbPojoJsonConvertor.convertToPojo(reviewPromptJson, ReviewPromptJson.class));
            }
            return prompt;
        }, reviewId, "FINISHED");

        logger.debug("Found {} not finished prompts for review ID: {}", notFinishedPrompts.size(), reviewId);
        return notFinishedPrompts;
    }

    @Override
    public boolean existsById(Long promptId) {
        logger.debug("Checking if prompt exists with ID: {}", promptId);

        String sql = "SELECT COUNT(*) FROM prompt WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, promptId);

        boolean exists = count != null && count > 0;
        logger.debug("Prompt with ID {} exists: {}", promptId, exists);
        return exists;
    }

    @Override
    public int countPromptsByReviewId(Long reviewId) {
        logger.debug("Counting prompts for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM prompt WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} prompts for review ID: {}", result, reviewId);
        return result;
    }

    @Override
    public void updateReviewPromptById(Long promptId, com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson reviewPrompt) {
        Objects.requireNonNull(reviewPrompt, "reviewPrompt must not be null");
        logger.debug("Updating prompt ID: {} with new review prompt", promptId);

        String reviewPromptAsString = dbPojoJsonConvertor.convertToString(reviewPrompt);

        String sql = "UPDATE prompt SET review_prompt = :reviewPrompt WHERE id = :promptId";
        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of("reviewPrompt", reviewPromptAsString, "promptId", promptId));

        logger.info("Updated {} prompt(s) with ID: {}", updatedCount, promptId);
    }

    @Override
    public void deleteById(Long promptId) {
        logger.debug("Deleting prompt with ID: {}", promptId);

        String sql = "DELETE FROM prompt WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, promptId);

        logger.info("Deleted {} prompt(s) with ID: {}", deletedCount, promptId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        logger.debug("Deleting prompts for review ID: {}", reviewId);

        String sql = "DELETE FROM prompt WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} prompts for review ID: {}", deletedCount, reviewId);
    }
}
