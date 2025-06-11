package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.ResolvedFileGroupDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;
import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileGroupRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ResolvedFileGroupRepository implementation using JDBCTemplate.
 */
public class ResolvedFileGroupRepositoryImpl implements ResolvedFileGroupRepository {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFileGroupRepositoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public ResolvedFileGroupRepositoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save(ResolvedFileGroupDB resolvedFileGroupDB) {
        Objects.requireNonNull(resolvedFileGroupDB, "resolvedFileGroupDB must not be null");
        logger.debug("Saving resolvedFileGroup for review ID: {} and target ID: {}", 
                     resolvedFileGroupDB.getReviewId(), resolvedFileGroupDB.getTargetId());

        String fileGroupJson = dbPojoJsonConvertor.convertToString(resolvedFileGroupDB.getFileGroup());

        String sql = "INSERT INTO resolved_file_group (target_id, review_id, file_group) " +
                "VALUES (:targetId, :reviewId, :fileGroup)";

        Long resolvedFileGroupId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "targetId", resolvedFileGroupDB.getTargetId(),
                        "reviewId", resolvedFileGroupDB.getReviewId(),
                        "fileGroup", fileGroupJson
                ),
                Long.class);

        logger.info("Saved resolvedFileGroup with ID: {} for review ID: {} and target ID: {}", 
                    resolvedFileGroupId, resolvedFileGroupDB.getReviewId(), resolvedFileGroupDB.getTargetId());
        return resolvedFileGroupId;
    }

    @Override
    public Optional<ResolvedFileGroupDB> findById(Long resolvedFileGroupId) {
        logger.debug("Finding resolvedFileGroup by ID: {}", resolvedFileGroupId);
        
        String sql = "SELECT id, target_id, review_id, file_group " +
                "FROM resolved_file_group WHERE id = ?";
        
        Optional<ResolvedFileGroupDB> resolvedFileGroup = jdbcTemplate.queryForObject(sql, this::mapRowToResolvedFileGroupDB, resolvedFileGroupId);
        
        logger.debug("Found resolvedFileGroup with ID: {}", resolvedFileGroupId);
        return resolvedFileGroup;
    }

    @Override
    public List<ResolvedFileGroupDB> findByReviewId(Long reviewId) {
        logger.debug("Finding resolvedFileGroups for review ID: {}", reviewId);

        String sql = "SELECT id, target_id, review_id, file_group " +
                "FROM resolved_file_group WHERE review_id = ? ORDER BY id";
        
        List<ResolvedFileGroupDB> resolvedFileGroups = jdbcTemplate.query(sql, this::mapRowToResolvedFileGroupDB, reviewId);

        logger.debug("Found {} resolvedFileGroups for review ID: {}", resolvedFileGroups.size(), reviewId);
        return resolvedFileGroups;
    }

    @Override
    public List<ResolvedFileGroupDB> findByTargetId(Long targetId) {
        logger.debug("Finding resolvedFileGroups for target ID: {}", targetId);

        String sql = "SELECT id, target_id, review_id, file_group " +
                "FROM resolved_file_group WHERE target_id = ? ORDER BY id";
        
        List<ResolvedFileGroupDB> resolvedFileGroups = jdbcTemplate.query(sql, this::mapRowToResolvedFileGroupDB, targetId);

        logger.debug("Found {} resolvedFileGroups for target ID: {}", resolvedFileGroups.size(), targetId);
        return resolvedFileGroups;
    }

    @Override
    public List<ResolvedFileGroupDB> findByReviewIdAndTargetId(Long reviewId, Long targetId) {
        logger.debug("Finding resolvedFileGroups for review ID: {} and target ID: {}", reviewId, targetId);

        String sql = "SELECT id, target_id, review_id, file_group " +
                "FROM resolved_file_group WHERE review_id = ? AND target_id = ? ORDER BY id";
        
        List<ResolvedFileGroupDB> resolvedFileGroups = jdbcTemplate.query(sql, this::mapRowToResolvedFileGroupDB, reviewId, targetId);

        logger.debug("Found {} resolvedFileGroups for review ID: {} and target ID: {}", 
                     resolvedFileGroups.size(), reviewId, targetId);
        return resolvedFileGroups;
    }

    @Override
    public List<Long> findGroupIdsByReviewId(Long reviewId) {
        logger.debug("Finding resolvedFileGroup IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM resolved_file_group WHERE review_id = ? ORDER BY id";
        List<Long> resolvedFileGroupIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} resolvedFileGroup IDs for review ID: {}", resolvedFileGroupIds.size(), reviewId);
        return resolvedFileGroupIds;
    }

    @Override
    public List<Long> findGroupIdsByTargetId(Long targetId) {
        logger.debug("Finding resolvedFileGroup IDs for target ID: {}", targetId);

        String sql = "SELECT id FROM resolved_file_group WHERE target_id = ? ORDER BY id";
        List<Long> resolvedFileGroupIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), targetId);

        logger.debug("Found {} resolvedFileGroup IDs for target ID: {}", resolvedFileGroupIds.size(), targetId);
        return resolvedFileGroupIds;
    }

    @Override
    public boolean existsById(Long resolvedFileGroupId) {
        logger.debug("Checking if resolvedFileGroup exists with ID: {}", resolvedFileGroupId);

        String sql = "SELECT COUNT(*) FROM resolved_file_group WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, resolvedFileGroupId);

        boolean exists = count != null && count > 0;
        logger.debug("ResolvedFileGroup with ID {} exists: {}", resolvedFileGroupId, exists);
        return exists;
    }

    @Override
    public boolean existsByReviewId(Long reviewId) {
        logger.debug("Checking if resolvedFileGroups exist for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM resolved_file_group WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        boolean exists = count != null && count > 0;
        logger.debug("ResolvedFileGroups for review ID {} exist: {}", reviewId, exists);
        return exists;
    }

    @Override
    public boolean existsByReviewIdAndTargetId(Long reviewId, Long targetId) {
        logger.debug("Checking if resolvedFileGroup exists with review ID: {} and target ID: {}", reviewId, targetId);

        String sql = "SELECT COUNT(*) FROM resolved_file_group WHERE review_id = ? AND target_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, targetId);

        boolean exists = count != null && count > 0;
        logger.debug("ResolvedFileGroup with review ID {} and target ID {} exists: {}", reviewId, targetId, exists);
        return exists;
    }

    @Override
    public int countGroupsByReviewId(Long reviewId) {
        logger.debug("Counting resolvedFileGroups for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM resolved_file_group WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} resolvedFileGroups for review ID: {}", result, reviewId);
        return result;
    }

    @Override
    public int countGroupsByTargetId(Long targetId) {
        logger.debug("Counting resolvedFileGroups for target ID: {}", targetId);

        String sql = "SELECT COUNT(*) FROM resolved_file_group WHERE target_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, targetId);

        int result = count != null ? count : 0;
        logger.debug("Found {} resolvedFileGroups for target ID: {}", result, targetId);
        return result;
    }

    @Override
    public void updateById(Long resolvedFileGroupId, ResolvedFileGroupDB resolvedFileGroupDB) {
        Objects.requireNonNull(resolvedFileGroupDB, "resolvedFileGroupDB must not be null");
        logger.debug("Updating resolvedFileGroup ID: {}", resolvedFileGroupId);

        String fileGroupJson = dbPojoJsonConvertor.convertToString(resolvedFileGroupDB.getFileGroup());

        String sql = "UPDATE resolved_file_group SET target_id = :targetId, review_id = :reviewId, " +
                "file_group = :fileGroup WHERE id = :resolvedFileGroupId";

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "targetId", resolvedFileGroupDB.getTargetId(),
                        "reviewId", resolvedFileGroupDB.getReviewId(),
                        "fileGroup", fileGroupJson,
                        "resolvedFileGroupId", resolvedFileGroupId
                ));

        logger.info("Updated {} resolvedFileGroup(s) with ID: {}", updatedCount, resolvedFileGroupId);
    }

    @Override
    public void deleteById(Long resolvedFileGroupId) {
        logger.debug("Deleting resolvedFileGroup with ID: {}", resolvedFileGroupId);

        String sql = "DELETE FROM resolved_file_group WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, resolvedFileGroupId);

        logger.info("Deleted {} resolvedFileGroup(s) with ID: {}", deletedCount, resolvedFileGroupId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        logger.debug("Deleting resolvedFileGroups for review ID: {}", reviewId);

        String sql = "DELETE FROM resolved_file_group WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} resolvedFileGroups for review ID: {}", deletedCount, reviewId);
    }

    @Override
    public void deleteByTargetId(Long targetId) {
        logger.debug("Deleting resolvedFileGroups for target ID: {}", targetId);

        String sql = "DELETE FROM resolved_file_group WHERE target_id = ?";
        int deletedCount = jdbcTemplate.update(sql, targetId);

        logger.info("Deleted {} resolvedFileGroups for target ID: {}", deletedCount, targetId);
    }

    /**
     * Maps a database row to a ResolvedFileGroupDB object.
     */
    private ResolvedFileGroupDB mapRowToResolvedFileGroupDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ResolvedFileGroupDB resolvedFileGroup = new ResolvedFileGroupDB();
        resolvedFileGroup.setId(rs.getLong("id"));
        
        // Handle nullable foreign key fields
        long targetId = rs.getLong("target_id");
        if (!rs.wasNull()) {
            resolvedFileGroup.setTargetId(targetId);
        }
        
        long reviewId = rs.getLong("review_id");
        if (!rs.wasNull()) {
            resolvedFileGroup.setReviewId(reviewId);
        }

        // Deserialize FileGroup from JSON
        String fileGroupJson = rs.getString("file_group");
        if (fileGroupJson != null) {
            FileGroup fileGroup = dbPojoJsonConvertor.convertToPojo(fileGroupJson, FileGroup.class);
            resolvedFileGroup.setFileGroup(fileGroup);
        }
        
        return resolvedFileGroup;
    }
}
