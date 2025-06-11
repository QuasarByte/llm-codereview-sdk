package com.quasarbyte.llm.codereview.sdk.service.liquibase.migration;

import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import liquibase.changelog.ChangeSet;

import java.sql.Connection;
import java.util.List;

/**
 * High-level interface for managing database migrations.
 * This interface provides convenient methods for common migration scenarios
 * and encapsulates best practices for database migration management.
 */
public interface LiquibaseMigrationService {

    /**
     * Performs a complete migration workflow: validation, check for unrun changes, and migration
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @throws RuntimeException if migration fails
     */
    void performMigration(Connection connection, String changeLogPath);

    /**
     * Performs a safe rollback with validation
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @param count number of changesets to rollback
     * @throws RuntimeException if rollback fails
     */
    void performRollback(Connection connection, String changeLogPath, int count);

    /**
     * Performs a safe rollback to a specific tag
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @param tag tag to rollback to
     * @throws RuntimeException if rollback fails
     */
    void performRollbackToTag(Connection connection, String changeLogPath, String tag);

    /**
     * Validates a changelog and returns detailed validation results
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @return validation results
     */
    ValidationResult validateChangelog(Connection connection, String changeLogPath);

    /**
     * Gets migration status with detailed information
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @return detailed migration status
     */
    MigrationStatus getMigrationStatus(Connection connection, String changeLogPath);

    /**
     * Generates a changelog from current database state
     * @param connection database connection
     * @param outputPath output path for generated changelog
     * @return path to generated changelog
     */
    String generateChangelogFromDatabase(Connection connection, String outputPath);

    /**
     * Creates a LiquibaseRunner for advanced operations
     * @param connection database connection
     * @param changeLogPath path to changelog file
     * @return configured LiquibaseRunner
     */
    LiquibaseRunnerService createRunner(Connection connection, String changeLogPath);

    /**
     * Validation result holder
     */
    class ValidationResult {
        private final boolean valid;
        private final String message;
        private final Throwable error;

        public ValidationResult(boolean valid, String message, Throwable error) {
            this.valid = valid;
            this.message = message;
            this.error = error;
        }

        public ValidationResult(boolean valid, String message) {
            this(valid, message, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getError() {
            return error;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    ", error=" + (error != null ? error.getMessage() : "none") +
                    '}';
        }
    }

    /**
     * Migration status holder
     */
    class MigrationStatus {
        private final String databaseUrl;
        private final String databaseProduct;
        private final String changelogPath;
        private final List<ChangeSet> unrunChangeSets;
        private final int appliedChangeSetsCount;
        private final String detailedStatus;

        public MigrationStatus(String databaseUrl, String databaseProduct, String changelogPath,
                             List<ChangeSet> unrunChangeSets, int appliedChangeSetsCount, String detailedStatus) {
            this.databaseUrl = databaseUrl;
            this.databaseProduct = databaseProduct;
            this.changelogPath = changelogPath;
            this.unrunChangeSets = unrunChangeSets;
            this.appliedChangeSetsCount = appliedChangeSetsCount;
            this.detailedStatus = detailedStatus;
        }

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        public String getDatabaseProduct() {
            return databaseProduct;
        }

        public String getChangelogPath() {
            return changelogPath;
        }

        public List<ChangeSet> getUnrunChangeSets() {
            return unrunChangeSets;
        }

        public int getUnrunChangeSetsCount() {
            return unrunChangeSets != null ? unrunChangeSets.size() : 0;
        }

        public int getAppliedChangeSetsCount() {
            return appliedChangeSetsCount;
        }

        public String getDetailedStatus() {
            return detailedStatus;
        }

        public boolean hasUnrunChanges() {
            return getUnrunChangeSetsCount() > 0;
        }

        @Override
        public String toString() {
            return "MigrationStatus{" +
                    "databaseUrl='" + databaseUrl + '\'' +
                    ", databaseProduct='" + databaseProduct + '\'' +
                    ", changelogPath='" + changelogPath + '\'' +
                    ", unrunChangeSetsCount=" + getUnrunChangeSetsCount() +
                    ", appliedChangeSetsCount=" + appliedChangeSetsCount +
                    '}';
        }
    }
}
