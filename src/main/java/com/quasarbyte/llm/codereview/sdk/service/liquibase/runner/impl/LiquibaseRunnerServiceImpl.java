package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.impl;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.util.LiquibaseValidationUtils;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import liquibase.changelog.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

public class LiquibaseRunnerServiceImpl implements LiquibaseRunnerService {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseRunnerServiceImpl.class);

    private final Connection connection;
    private final String changeLogPath;
    private final LiquibaseInnerRunner liquibaseInnerRunner;
    private boolean closed = false;

    public LiquibaseRunnerServiceImpl(Connection connection, String changeLogPath, LiquibaseInnerRunner liquibaseInnerRunner) {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");
        this.changeLogPath = Objects.requireNonNull(changeLogPath, "ChangeLog path cannot be null");
        this.liquibaseInnerRunner = Objects.requireNonNull(liquibaseInnerRunner, "LiquibaseRunnerService cannot be null");
        
        // Validate parameters at construction time
        try {
            LiquibaseValidationUtils.validateChangeLogPath(changeLogPath);
        } catch (Exception e) {
            throw new LiquibaseValidationException("Invalid changelog path provided to LiquibaseRunner", e);
        }
        
        logger.debug("LiquibaseRunner created for changelog: {}", changeLogPath);
    }

    @Override
    public void update() {
        checkNotClosed();
        logger.debug("Executing update operation");
        liquibaseInnerRunner.runMigrations(connection, changeLogPath);
    }

    @Override
    public void rollback(int count) {
        checkNotClosed();
        logger.debug("Executing rollback operation for {} changesets", count);
        liquibaseInnerRunner.rollback(connection, changeLogPath, count);
    }

    @Override
    public void rollbackToTag(String tag) {
        checkNotClosed();
        logger.debug("Executing rollback to tag operation: {}", tag);
        liquibaseInnerRunner.rollbackToTag(connection, changeLogPath, tag);
    }

    @Override
    public void validate() {
        checkNotClosed();
        logger.debug("Executing validation operation");
        
        boolean isValid = liquibaseInnerRunner.validateChangelog(connection, changeLogPath);
        if (!isValid) {
            throw new LiquibaseValidationException("Changelog validation failed for: " + changeLogPath);
        }
    }

    @Override
    public List<ChangeSet> listUnrunChangeSets() {
        checkNotClosed();
        logger.debug("Listing unrun changesets");
        return liquibaseInnerRunner.listUnrunChangeSets(connection, changeLogPath);
    }

    @Override
    public boolean hasUnrunChanges() {
        checkNotClosed();
        logger.debug("Checking for unrun changes");
        return liquibaseInnerRunner.hasUnrunChanges(connection, changeLogPath);
    }

    @Override
    public String getStatus() {
        checkNotClosed();
        logger.debug("Getting migration status");
        return liquibaseInnerRunner.getStatus(connection, changeLogPath);
    }

    @Override
    public void dropAll() {
        checkNotClosed();
        logger.warn("DESTRUCTIVE OPERATION: Executing drop all operation");
        liquibaseInnerRunner.dropAll(connection, changeLogPath);
    }

    @Override
    public String generateChangeLog(String outputPath) {
        checkNotClosed();
        logger.debug("Generating changelog to: {}", outputPath);
        return liquibaseInnerRunner.generateChangeLog(connection, outputPath);
    }

    @Override
    public String getChangeLogPath() {
        return changeLogPath;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        if (!closed) {
            logger.debug("Closing LiquibaseRunner for changelog: {}", changeLogPath);
            this.closed = true;
        }
    }

    /**
     * Checks if the runner has been closed and throws an exception if it has
     * @throws IllegalStateException if the runner is closed
     */
    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("LiquibaseRunner has been closed and cannot be used");
        }
    }

    /**
     * Checks if the runner is closed
     * @return true if the runner is closed
     */
    public boolean isClosed() {
        return closed;
    }
}
