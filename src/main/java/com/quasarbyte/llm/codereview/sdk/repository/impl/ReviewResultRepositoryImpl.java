package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewResultDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewResultRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * ReviewResultRepository implementation using JDBCTemplate.
 */
public class ReviewResultRepositoryImpl implements ReviewResultRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReviewResultRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public ReviewResultRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public List<ReviewResultDB> findByReviewId(Long reviewId) {
        logger.debug("Finding review results for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, run_id, review_parameter, review_result " +
                "FROM review_result WHERE review_id = ? ORDER BY id";
        
        List<ReviewResultDB> reviewResults = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReviewResultDB reviewResult = new ReviewResultDB();
            reviewResult.setId(rs.getLong("id"));
            reviewResult.setReviewId(rs.getLong("review_id"));
            reviewResult.setRunId(rs.getLong("run_id"));
            
            String reviewParameterJson = rs.getString("review_parameter");
            if (reviewParameterJson != null) {
                reviewResult.setReviewParameter(dbPojoJsonConvertor.convertToPojo(reviewParameterJson, ReviewParameter.class));
            }
            
            String reviewResultJson = rs.getString("review_result");
            if (reviewResultJson != null) {
                reviewResult.setReviewResult(dbPojoJsonConvertor.convertToPojo(reviewResultJson, ReviewResult.class));
            }
            
            return reviewResult;
        }, reviewId);

        logger.debug("Found {} review results for review ID: {}", reviewResults.size(), reviewId);
        return reviewResults;
    }

    @Override
    public List<ReviewResultDB> findByRunId(Long runId) {
        logger.debug("Finding review results for run ID: {}", runId);

        String sql = "SELECT id, review_id, run_id, review_parameter, review_result " +
                "FROM review_result WHERE run_id = ? ORDER BY id";
        
        List<ReviewResultDB> reviewResults = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReviewResultDB reviewResult = new ReviewResultDB();
            reviewResult.setId(rs.getLong("id"));
            reviewResult.setReviewId(rs.getLong("review_id"));
            reviewResult.setRunId(rs.getLong("run_id"));
            
            String reviewParameterJson = rs.getString("review_parameter");
            if (reviewParameterJson != null) {
                reviewResult.setReviewParameter(dbPojoJsonConvertor.convertToPojo(reviewParameterJson, ReviewParameter.class));
            }
            
            String reviewResultJson = rs.getString("review_result");
            if (reviewResultJson != null) {
                reviewResult.setReviewResult(dbPojoJsonConvertor.convertToPojo(reviewResultJson, ReviewResult.class));
            }
            
            return reviewResult;
        }, runId);

        logger.debug("Found {} review results for run ID: {}", reviewResults.size(), runId);
        return reviewResults;
    }

    @Override
    public Long save(ReviewResultDB reviewResultDB) {
        Objects.requireNonNull(reviewResultDB, "reviewResultDB must not be null");
        logger.debug("Saving review result for review ID: {}, run ID: {}", 
                reviewResultDB.getReviewId(), reviewResultDB.getRunId());

        String reviewParameterAsString = null;
        if (reviewResultDB.getReviewParameter() != null) {
            reviewParameterAsString = dbPojoJsonConvertor.convertToString(reviewResultDB.getReviewParameter());
        }

        String reviewResultAsString = null;
        if (reviewResultDB.getReviewResult() != null) {
            reviewResultAsString = dbPojoJsonConvertor.convertToString(reviewResultDB.getReviewResult());
        }

        String sql = "INSERT INTO review_result (review_id, run_id, review_parameter, review_result) " +
                "VALUES (:reviewId, :runId, :reviewParameter, :reviewResult)";
        
        Long reviewResultId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "reviewId", reviewResultDB.getReviewId(),
                        "runId", reviewResultDB.getRunId(),
                        "reviewParameter", reviewParameterAsString,
                        "reviewResult", reviewResultAsString
                ),
                Long.class);

        logger.info("Saved review result with ID: {} for review ID: {}, run ID: {}", 
                reviewResultId, reviewResultDB.getReviewId(), reviewResultDB.getRunId());
        return reviewResultId;
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Deleting review results for review ID: {}", reviewId);

        String sql = "DELETE FROM review_result WHERE review_id = :reviewId";
        int deletedCount = jdbcTemplate.update(sql,
                ParameterSources.of("reviewId", reviewId));

        logger.info("Deleted {} review result(s) for review ID: {}", deletedCount, reviewId);
    }

    @Override
    public void deleteByRunId(Long runId) {
        Objects.requireNonNull(runId, "runId must not be null");
        logger.debug("Deleting review results for run ID: {}", runId);

        String sql = "DELETE FROM review_result WHERE run_id = :runId";
        int deletedCount = jdbcTemplate.update(sql,
                ParameterSources.of("runId", runId));

        logger.info("Deleted {} review result(s) for run ID: {}", deletedCount, runId);
    }
}
