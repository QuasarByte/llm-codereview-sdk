package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.InferenceDB;
import com.quasarbyte.llm.codereview.sdk.model.db.InferenceStatusEnum;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItemJson;
import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * InferenceRepository implementation using JDBCTemplate.
 */
public class InferenceRepositoryImpl implements InferenceRepository {

    private static final Logger logger = LoggerFactory.getLogger(InferenceRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public InferenceRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public List<InferenceDB> findByReviewId(Long reviewId) {
        logger.debug("Finding inferences for review ID: {}", reviewId);

        String sql = "SELECT id, run_id, review_id, prompt_id, status, reviewed_result_item " +
                "FROM inference WHERE review_id = ? ORDER BY id";
        
        List<InferenceDB> inferences = jdbcTemplate.query(sql, (rs, rowNum) -> {
            InferenceDB inference = new InferenceDB();
            inference.setId(rs.getLong("id"));
            inference.setRunId(rs.getLong("run_id"));
            inference.setReviewId(rs.getLong("review_id"));
            inference.setPromptId(rs.getLong("prompt_id"));
            
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                inference.setStatus(InferenceStatusEnum.valueOf(statusStr));
            }
            
            String reviewedResultItemJson = rs.getString("reviewed_result_item");
            if (reviewedResultItemJson != null) {
                inference.setReviewedResultItem(dbPojoJsonConvertor.convertToPojo(reviewedResultItemJson, ReviewedResultItemJson.class));
            }
            
            return inference;
        }, reviewId);

        logger.debug("Found {} inferences for review ID: {}", inferences.size(), reviewId);
        return inferences;
    }

    @Override
    public List<InferenceDB> findByRunId(Long runId) {
        logger.debug("Finding inferences for run ID: {}", runId);

        String sql = "SELECT id, run_id, review_id, prompt_id, status, reviewed_result_item " +
                "FROM inference WHERE run_id = ? ORDER BY id";
        
        List<InferenceDB> inferences = jdbcTemplate.query(sql, (rs, rowNum) -> {
            InferenceDB inference = new InferenceDB();
            inference.setId(rs.getLong("id"));
            inference.setRunId(rs.getLong("run_id"));
            inference.setReviewId(rs.getLong("review_id"));
            inference.setPromptId(rs.getLong("prompt_id"));
            
            String statusStr = rs.getString("status");
            if (statusStr != null) {
                inference.setStatus(InferenceStatusEnum.valueOf(statusStr));
            }
            
            String reviewedResultItemJson = rs.getString("reviewed_result_item");
            if (reviewedResultItemJson != null) {
                inference.setReviewedResultItem(dbPojoJsonConvertor.convertToPojo(reviewedResultItemJson, ReviewedResultItemJson.class));
            }
            
            return inference;
        }, runId);

        logger.debug("Found {} inferences for run ID: {}", inferences.size(), runId);
        return inferences;
    }

    @Override
    public Long save(InferenceDB inferenceDB) {
        Objects.requireNonNull(inferenceDB, "inferenceDB must not be null");
        logger.debug("Saving inference for review ID: {}, run ID: {}, prompt ID: {}", 
                inferenceDB.getReviewId(), inferenceDB.getRunId(), inferenceDB.getPromptId());

        String reviewedResultItemAsString = null;
        if (inferenceDB.getReviewedResultItem() != null) {
            reviewedResultItemAsString = dbPojoJsonConvertor.convertToString(inferenceDB.getReviewedResultItem());
        }

        String statusString = inferenceDB.getStatus() != null ? inferenceDB.getStatus().name() : null;

        String sql = "INSERT INTO inference (run_id, review_id, prompt_id, status, reviewed_result_item) " +
                "VALUES (:runId, :reviewId, :promptId, :status, :reviewedResultItem)";
        
        Long inferenceId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "runId", inferenceDB.getRunId(),
                        "reviewId", inferenceDB.getReviewId(),
                        "promptId", inferenceDB.getPromptId(),
                        "status", statusString,
                        "reviewedResultItem", reviewedResultItemAsString
                ),
                Long.class);

        logger.info("Saved inference with ID: {} for review ID: {}, run ID: {}, prompt ID: {}", 
                inferenceId, inferenceDB.getReviewId(), inferenceDB.getRunId(), inferenceDB.getPromptId());
        return inferenceId;
    }

    @Override
    public void updateStatus(Long id, InferenceStatusEnum status) {
        Objects.requireNonNull(status, "status must not be null");
        logger.debug("Updating inference ID: {} with status: {}", id, status);

        String sql = "UPDATE inference SET status = :status WHERE id = :id";
        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of("status", status.name(), "id", id));

        logger.info("Updated {} inference(s) with ID: {} to status: {}", updatedCount, id, status);
    }
}
