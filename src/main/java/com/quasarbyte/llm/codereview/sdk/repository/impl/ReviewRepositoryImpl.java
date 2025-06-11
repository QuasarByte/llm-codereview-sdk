package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewDB;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ReviewRepository implementation using JDBCTemplate.
 * Handles basic CRUD operations for ReviewDB entities.
 */
public class ReviewRepositoryImpl implements ReviewRepository {

    private static final Logger logger = LoggerFactory.getLogger(ReviewRepositoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public ReviewRepositoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save() {
        logger.debug("Saving new ReviewDB entity");

        String sql = "INSERT INTO review (created_at) VALUES (:createdAt)";

        Long reviewId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of("createdAt", toTimestamp(LocalDateTime.now())),
                Long.class);

        logger.info("Saved ReviewDB entity with ID: {}", reviewId);
        return reviewId;
    }

    @Override
    public ReviewDB save(ReviewDB reviewDB) {
        Objects.requireNonNull(reviewDB, "reviewDB must not be null");

        if (reviewDB.getId() == null) {
            // Insert new entity
            logger.debug("Saving new ReviewDB entity");

            String sql = "INSERT INTO review (created_at) VALUES (:createdAt)";

            Long reviewId = jdbcTemplate.insertAndReturnKey(sql,
                    ParameterSources.of("createdAt", toTimestamp(LocalDateTime.now())),
                    Long.class);

            reviewDB.setId(reviewId);
            logger.info("Saved new ReviewDB entity with ID: {}", reviewId);
        } else {
            // Update existing entity (though ReviewDB has no additional fields to update)
            logger.debug("Updating ReviewDB entity with ID: {}", reviewDB.getId());

            String sql = "UPDATE review SET created_at = ? WHERE id = ?";
            int updatedCount = jdbcTemplate.update(sql, reviewDB.getId(), toTimestamp(reviewDB.getCreatedAt()));

            if (updatedCount == 0) {
                logger.warn("No ReviewDB entity found with ID: {} for update", reviewDB.getId());
            } else {
                logger.info("Updated ReviewDB entity with ID: {}", reviewDB.getId());
            }
        }

        return reviewDB;
    }

    @Override
    public Optional<ReviewDB> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Finding ReviewDB entity by ID: {}", id);

        String sql = "SELECT id FROM review WHERE id = ?";

        try {
            Long foundId = jdbcTemplate.queryForObject(sql, Long.class, id);
            if (foundId != null) {
                ReviewDB reviewDB = new ReviewDB().setId(foundId);
                logger.debug("Found ReviewDB entity with ID: {}", foundId);
                return Optional.of(reviewDB);
            }
        } catch (Exception e) {
            logger.debug("No ReviewDB entity found with ID: {}", id);
        }

        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Checking if ReviewDB entity exists with ID: {}", id);

        String sql = "SELECT COUNT(*) FROM review WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        boolean exists = count != null && count > 0;
        logger.debug("ReviewDB entity with ID {} exists: {}", id, exists);
        return exists;
    }

    @Override
    public List<ReviewDB> findAll() {
        logger.debug("Finding all ReviewDB entities");

        String sql = "SELECT id FROM review ORDER BY id";
        List<ReviewDB> reviewDBs = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReviewDB reviewDB = new ReviewDB();
            reviewDB.setId(rs.getLong("id"));
            return reviewDB;
        });

        logger.debug("Found {} ReviewDB entities", reviewDBs.size());
        return reviewDBs;
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Deleting ReviewDB entity with ID: {}", id);

        String sql = "DELETE FROM review WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, id);

        if (deletedCount > 0) {
            logger.info("Deleted ReviewDB entity with ID: {}", id);
        } else {
            logger.warn("No ReviewDB entity found with ID: {} for deletion", id);
        }
    }

    @Override
    public long count() {
        logger.debug("Counting all ReviewDB entities");

        String sql = "SELECT COUNT(*) FROM review";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        long result = count != null ? count.longValue() : 0L;
        logger.debug("Total ReviewDB entities count: {}", result);
        return result;
    }

    /**
     * Safely parses timestamp from ResultSet, handling SQLite-specific issues.
     */
    private LocalDateTime parseTimestamp(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        try {
            Timestamp timestamp = rs.getTimestamp(columnName);
            return toLocalDateTime(timestamp);
        } catch (java.sql.SQLException e) {
            // SQLite might store timestamps as epoch milliseconds, try parsing as long
            try {
                long epochMillis = rs.getLong(columnName);
                if (rs.wasNull()) {
                    return null;
                }
                return java.time.Instant.ofEpochMilli(epochMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (Exception fallbackException) {
                logger.warn("Failed to parse timestamp from column '{}': {} (fallback also failed: {})",
                        columnName, e.getMessage(), fallbackException.getMessage());
                return null;
            }
        }
    }

    /**
     * Converts LocalDateTime to Timestamp for database storage.
     */
    private Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    /**
     * Converts Timestamp to LocalDateTime for object mapping.
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
