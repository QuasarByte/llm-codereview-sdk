package com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.impl;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseMigrationException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.LiquibaseMigrationService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of DatabaseMigrationManager that provides high-level migration management.
 * This class encapsulates best practices for database migrations and provides
 * convenient methods for common migration scenarios.
 */
public class LiquibaseMigrationServiceImpl implements LiquibaseMigrationService {

    private final LiquibaseInnerRunner liquibaseInnerRunner;

    public LiquibaseMigrationServiceImpl(LiquibaseInnerRunner liquibaseInnerRunner) {
        this.liquibaseInnerRunner = Objects.requireNonNull(liquibaseInnerRunner, "LiquibaseRunnerService cannot be null");
    }

    @Override
    public void performMigration(Connection connection, String changeLogPath) {
        try (LiquibaseRunnerService runner = createRunner(connection, changeLogPath)) {
            // Step 1: Validate changelog
            ValidationResult validationResult = validateChangelog(connection, changeLogPath);
            if (!validationResult.isValid()) {
                throw new LiquibaseValidationException("Changelog validation failed: " + validationResult.getMessage());
            }

            // Step 2: Check for unrun changes
            List<ChangeSet> unrunChangeSets = runner.listUnrunChangeSets();
            if (unrunChangeSets.isEmpty()) {
                // No changes to apply
                return;
            }

            // Step 3: Apply migrations
            runner.update();

        } catch (Exception e) {
            throw new LiquibaseMigrationException("Migration failed for changelog: " + changeLogPath, e);
        }
    }

    @Override
    public void performRollback(Connection connection, String changeLogPath, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Rollback count must be positive");
        }

        try (LiquibaseRunnerService runner = createRunner(connection, changeLogPath)) {
            // Validate before rollback
            ValidationResult validationResult = validateChangelog(connection, changeLogPath);
            if (!validationResult.isValid()) {
                throw new LiquibaseValidationException("Cannot perform rollback on invalid changelog: " + validationResult.getMessage());
            }

            // Perform rollback
            runner.rollback(count);

        } catch (Exception e) {
            throw new LiquibaseException("Rollback failed for changelog: " + changeLogPath, e);
        }
    }

    @Override
    public void performRollbackToTag(Connection connection, String changeLogPath, String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }

        try (LiquibaseRunnerService runner = createRunner(connection, changeLogPath)) {
            // Validate before rollback
            ValidationResult validationResult = validateChangelog(connection, changeLogPath);
            if (!validationResult.isValid()) {
                throw new LiquibaseValidationException("Cannot perform rollback on invalid changelog: " + validationResult.getMessage());
            }

            // Perform rollback to tag
            runner.rollbackToTag(tag);

        } catch (Exception e) {
            throw new LiquibaseException("Rollback to tag '" + tag + "' failed for changelog: " + changeLogPath, e);
        }
    }

    @Override
    public ValidationResult validateChangelog(Connection connection, String changeLogPath) {
        try {
            boolean isValid = liquibaseInnerRunner.validateChangelog(connection, changeLogPath);
            if (isValid) {
                return new ValidationResult(true, "Changelog is valid");
            } else {
                return new ValidationResult(false, "Changelog validation failed");
            }
        } catch (Exception e) {
            return new ValidationResult(false, "Validation error: " + e.getMessage(), e);
        }
    }

    @Override
    public MigrationStatus getMigrationStatus(Connection connection, String changeLogPath) {
        try {
            // Get database information
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            String databaseUrl = database.getConnection().getURL();
            String databaseProduct = database.getDatabaseProductName();

            // Get unrun change sets
            List<ChangeSet> unrunChangeSets = liquibaseInnerRunner.listUnrunChangeSets(connection, changeLogPath);

            // Get detailed status
            String detailedStatus = liquibaseInnerRunner.getStatus(connection, changeLogPath);

            // Calculate applied changesets count (this is an approximation)
            // In a real implementation, you might want to query the DATABASECHANGELOG table
            int appliedChangeSetsCount = calculateAppliedChangeSetsCount(connection);

            return new MigrationStatus(
                    databaseUrl,
                    databaseProduct,
                    changeLogPath,
                    unrunChangeSets,
                    appliedChangeSetsCount,
                    detailedStatus
            );

        } catch (Exception e) {
            throw new LiquibaseException("Failed to get migration status", e);
        }
    }

    @Override
    public String generateChangelogFromDatabase(Connection connection, String outputPath) {
        return liquibaseInnerRunner.generateChangeLog(connection, outputPath);
    }

    @Override
    public LiquibaseRunnerService createRunner(Connection connection, String changeLogPath) {
        return liquibaseInnerRunner.createRunner(connection, changeLogPath);
    }

    /**
     * Calculates the number of applied changesets by querying the DATABASECHANGELOG table.
     * This is a simplified implementation that assumes the table exists.
     *
     * @param connection database connection
     * @return number of applied changesets
     */
    private int calculateAppliedChangeSetsCount(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM DATABASECHANGELOG");
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            // Table might not exist yet, return 0
            return 0;
        }
    }
}
