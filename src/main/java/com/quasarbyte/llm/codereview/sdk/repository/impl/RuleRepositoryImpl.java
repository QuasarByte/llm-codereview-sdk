package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.RuleDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import com.quasarbyte.llm.codereview.sdk.repository.RuleRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RuleRepositoryImpl implements RuleRepository {

    private static final Logger logger = LoggerFactory.getLogger(RuleRepositoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public RuleRepositoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public boolean existById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Checking if rule exists with ID: {}", id);

        String sql = "SELECT COUNT(*) FROM rule WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);

        boolean exists = count != null && count > 0;
        logger.debug("Rule with ID {} exists: {}", id, exists);
        return exists;
    }

    @Override
    public boolean existByReviewIdAndCode(Long reviewId, String code) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        logger.debug("Checking if rule exists with reviewId: {} and code: {}", reviewId, code);

        String sql = "SELECT COUNT(*) FROM rule WHERE review_id = ? AND code = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, code);

        boolean exists = count != null && count > 0;
        logger.debug("Rule with reviewId {} and code {} exists: {}", reviewId, code, exists);
        return exists;
    }

    @Override
    public Optional<RuleDB> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Finding rule by ID: {}", id);

        String sql = "SELECT id, review_id, code, description, severity FROM rule WHERE id = ?";
        Optional<RuleDB> rule = jdbcTemplate.queryForObject(sql, this::mapRowToRuleDB, id);

        logger.debug("Found rule with ID: {}", id);
        return rule;
    }

    @Override
    public Optional<RuleDB> findByReviewIdAndCode(Long reviewId, String code) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        logger.debug("Finding rule by reviewId: {} and code: {}", reviewId, code);

        String sql = "SELECT id, review_id, code, description, severity FROM rule WHERE review_id = ? AND code = ?";
        Optional<RuleDB> rule = jdbcTemplate.queryForObject(sql, this::mapRowToRuleDB, reviewId, code);

        logger.debug("Found rule with reviewId {} and code {}", reviewId, code);
        return rule;
    }

    @Override
    public List<RuleDB> findAll() {
        logger.debug("Finding all rules");

        String sql = "SELECT id, review_id, code, description, severity FROM rule ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB);

        logger.debug("Found {} rules", rules.size());
        return rules;
    }

    @Override
    public List<RuleDB> findAllByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Finding rules for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, code, description, severity FROM rule WHERE review_id = ? ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB, reviewId);

        logger.debug("Found {} rules for review ID: {}", rules.size(), reviewId);
        return rules;
    }

    @Override
    public List<RuleDB> searchByReviewIdAndDescription(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Searching rules by reviewId: {} and description (non-null)", reviewId);

        String sql = "SELECT id, review_id, code, description, severity FROM rule " +
                "WHERE review_id = ? AND description IS NOT NULL ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB, reviewId);

        logger.debug("Found {} rules for reviewId {} with non-null description", rules.size(), reviewId);
        return rules;
    }

    @Override
    public List<RuleDB> searchByDescription(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Searching rules by description (non-null) for reviewId: {}", reviewId);

        String sql = "SELECT id, review_id, code, description, severity FROM rule " +
                "WHERE review_id = ? AND description IS NOT NULL ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB, reviewId);

        logger.debug("Found {} rules with non-null description for reviewId: {}", rules.size(), reviewId);
        return rules;
    }

    @Override
    public List<RuleDB> searchByReviewIdAndDescriptionCI(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Searching rules by reviewId: {} and description (case-insensitive, non-null)", reviewId);

        String sql = "SELECT id, review_id, code, description, severity FROM rule " +
                "WHERE review_id = ? AND UPPER(description) IS NOT NULL ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB, reviewId);

        logger.debug("Found {} rules for reviewId {} with non-null description (CI)", rules.size(), reviewId);
        return rules;
    }

    @Override
    public List<RuleDB> searchByDescriptionCI(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Searching rules by description (case-insensitive, non-null) for reviewId: {}", reviewId);

        String sql = "SELECT id, review_id, code, description, severity FROM rule " +
                "WHERE review_id = ? AND UPPER(description) IS NOT NULL ORDER BY id";
        List<RuleDB> rules = jdbcTemplate.query(sql, this::mapRowToRuleDB, reviewId);

        logger.debug("Found {} rules with non-null description (CI) for reviewId: {}", rules.size(), reviewId);
        return rules;
    }

    @Override
    public Long save(RuleDB ruleDB) {
        Objects.requireNonNull(ruleDB, "ruleDB must not be null");
        logger.debug("Saving rule: {} for review ID: {}", ruleDB.getCode(), ruleDB.getReviewId());

        String sql = "INSERT INTO rule (review_id, code, description, severity) " +
                "VALUES (:reviewId, :code, :description, :severity)";

        String severityValue = ruleDB.getSeverity() != null ? ruleDB.getSeverity().name() : null;

        Long ruleId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "reviewId", ruleDB.getReviewId(),
                        "code", ruleDB.getCode(),
                        "description", ruleDB.getDescription(),
                        "severity", severityValue
                ),
                Long.class);

        logger.info("Saved rule with ID: {} for review ID: {}", ruleId, ruleDB.getReviewId());
        return ruleId;
    }

    @Override
    public void update(RuleDB ruleDB) {
        Objects.requireNonNull(ruleDB, "ruleDB must not be null");
        Objects.requireNonNull(ruleDB.getId(), "ruleDB.id must not be null for update");
        logger.debug("Updating rule ID: {}", ruleDB.getId());

        String sql = "UPDATE rule SET review_id = :reviewId, code = :code, " +
                "description = :description, severity = :severity WHERE id = :ruleId";

        String severityValue = ruleDB.getSeverity() != null ? ruleDB.getSeverity().name() : null;

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "reviewId", ruleDB.getReviewId(),
                        "code", ruleDB.getCode(),
                        "description", ruleDB.getDescription(),
                        "severity", severityValue,
                        "ruleId", ruleDB.getId()
                ));

        logger.info("Updated {} rule(s) with ID: {}", updatedCount, ruleDB.getId());
    }

    @Override
    public void updateReviewId(Long id, Long reviewId) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Updating reviewId for rule ID: {} to {}", id, reviewId);

        String sql = "UPDATE rule SET review_id = ? WHERE id = ?";
        int updatedCount = jdbcTemplate.update(sql, reviewId, id);

        logger.info("Updated reviewId for {} rule(s) with ID: {}", updatedCount, id);
    }

    @Override
    public void updateCode(Long id, String code) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Updating code for rule ID: {} to {}", id, code);

        String sql = "UPDATE rule SET code = ? WHERE id = ?";
        int updatedCount = jdbcTemplate.update(sql, code, id);

        logger.info("Updated code for {} rule(s) with ID: {}", updatedCount, id);
    }

    @Override
    public void updateDescription(Long id, String description) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Updating description for rule ID: {}", id);

        String sql = "UPDATE rule SET description = ? WHERE id = ?";
        int updatedCount = jdbcTemplate.update(sql, description, id);

        logger.info("Updated description for {} rule(s) with ID: {}", updatedCount, id);
    }

    @Override
    public void updateSeverity(Long id, RuleSeverityEnum severity) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Updating severity for rule ID: {} to {}", id, severity);

        String severityValue = severity != null ? severity.name() : null;
        String sql = "UPDATE rule SET severity = ? WHERE id = ?";
        int updatedCount = jdbcTemplate.update(sql, severityValue, id);

        logger.info("Updated severity for {} rule(s) with ID: {}", updatedCount, id);
    }

    @Override
    public void deleteAll() {
        logger.debug("Deleting all rules");

        String sql = "DELETE FROM rule";
        int deletedCount = jdbcTemplate.update(sql);

        logger.info("Deleted {} rule(s)", deletedCount);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        logger.debug("Deleting rule with ID: {}", id);

        String sql = "DELETE FROM rule WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, id);

        logger.info("Deleted {} rule(s) with ID: {}", deletedCount, id);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        logger.debug("Deleting rules for review ID: {}", reviewId);

        String sql = "DELETE FROM rule WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} rules for review ID: {}", deletedCount, reviewId);
    }

    @Override
    public void deleteByReviewIdAndCode(Long reviewId, String code) {
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        logger.debug("Deleting rule with reviewId: {} and code: {}", reviewId, code);

        String sql = "DELETE FROM rule WHERE review_id = ? AND code = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId, code);

        logger.info("Deleted {} rule(s) with reviewId {} and code {}", deletedCount, reviewId, code);
    }

    /**
     * Maps a database row to a RuleDB object.
     */
    private RuleDB mapRowToRuleDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        RuleDB rule = new RuleDB();
        rule.setId(rs.getLong("id"));

        // Handle nullable foreign key fields
        long reviewId = rs.getLong("review_id");
        if (!rs.wasNull()) {
            rule.setReviewId(reviewId);
        }

        rule.setCode(rs.getString("code"));
        rule.setDescription(rs.getString("description"));

        // Handle severity enum
        String severityString = rs.getString("severity");
        if (severityString != null && !severityString.trim().isEmpty()) {
            try {
                rule.setSeverity(RuleSeverityEnum.valueOf(severityString.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid severity value '{}' for rule ID {}, setting to null",
                        severityString, rule.getId());
                rule.setSeverity(null);
            }
        }

        return rule;
    }
}