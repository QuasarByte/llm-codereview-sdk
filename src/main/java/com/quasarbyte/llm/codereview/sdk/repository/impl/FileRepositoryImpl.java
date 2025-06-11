package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.model.db.FileDB;
import com.quasarbyte.llm.codereview.sdk.repository.FileRepository;
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
 * FileRepository implementation using JDBCTemplate.
 */
public class FileRepositoryImpl implements FileRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public FileRepositoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public Long save(FileDB fileDB) {
        Objects.requireNonNull(fileDB, "fileDB must not be null");
        logger.debug("Saving file: {} for review ID: {}", fileDB.getFileName(), fileDB.getReviewId());

        String sql = "INSERT INTO file (review_id, file_name, file_name_extension, " +
                "file_path, content, size, created_at, modified_at, accessed_at) " +
                "VALUES (:reviewId, :fileName, :fileNameExtension, " +
                ":filePath, :content, :size, :createdAt, :modifiedAt, :accessedAt)";

        Long fileId = jdbcTemplate.insertAndReturnKey(sql,
                ParameterSources.of(
                        "reviewId", fileDB.getReviewId(),
                        "fileName", fileDB.getFileName(),
                        "fileNameExtension", fileDB.getFileNameExtension(),
                        "filePath", fileDB.getFilePath(),
                        "content", fileDB.getContent(),
                        "size", fileDB.getSize(),
                        "createdAt", toTimestamp(fileDB.getCreatedAt()),
                        "modifiedAt", toTimestamp(fileDB.getModifiedAt()),
                        "accessedAt", toTimestamp(fileDB.getAccessedAt())
                ),
                Long.class);

        logger.info("Saved file with ID: {} for review ID: {}", fileId, fileDB.getReviewId());
        return fileId;
    }

    @Override
    public Optional<FileDB> findById(Long fileId) {
        logger.debug("Finding file by ID: {}", fileId);
        
        String sql = "SELECT id, review_id, file_name, file_name_extension, " +
                "file_path, content, size, created_at, modified_at, accessed_at " +
                "FROM file WHERE id = ?";
        
        Optional<FileDB> file = jdbcTemplate.queryForObject(sql, this::mapRowToFileDB, fileId);
        
        logger.debug("Found file with ID: {}", fileId);
        return file;
    }

    @Override
    public Optional<FileDB> findByFilePath(String filePath) {
        logger.debug("Finding file by path: {}", filePath);
        
        String sql = "SELECT id, review_id, file_name, file_name_extension, " +
                "file_path, content, size, created_at, modified_at, accessed_at " +
                "FROM file WHERE file_path = ?";
        
        Optional<FileDB> file = jdbcTemplate.queryForObject(sql, this::mapRowToFileDB, filePath);
        
        logger.debug("Found file with path: {}", filePath);
        return file;
    }

    @Override
    public List<FileDB> findByReviewId(Long reviewId) {
        logger.debug("Finding files for review ID: {}", reviewId);

        String sql = "SELECT id, review_id, file_name, file_name_extension, " +
                "file_path, content, size, created_at, modified_at, accessed_at " +
                "FROM file WHERE review_id = ? ORDER BY id";
        
        List<FileDB> files = jdbcTemplate.query(sql, this::mapRowToFileDB, reviewId);

        logger.debug("Found {} files for review ID: {}", files.size(), reviewId);
        return files;
    }

    @Override
    public List<Long> findFileIdsByReviewId(Long reviewId) {
        logger.debug("Finding file IDs for review ID: {}", reviewId);

        String sql = "SELECT id FROM file WHERE review_id = ? ORDER BY id";
        List<Long> fileIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("id"), reviewId);

        logger.debug("Found {} file IDs for review ID: {}", fileIds.size(), reviewId);
        return fileIds;
    }

    @Override
    public boolean existsById(Long fileId) {
        logger.debug("Checking if file exists with ID: {}", fileId);

        String sql = "SELECT COUNT(*) FROM file WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fileId);

        boolean exists = count != null && count > 0;
        logger.debug("File with ID {} exists: {}", fileId, exists);
        return exists;
    }

    @Override
    public boolean existsByFilePath(String filePath) {
        logger.debug("Checking if file exists with path: {}", filePath);

        String sql = "SELECT COUNT(*) FROM file WHERE file_path = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filePath);

        boolean exists = count != null && count > 0;
        logger.debug("File with path {} exists: {}", filePath, exists);
        return exists;
    }

    @Override
    public int countFilesByReviewId(Long reviewId) {
        logger.debug("Counting files for review ID: {}", reviewId);

        String sql = "SELECT COUNT(*) FROM file WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);

        int result = count != null ? count : 0;
        logger.debug("Found {} files for review ID: {}", result, reviewId);
        return result;
    }

    @Override
    public void updateById(Long fileId, FileDB fileDB) {
        Objects.requireNonNull(fileDB, "fileDB must not be null");
        logger.debug("Updating file ID: {}", fileId);

        String sql = "UPDATE file SET review_id = :reviewId, " +
                "file_name = :fileName, file_name_extension = :fileNameExtension, file_path = :filePath, " +
                "content = :content, size = :size, created_at = :createdAt, modified_at = :modifiedAt, " +
                "accessed_at = :accessedAt WHERE id = :fileId";

        int updatedCount = jdbcTemplate.update(sql,
                ParameterSources.of(
                        "reviewId", fileDB.getReviewId(),
                        "fileName", fileDB.getFileName(),
                        "fileNameExtension", fileDB.getFileNameExtension(),
                        "filePath", fileDB.getFilePath(),
                        "content", fileDB.getContent(),
                        "size", fileDB.getSize(),
                        "createdAt", toTimestamp(fileDB.getCreatedAt()),
                        "modifiedAt", toTimestamp(fileDB.getModifiedAt()),
                        "accessedAt", toTimestamp(fileDB.getAccessedAt()),
                        "fileId", fileId
                ));

        logger.info("Updated {} file(s) with ID: {}", updatedCount, fileId);
    }

    @Override
    public void deleteById(Long fileId) {
        logger.debug("Deleting file with ID: {}", fileId);

        String sql = "DELETE FROM file WHERE id = ?";
        int deletedCount = jdbcTemplate.update(sql, fileId);

        logger.info("Deleted {} file(s) with ID: {}", deletedCount, fileId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        logger.debug("Deleting files for review ID: {}", reviewId);

        String sql = "DELETE FROM file WHERE review_id = ?";
        int deletedCount = jdbcTemplate.update(sql, reviewId);

        logger.info("Deleted {} files for review ID: {}", deletedCount, reviewId);
    }

    /**
     * Maps a database row to a FileDB object.
     */
    private FileDB mapRowToFileDB(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        FileDB file = new FileDB();
        file.setId(rs.getLong("id"));
        
        // Handle nullable foreign key fields
        long reviewId = rs.getLong("review_id");
        if (!rs.wasNull()) {
            file.setReviewId(reviewId);
        }
        
        file.setFileName(rs.getString("file_name"));
        file.setFileNameExtension(rs.getString("file_name_extension"));
        file.setFilePath(rs.getString("file_path"));
        file.setContent(rs.getBytes("content"));
        
        // Handle nullable size field
        long size = rs.getLong("size");
        if (!rs.wasNull()) {
            file.setSize(size);
        }
        
        // Handle timestamps with SQLite-specific parsing
        file.setCreatedAt(parseTimestamp(rs, "created_at"));
        file.setModifiedAt(parseTimestamp(rs, "modified_at"));
        file.setAccessedAt(parseTimestamp(rs, "accessed_at"));

        return file;
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
