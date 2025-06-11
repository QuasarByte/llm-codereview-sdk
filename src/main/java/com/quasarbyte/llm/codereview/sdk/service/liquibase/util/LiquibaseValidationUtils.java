package com.quasarbyte.llm.codereview.sdk.service.liquibase.util;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;

import java.sql.Connection;
import java.util.Objects;

/**
 * Utility class for validating Liquibase operation parameters.
 * Follows DRY principle by centralizing validation logic.
 */
public final class LiquibaseValidationUtils {
    
    private LiquibaseValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates database connection parameter.
     * 
     * @param connection the connection to validate
     * @throws LiquibaseValidationException if connection is null
     */
    public static void validateConnection(Connection connection) {
        Objects.requireNonNull(connection, "Database connection cannot be null");
    }
    
    /**
     * Validates changelog path parameter.
     * 
     * @param changeLogPath the changelog path to validate
     * @throws LiquibaseValidationException if path is null or empty
     */
    public static void validateChangeLogPath(String changeLogPath) {
        Objects.requireNonNull(changeLogPath, "ChangeLog path cannot be null");
        if (changeLogPath.trim().isEmpty()) {
            throw new LiquibaseValidationException("ChangeLog path cannot be empty");
        }
    }
    
    /**
     * Validates rollback count parameter.
     * 
     * @param count the rollback count to validate
     * @throws LiquibaseValidationException if count is not positive
     */
    public static void validateRollbackCount(int count) {
        if (count <= 0) {
            throw new LiquibaseValidationException("Rollback count must be positive, got: " + count);
        }
    }
    
    /**
     * Validates tag parameter for rollback operations.
     * 
     * @param tag the tag to validate
     * @throws LiquibaseValidationException if tag is null or empty
     */
    public static void validateTag(String tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        if (tag.trim().isEmpty()) {
            throw new LiquibaseValidationException("Tag cannot be empty");
        }
    }
    
    /**
     * Validates output path parameter.
     * 
     * @param outputPath the output path to validate
     * @throws LiquibaseValidationException if path is null or empty
     */
    public static void validateOutputPath(String outputPath) {
        Objects.requireNonNull(outputPath, "Output path cannot be null");
        if (outputPath.trim().isEmpty()) {
            throw new LiquibaseValidationException("Output path cannot be empty");
        }
    }
    
    /**
     * Validates all basic parameters for Liquibase operations.
     * 
     * @param connection the database connection
     * @param changeLogPath the changelog path
     * @throws LiquibaseValidationException if any parameter is invalid
     */
    public static void validateBasicParameters(Connection connection, String changeLogPath) {
        validateConnection(connection);
        validateChangeLogPath(changeLogPath);
    }
}
