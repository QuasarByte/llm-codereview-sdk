package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.RunDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.repository.RunRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RunRepository implementation using JDBCTemplate.
 * Handles CRUD operations for RunDB entities.
 */
public class RunRepositoryImpl implements RunRepository {

    private static final Logger logger = LoggerFactory.getLogger(RunRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public RunRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public RunDB save(RunDB runDB) {
        Objects.requireNonNull(runDB, "runDB must not be null");

        if (runDB.getId() == null) {
            // Insert new entity
            logger.debug("Saving new RunDB entity for review ID: {}", runDB.getReviewId());

            String reviewParameterAsString = runDB.getReviewParameter() != null 
                ? dbPojoJsonConvertor.convertToString(runDB.getReviewParameter()) 
                : null;

            String sql = "INSERT INTO run (review_id, review_parameter) VALUES (:reviewId, :reviewParameter)";
            Long runId = jdbcTemplate.insertAndReturnKey(sql,
                    ParameterSources.of("reviewId", runDB.getReviewId(), "reviewParameter", reviewParameterAsString),
                    Long.class);

            runDB.setId(runId);
            logger.info("Saved new RunDB entity with ID: {} for review ID: {}", runId, runDB.getReviewId());
        } else {
            // Update existing entity
            logger.debug("Updating RunDB entity with ID: {}", runDB.getId());

            String reviewParameterAsString = runDB.getReviewParameter() != null 
                ? dbPojoJsonConvertor.convertToString(runDB.getReviewParameter()) 
                : null;

            String sql = "UPDATE run SET review_id = :reviewId, review_parameter = :reviewParameter WHERE id = :runId";
            int updatedCount = jdbcTemplate.update(sql,
                    ParameterSources.of(
                        "reviewId", runDB.getReviewId(),
                        "reviewParameter", reviewParameterAsString,
                        "runId", runDB.getId()));

            if (updatedCount == 0) {
                logger.warn("No RunDB entity found with ID: {} for update", runDB.getId());
            } else {
                logger.info("Updated RunDB entity with ID: {}", runDB.getId());
            }
        }

        return runDB;
    }

    @Override
    public RunDB save(Long reviewId, ReviewParameter reviewParameter) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        logger.debug("Creating new RunDB entity for review ID: {}", reviewId);

        RunDB runDB = new RunDB()
                .setReviewId(reviewId)
                .setReviewParameter(reviewParameter);

        return save(runDB);
    }

    @Override
    public Optional<RunDB> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Finding RunDB entity by ID: {}", id);

        String sql = "SELECT id, review_id, review_parameter FROM run WHERE id = ?";
        Optional<RunDB> result = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            RunDB runDB = new RunDB();
            runDB.setId(rs.getLong("id"));
            runDB.setReviewId(rs.getLong("review_id"));
            
            String reviewParameterJson = rs.getString("review_parameter");
            if (reviewParameterJson != null) {
                ReviewParameter reviewParameter = dbPojoJsonConvertor.convertToPojo(reviewParameterJson, ReviewParameter.class);
                runDB.setReviewParameter(reviewParameter);
            }
            
            return runDB;
        }, id);

        if (result.isPresent()) {
            logger.debug("Found RunDB entity with ID: {}", id);
        } else {
            logger.debug("No RunDB entity found with ID: {}", id);
        }
        return result;
    }

    @Override
    public List<RunDB> findByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Finding RunDB entities by review ID: {}", reviewId);

        String sql = "SELECT id, review_id, review_parameter FROM run WHERE review_id = ? ORDER BY id";
        List<RunDB> runDBs = jdbcTemplate.query(sql, this::mapRowToRunDB, reviewId);

        logger.debug("Found {} RunDB entities for review ID: {}", runDBs.size(), reviewId);
        return runDBs;
    }

    @Override
    public List<Long> findRunIdsByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Finding run IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM run WHERE review_id = ? ORDER BY id";
        List<Long> runIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} run IDs for review ID: {}", runIds.size(), reviewId);
        return runIds;
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Checking if RunDB entity exists with ID: {}", id);

        String sql = "SELECT COUNT(*) FROM run WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        boolean exists = count != null && count > 0;
        logger.debug("RunDB entity with ID {} exists: {}", id, exists);
        return exists;
    }

    @Override
    public List<RunDB> findAll() {
        logger.debug("Finding all RunDB entities");

        String sql = "SELECT id, review_id, review_parameter FROM run ORDER BY id";
        List<RunDB> runDBs = jdbcTemplate.query(sql, this::mapRowToRunDB);

        logger.debug("Found {} RunDB entities", runDBs.size());
        return runDBs;
    }

    @Override
    public RunDB update(RunDB runDB) {
        Objects.requireNonNull(runDB, "runDB must not be null");
        Objects.requireNonNull(runDB.getId(), "runDB.id must not be null for update");

        return save(runDB);
    }

    @Override
    public void updateReviewParameter(Long runId, ReviewParameter reviewParameter) {
        Objects.requireNonNull(runId, "runId must not be null");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        logger.debug("Updating review parameter for run ID: {}", runId);

        String reviewParameterAsString = dbPojoJsonConvertor.convertToString(reviewParameter);

        String sql = "UPDATE run SET review_parameter = :reviewParameter WHERE id = :runId";
        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of("reviewParameter", reviewParameterAsString, "runId", runId));

        if (updatedCount > 0) {
            logger.info("Updated review parameter for run ID: {}", runId);
        } else {
            logger.warn("No RunDB entity found with ID: {} for review parameter update", runId);
        }
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Deleting RunDB entity with ID: {}", id);

        String sql = "DELETE FROM run WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, id);

        if (deletedCount > 0) {
            logger.info("Deleted RunDB entity with ID: {}", id);
        } else {
            logger.warn("No RunDB entity found with ID: {} for deletion", id);
        }
    }

    @Override
    public int deleteByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Deleting RunDB entities for review ID: {}", reviewId);

        String sql = "DELETE FROM run WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} RunDB entities for review ID: {}", deletedCount, reviewId);
        return deletedCount;
    }

    @Override
    public long count() {
        logger.debug("Counting all RunDB entities");

        String sql = "SELECT COUNT(*) FROM run";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        long result = count != null ? count.longValue() : 0L;
        logger.debug("Total RunDB entities count: {}", result);
        return result;
    }

    @Override
    public int countByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Counting RunDB entities for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM run WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} RunDB entities for review ID: {}", result, reviewId);
        return result;
    }

    /**
     * Helper method to map ResultSet rows to RunDB entities.
     * Extracted to reduce code duplication.
     */
    private RunDB mapRowToRunDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        RunDB runDB = new RunDB();
        runDB.setId(rs.getLong("id"));
        runDB.setReviewId(rs.getLong("review_id"));
        
        String reviewParameterJson = rs.getString("review_parameter");
        if (reviewParameterJson != null) {
            ReviewParameter reviewParameter = dbPojoJsonConvertor.convertToPojo(reviewParameterJson, ReviewParameter.class);
            runDB.setReviewParameter(reviewParameter);
        }
        
        return runDB;
    }
}
