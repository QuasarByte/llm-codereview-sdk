package com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseConnectionException;
import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;
import liquibase.changelog.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class LiquibaseMigrationManagerImpl implements LiquibaseMigrationManager {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseMigrationManagerImpl.class);

    private final DBConnectionManager dbConnectionManager;
    private final LiquibaseInnerRunner liquibaseRunner;
    private final String defaultChangeLogPath;

    public LiquibaseMigrationManagerImpl(DBConnectionManager dbConnectionManager) {
        this(dbConnectionManager, new LiquibaseInnerRunnerImpl(), LiquibaseConfig.DEFAULT_CHANGELOG_PATH);
    }

    public LiquibaseMigrationManagerImpl(DBConnectionManager dbConnectionManager,
                                         LiquibaseInnerRunner liquibaseRunner) {
        this(dbConnectionManager, liquibaseRunner, LiquibaseConfig.DEFAULT_CHANGELOG_PATH);
    }

    public LiquibaseMigrationManagerImpl(DBConnectionManager dbConnectionManager,
                                         LiquibaseInnerRunner liquibaseRunner,
                                         String defaultChangeLogPath) {
        this.dbConnectionManager = dbConnectionManager;
        this.liquibaseRunner = liquibaseRunner;
        this.defaultChangeLogPath = defaultChangeLogPath;

        logger.debug("LiquibaseService initialized with default changelog: {}", defaultChangeLogPath);
    }

    /**
     * Executes all pending database migrations
     *
     * @throws RuntimeException if migration fails
     */
    @Override
    public void runMigrations() {
        logger.info("Starting migrations with default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            liquibaseRunner.runMigrations(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection for migrations: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection for migrations", e);
        }
    }

    /**
     * Checks if there are any unrun change sets
     *
     * @return true if there are pending migrations
     */
    @Override
    public boolean hasUnrunChanges() {
        logger.debug("Checking for unrun changes with default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            return liquibaseRunner.hasUnrunChanges(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to check unrun changes: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to check unrun changes", e);
        }
    }

    /**
     * Returns the current migration status
     *
     * @return status information as string
     */
    @Override
    public String getStatus() {
        logger.debug("Getting migration status with default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            return liquibaseRunner.getStatus(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to get status: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to get status", e);
        }
    }

    /**
     * Rolls back the specified number of change sets
     *
     * @param count number of change sets to rollback
     */
    @Override
    public void rollback(int count) {
        logger.info("Starting rollback of {} changesets with default changelog: {}", count, defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            liquibaseRunner.rollback(connection, defaultChangeLogPath, count);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection for rollback: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection for rollback", e);
        }
    }

    /**
     * Rolls back to the specified tag
     *
     * @param tag the tag to rollback to
     */
    @Override
    public void rollbackToTag(String tag) {
        logger.info("Starting rollback to tag '{}' with default changelog: {}", tag, defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            liquibaseRunner.rollbackToTag(connection, defaultChangeLogPath, tag);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection for rollback to tag: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection for rollback to tag", e);
        }
    }

    /**
     * Validates the changelog file
     *
     * @return true if changelog is valid
     */
    @Override
    public boolean validateChangelog() {
        logger.debug("Validating default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            return liquibaseRunner.validateChangelog(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to validate changelog: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to validate changelog", e);
        }
    }

    /**
     * Returns list of unrun change sets
     *
     * @return list of unrun change sets
     */
    @Override
    public List<ChangeSet> listUnrunChangeSets() {
        logger.debug("Listing unrun changesets with default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            return liquibaseRunner.listUnrunChangeSets(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to list unrun change sets: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to list unrun change sets", e);
        }
    }

    /**
     * Generates a changelog from the current database state
     *
     * @param outputPath path where to save the generated changelog
     * @return path to the generated changelog file
     */
    @Override
    public String generateChangeLog(String outputPath) {
        logger.info("Generating changelog to output path: {}", outputPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            return liquibaseRunner.generateChangeLog(connection, outputPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to generate changelog: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to generate changelog", e);
        }
    }

    /**
     * Drops all database objects managed by Liquibase
     * WARNING: This will delete all data!
     */
    @Override
    public void dropAll() {
        logger.warn("DESTRUCTIVE OPERATION: Starting drop all with default changelog: {}", defaultChangeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            liquibaseRunner.dropAll(connection, defaultChangeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection to drop all: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection to drop all", e);
        }
    }

    // Additional convenience methods using custom parameters

    /**
     * Executes migrations with custom changelog path
     *
     * @param changeLogPath path to custom changelog file
     */
    public void runMigrations(String changeLogPath) {
        logger.info("Starting migrations with custom changelog: {}", changeLogPath);

        try (Connection connection = dbConnectionManager.openConnection()) {
            liquibaseRunner.runMigrations(connection, changeLogPath);
        } catch (Exception e) {
            logger.error("Failed to obtain database connection for migrations: {}", e.getMessage(), e);
            throw new LiquibaseConnectionException("Failed to obtain database connection for migrations", e);
        }
    }

    /**
     * Executes migrations with custom connection and changelog path
     *
     * @param connection    custom database connection
     * @param changeLogPath path to custom changelog file
     */
    public void runMigrations(Connection connection, String changeLogPath) {
        logger.info("Starting migrations with external connection and changelog: {}", changeLogPath);
        liquibaseRunner.runMigrations(connection, changeLogPath);
    }

    /**
     * Provides access to the underlying LiquibaseRunnerService for advanced operations
     *
     * @return the LiquibaseRunnerService instance
     */
    public LiquibaseInnerRunner getRunner() {
        return liquibaseRunner;
    }

    /**
     * Provides access to the DBConnectionManager
     *
     * @return the DBConnectionManager instance
     */
    public DBConnectionManager getConnectionManager() {
        return dbConnectionManager;
    }

    /**
     * Gets the default changelog path used by this service
     *
     * @return the default changelog path
     */
    public String getDefaultChangeLogPath() {
        return defaultChangeLogPath;
    }
}
