package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.ResolvedFileDB;
import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileRepository;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * ResolvedFileRepository implementation using JDBCTemplate.
 */
public class ResolvedFileRepositoryImpl implements ResolvedFileRepository {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFileRepositoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public ResolvedFileRepositoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save(ResolvedFileDB resolvedFileDB) {
        Objects.requireNonNull(resolvedFileDB, "fileDB must not be null");
        logger.debug("Saving resolvedFile: {} for review ID: {}", resolvedFileDB.getFileName(), resolvedFileDB.getReviewId());

        String sql = "INSERT INTO resolved_file (file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page) " +
                "VALUES (:fileId, :groupId, :targetId, :reviewId, :fileName, :fileNameExtension, " +
                ":filePath, :codePage)";

        Long resolvedFileId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "fileId", resolvedFileDB.getFileId(),
                        "groupId", resolvedFileDB.getGroupId(),
                        "targetId", resolvedFileDB.getTargetId(),
                        "reviewId", resolvedFileDB.getReviewId(),
                        "fileName", resolvedFileDB.getFileName(),
                        "fileNameExtension", resolvedFileDB.getFileNameExtension(),
                        "filePath", resolvedFileDB.getFilePath(),
                        "codePage", resolvedFileDB.getCodePage()
                ),
                Long.class);

        logger.info("Saved resolvedFile with ID: {} for review ID: {}", resolvedFileId, resolvedFileDB.getReviewId());
        return resolvedFileId;
    }

    @Override
    public Optional<ResolvedFileDB> findById(Long resolvedFileId) {
        logger.debug("Finding resolvedFile by ID: {}", resolvedFileId);
        
        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE id = ?";
        
        Optional<ResolvedFileDB> resolvedFile = jdbcTemplate.queryForObject(sql, this::mapRowToResolvedFileDB, resolvedFileId);
        
        logger.debug("Found resolvedFile with ID: {}", resolvedFileId);
        return resolvedFile;
    }

    @Override
    public List<ResolvedFileDB> findByReviewId(Long reviewId) {
        logger.debug("Finding resolvedFiles for review ID: {}", reviewId);

        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE review_id = ? ORDER BY id";
        
        List<ResolvedFileDB> resolvedFiles = jdbcTemplate.query(sql, this::mapRowToResolvedFileDB, reviewId);

        logger.debug("Found {} resolvedFiles for review ID: {}", resolvedFiles.size(), reviewId);
        return resolvedFiles;
    }

    @Override
    public List<ResolvedFileDB> findByGroupId(Long groupId) {
        logger.debug("Finding resolvedFiles for group ID: {}", groupId);

        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE group_id = ? ORDER BY id";
        
        List<ResolvedFileDB> resolvedFiles = jdbcTemplate.query(sql, this::mapRowToResolvedFileDB, groupId);

        logger.debug("Found {} resolvedFiles for group ID: {}", resolvedFiles.size(), groupId);
        return resolvedFiles;
    }

    @Override
    public List<ResolvedFileDB> findByTargetId(Long targetId) {
        logger.debug("Finding resolvedFiles for target ID: {}", targetId);

        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE target_id = ? ORDER BY id";
        
        List<ResolvedFileDB> resolvedFiles = jdbcTemplate.query(sql, this::mapRowToResolvedFileDB, targetId);

        logger.debug("Found {} resolvedFiles for target ID: {}", resolvedFiles.size(), targetId);
        return resolvedFiles;
    }

    @Override
    public List<ResolvedFileDB> findByFileId(Long fileId) {
        logger.debug("Finding resolvedFiles for file ID: {}", fileId);

        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE file_id = ? ORDER BY id";
        
        List<ResolvedFileDB> resolvedFiles = jdbcTemplate.query(sql, this::mapRowToResolvedFileDB, fileId);

        logger.debug("Found {} resolvedFiles for file ID: {}", resolvedFiles.size(), fileId);
        return resolvedFiles;
    }

    @Override
    public Optional<ResolvedFileDB> findByFileIdAndReviewId(Long fileId, Long reviewId) {
        logger.debug("Finding resolvedFile for file ID: {} and review ID: {}", fileId, reviewId);
        
        String sql = "SELECT id, file_id, group_id, target_id, review_id, file_name, file_name_extension, " +
                "file_path, code_page " +
                "FROM resolved_file WHERE file_id = ? AND review_id = ?";
        
        Optional<ResolvedFileDB> resolvedFile = jdbcTemplate.queryForObject(sql, this::mapRowToResolvedFileDB, fileId, reviewId);
        
        logger.debug("Found resolvedFile for file ID: {} and review ID: {}", fileId, reviewId);
        return resolvedFile;
    }

    @Override
    public List<Long> findFileIdsByReviewId(Long reviewId) {
        logger.debug("Finding resolvedFile IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM resolved_file WHERE review_id = ? ORDER BY id";
        List<Long> resolvedFileIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} resolvedFile IDs for review ID: {}", resolvedFileIds.size(), reviewId);
        return resolvedFileIds;
    }

    @Override
    public List<Long> findFileIdsByGroupId(Long groupId) {
        logger.debug("Finding resolvedFile IDs for group ID: {}", groupId);

        String sql = "SELECT id FROM resolved_file WHERE group_id = ? ORDER BY id";
        List<Long> resolvedFileIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), groupId);

        logger.debug("Found {} resolvedFile IDs for group ID: {}", resolvedFileIds.size(), groupId);
        return resolvedFileIds;
    }

    @Override
    public boolean existsById(Long resolvedFileId) {
        logger.debug("Checking if resolved_file exists with ID: {}", resolvedFileId);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, resolvedFileId);

        boolean exists = count != null && count > 0;
        logger.debug("File with ID {} exists: {}", resolvedFileId, exists);
        return exists;
    }

    @Override
    public boolean existsByFilePath(String resolvedFilePath) {
        logger.debug("Checking if resolvedFile exists with path: {}", resolvedFilePath);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE file_path = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, resolvedFilePath);

        boolean exists = count != null && count > 0;
        logger.debug("File with path {} exists: {}", resolvedFilePath, exists);
        return exists;
    }

    @Override
    public boolean existsByFileId(Long fileId) {
        logger.debug("Checking if resolvedFile exists with file ID: {}", fileId);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE file_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fileId);

        boolean exists = count != null && count > 0;
        logger.debug("File with file ID {} exists: {}", fileId, exists);
        return exists;
    }

    @Override
    public boolean existsByFileIdAndReviewId(Long fileId, Long reviewId) {
        logger.debug("Checking if resolvedFile exists with file ID: {} and review ID: {}", fileId, reviewId);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE file_id = ? AND review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fileId, reviewId);

        boolean exists = count != null && count > 0;
        logger.debug("File with file ID {} and review ID {} exists: {}", fileId, reviewId, exists);
        return exists;
    }

    @Override
    public int countFilesByReviewId(Long reviewId) {
        logger.debug("Counting resolvedFiles for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} resolvedFiles for review ID: {}", result, reviewId);
        return result;
    }

    @Override
    public int countFilesByGroupId(Long groupId) {
        logger.debug("Counting resolvedFiles for group ID: {}", groupId);

        String sql = "SELECT COUNT(*) FROM resolved_file WHERE group_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, groupId);

        int result = count != null ? count : 0;
        logger.debug("Found {} resolvedFiles for group ID: {}", result, groupId);
        return result;
    }

    @Override
    public void updateById(Long resolvedFileId, ResolvedFileDB resolvedFileDB) {
        Objects.requireNonNull(resolvedFileDB, "fileDB must not be null");
        logger.debug("Updating resolvedFile ID: {}", resolvedFileId);

        String sql = "UPDATE resolved_file SET file_id = :fileId, group_id = :groupId, target_id = :targetId, review_id = :reviewId, " +
                "file_name = :fileName, file_name_extension = :fileNameExtension, file_path = :filePath, " +
                "code_page = :codePage WHERE id = :resolvedFileId";

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "fileId", resolvedFileDB.getFileId(),
                        "groupId", resolvedFileDB.getGroupId(),
                        "targetId", resolvedFileDB.getTargetId(),
                        "reviewId", resolvedFileDB.getReviewId(),
                        "fileName", resolvedFileDB.getFileName(),
                        "fileNameExtension", resolvedFileDB.getFileNameExtension(),
                        "filePath", resolvedFileDB.getFilePath(),
                        "codePage", resolvedFileDB.getCodePage(),
                        "resolvedFileId", resolvedFileId
                ));

        logger.info("Updated {} resolvedFile(s) with ID: {}", updatedCount, resolvedFileId);
    }

    @Override
    public void deleteById(Long resolvedFileId) {
        logger.debug("Deleting resolvedFile with ID: {}", resolvedFileId);

        String sql = "DELETE FROM resolved_file WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, resolvedFileId);

        logger.info("Deleted {} resolvedFile(s) with ID: {}", deletedCount, resolvedFileId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        logger.debug("Deleting resolvedFiles for review ID: {}", reviewId);

        String sql = "DELETE FROM resolved_file WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} resolvedFiles for review ID: {}", deletedCount, reviewId);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        logger.debug("Deleting resolvedFiles for group ID: {}", groupId);

        String sql = "DELETE FROM resolved_file WHERE group_id = ?";
        int deletedCount = jdbcTemplate.update(sql, groupId);

        logger.info("Deleted {} resolvedFiles for group ID: {}", deletedCount, groupId);
    }

    @Override
    public void deleteByFileId(Long fileId) {
        logger.debug("Deleting resolvedFiles for file ID: {}", fileId);

        String sql = "DELETE FROM resolved_file WHERE file_id = ?";
        int deletedCount = jdbcTemplate.update(sql, fileId);

        logger.info("Deleted {} resolvedFiles for file ID: {}", deletedCount, fileId);
    }

    /**
     * Maps a database row to a ResolvedFileDB object.
     */
    private ResolvedFileDB mapRowToResolvedFileDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ResolvedFileDB resolvedFile = new ResolvedFileDB();
        resolvedFile.setId(rs.getLong("id"));
        
        // Handle nullable file_id field
        long fileId = rs.getLong("file_id");
        if (!rs.wasNull()) {
            resolvedFile.setFileId(fileId);
        }
        
        // Handle nullable foreign key fields
        long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            resolvedFile.setGroupId(groupId);
        }
        
        long targetId = rs.getLong("target_id");
        if (!rs.wasNull()) {
            resolvedFile.setTargetId(targetId);
        }
        
        long reviewId = rs.getLong("review_id");
        if (!rs.wasNull()) {
            resolvedFile.setReviewId(reviewId);
        }

        resolvedFile.setFileName(rs.getString("file_name"));
        resolvedFile.setFileNameExtension(rs.getString("file_name_extension"));
        resolvedFile.setFilePath(rs.getString("file_path"));

        resolvedFile.setCodePage(rs.getString("code_page"));
        
        return resolvedFile;
    }
}
