package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewTargetDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewTargetRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ReviewTargetRepository implementation using JDBCTemplate.
 */
public class ReviewTargetRepositoryImpl implements ReviewTargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReviewTargetRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public ReviewTargetRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save(ReviewTargetDB reviewTargetDB) {
        Objects.requireNonNull(reviewTargetDB, "reviewTargetDB must not be null");
        logger.debug("Saving reviewTarget for review ID: {}", reviewTargetDB.getReviewId());

        String reviewTargetJson = dbPojoJsonConvertor.convertToString(reviewTargetDB.getReviewTarget());

        String sql = "INSERT INTO review_target (review_id, review_target) " +
                "VALUES (:reviewId, :reviewTarget)";

        Long reviewTargetId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "reviewId", reviewTargetDB.getReviewId(),
                        "reviewTarget", reviewTargetJson
                ),
                Long.class);

        logger.info("Saved reviewTarget with ID: {} for review ID: {}",
                reviewTargetId, reviewTargetDB.getReviewId());
        return reviewTargetId;
    }

    @Override
    public Optional<ReviewTargetDB> findById(Long reviewTargetId) {
        logger.debug("Finding reviewTarget by ID: {}", reviewTargetId);

        String sql = "SELECT id, review_id, review_target " +
                "FROM review_target WHERE id = ?";

        Optional<ReviewTargetDB> reviewTarget = jdbcTemplate.queryForObject(sql, this::mapRowToReviewTargetDB, reviewTargetId);

        logger.debug("Found reviewTarget with ID: {}", reviewTargetId);
        return reviewTarget;
    }

    @Override
    public List<ReviewTargetDB> findByReviewId(Long reviewId) {
        logger.debug("Finding reviewTargets for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, review_target " +
                "FROM review_target WHERE review_id = ? ORDER BY id";

        List<ReviewTargetDB> reviewTargets = jdbcTemplate.query(sql, this::mapRowToReviewTargetDB, reviewId);

        logger.debug("Found {} reviewTargets for review ID: {}", reviewTargets.size(), reviewId);
        return reviewTargets;
    }

    @Override
    public Optional<ReviewTargetDB> findFirstByReviewId(Long reviewId) {
        logger.debug("Finding first reviewTarget for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, review_target " +
                "FROM review_target WHERE review_id = ? ORDER BY id LIMIT 1";

        Optional<ReviewTargetDB> reviewTarget = jdbcTemplate.queryForObject(sql, this::mapRowToReviewTargetDB, reviewId);

        logger.debug("Found first reviewTarget for review ID: {}", reviewId);
        return reviewTarget;
    }

    @Override
    public List<Long> findTargetIdsByReviewId(Long reviewId) {
        logger.debug("Finding reviewTarget IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM review_target WHERE review_id = ? ORDER BY id";
        List<Long> reviewTargetIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} reviewTarget IDs for review ID: {}", reviewTargetIds.size(), reviewId);
        return reviewTargetIds;
    }

    @Override
    public boolean existsById(Long reviewTargetId) {
        logger.debug("Checking if reviewTarget exists with ID: {}", reviewTargetId);

        String sql = "SELECT COUNT(*) FROM review_target WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewTargetId);

        boolean exists = count != null && count > 0;
        logger.debug("ResolvedReviewTarget with ID {} exists: {}", reviewTargetId, exists);
        return exists;
    }

    @Override
    public boolean existsByReviewId(Long reviewId) {
        logger.debug("Checking if reviewTarget exists with review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM review_target WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        boolean exists = count != null && count > 0;
        logger.debug("ResolvedReviewTarget with review ID {} exists: {}", reviewId, exists);
        return exists;
    }

    @Override
    public int countTargetsByReviewId(Long reviewId) {
        logger.debug("Counting reviewTargets for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM review_target WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} reviewTargets for review ID: {}", result, reviewId);
        return result;
    }

    @Override
    public void updateById(Long reviewTargetId, ReviewTargetDB reviewTargetDB) {
        Objects.requireNonNull(reviewTargetDB, "reviewTargetDB must not be null");
        logger.debug("Updating reviewTarget ID: {}", reviewTargetId);

        String reviewTargetJson = dbPojoJsonConvertor.convertToString(reviewTargetDB.getReviewTarget());

        String sql = "UPDATE review_target SET review_id = :reviewId, " +
                "review_target = :reviewTarget WHERE id = :reviewTargetId";

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "reviewId", reviewTargetDB.getReviewId(),
                        "reviewTarget", reviewTargetJson,
                        "reviewTargetId", reviewTargetId
                ));

        logger.info("Updated {} reviewTarget(s) with ID: {}", updatedCount, reviewTargetId);
    }

    @Override
    public void updateResolvedReviewTargetById(Long reviewTargetId, ReviewTarget reviewTarget) {
        Objects.requireNonNull(reviewTarget, "reviewTarget must not be null");
        logger.debug("Updating reviewTarget ID: {} with new ReviewTarget", reviewTargetId);

        String reviewTargetJson = dbPojoJsonConvertor.convertToString(reviewTarget);

        String sql = "UPDATE review_target SET review_target = :reviewTarget WHERE id = :reviewTargetId";

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "reviewTarget", reviewTargetJson,
                        "reviewTargetId", reviewTargetId
                ));

        logger.info("Updated {} reviewTarget(s) with ID: {} with new ReviewTarget", updatedCount, reviewTargetId);
    }

    @Override
    public void deleteById(Long reviewTargetId) {
        logger.debug("Deleting reviewTarget with ID: {}", reviewTargetId);

        String sql = "DELETE FROM review_target WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewTargetId);

        logger.info("Deleted {} reviewTarget(s) with ID: {}", deletedCount, reviewTargetId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        logger.debug("Deleting reviewTargets for review ID: {}", reviewId);

        String sql = "DELETE FROM review_target WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} reviewTargets for review ID: {}", deletedCount, reviewId);
    }

    /**
     * Maps a database row to a ReviewTargetDB object.
     */
    private ReviewTargetDB mapRowToReviewTargetDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ReviewTargetDB reviewTargetDB = new ReviewTargetDB();
        reviewTargetDB.setId(rs.getLong("id"));
        reviewTargetDB.setReviewId(rs.getLong("review_id"));

        // Deserialize ReviewTarget from JSON
        String reviewTargetJson = rs.getString("review_target");
        if (reviewTargetJson != null) {
            ReviewTarget reviewTarget = dbPojoJsonConvertor.convertToPojo(reviewTargetJson, ReviewTarget.class);
            reviewTargetDB.setReviewTarget(reviewTarget);
        }

        return reviewTargetDB;
    }
}
