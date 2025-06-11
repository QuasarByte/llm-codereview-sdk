package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseConnectionException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseMigrationException;
import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.impl.LiquibaseRunnerServiceImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.util.LiquibaseValidationUtils;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.LoggerUIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class LiquibaseInnerRunnerImpl implements LiquibaseInnerRunner {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseInnerRunnerImpl.class);

    private final LiquibaseConfig config;
    private final ResourceAccessor defaultResourceAccessor;

    public LiquibaseInnerRunnerImpl() {
        this(LiquibaseConfig.builder().build());
        disableLiquibaseAnalytics();
    }

    public LiquibaseInnerRunnerImpl(LiquibaseConfig config) {
        this.config = config;
        this.defaultResourceAccessor = config.getResourceAccessor() != null
                ? config.getResourceAccessor()
                : new ClassLoaderResourceAccessor();

        logger.debug("LiquibaseRunnerService initialized with config: contexts={}, labels={}",
                config.getContexts(), config.getLabels());
    }

    @Override
    public void runMigrations(Connection connection, String changeLogPath) {
        long startTime = System.currentTimeMillis();

        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Starting database migrations with changelog: {}", changeLogPath);

        try {
            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Contexts contexts = new Contexts(config.getContexts());
                    liquibase.update(contexts);
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully completed migrations in {}ms for changelog: {}", duration, changeLogPath);

        } catch (LiquibaseException e) {
            logger.error("Failed to run migrations for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to run Liquibase migrations for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error during migration for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error during migration", e);
        }
    }

    @Override
    public boolean hasUnrunChanges(Connection connection, String changeLogPath) {
        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Checking for unrun changes in changelog: {}", changeLogPath);

        try {

            final boolean hasUnrunChanges;

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                hasUnrunChanges = Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Contexts contexts = new Contexts(config.getContexts());
                    LabelExpression labels = new LabelExpression(config.getLabels());
                    return !liquibase.listUnrunChangeSets(contexts, labels).isEmpty();
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            logger.info("Unrun changes check result for {}: {}", changeLogPath, hasUnrunChanges);

            return hasUnrunChanges;

        } catch (LiquibaseException e) {
            logger.error("Failed to check for unrun changes in changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to check for unrun changes in changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error checking unrun changes for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error while checking unrun changes", e);
        }
    }

    @Override
    public String getStatus(Connection connection, String changeLogPath) {
        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Getting migration status for changelog: {}", changeLogPath);

        try {

            final String statusResult;

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                statusResult = Scope.child(services, () -> {

                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Database database = liquibase.getDatabase();
                    Contexts contexts = new Contexts(config.getContexts());
                    LabelExpression labels = new LabelExpression(config.getLabels());

                    StringBuilder status = new StringBuilder();
                    status.append("Database URL: ").append(database.getConnection().getURL()).append("\n");
                    status.append("Database Product: ").append(database.getDatabaseProductName()).append("\n");
                    status.append("Changelog Path: ").append(changeLogPath).append("\n");
                    status.append("Contexts: ").append(config.getContexts() != null ? config.getContexts() : "none").append("\n");
                    status.append("Labels: ").append(config.getLabels() != null ? config.getLabels() : "none").append("\n");

                    int unrunCount = liquibase.listUnrunChangeSets(contexts, labels).size();
                    status.append("Unrun changesets: ").append(unrunCount).append("\n");

                    return status.toString();

                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            logger.info("Migration status retrieved for changelog: {}", changeLogPath);

            return statusResult;

        } catch (LiquibaseException e) {
            logger.error("Failed to get migration status for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to get migration status for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error getting status for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error while getting status", e);
        }
    }

    @Override
    public void rollback(Connection connection, String changeLogPath, int count) {
        long startTime = System.currentTimeMillis();

        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);
        LiquibaseValidationUtils.validateRollbackCount(count);

        logger.info("Starting rollback of {} changesets for changelog: {}", count, changeLogPath);

        try {

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Contexts contexts = new Contexts(config.getContexts());
                    liquibase.rollback(count, contexts.toString());
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully rolled back {} changesets in {}ms for changelog: {}", count, duration, changeLogPath);

        } catch (LiquibaseException e) {
            logger.error("Failed to rollback {} changesets for changelog: {}, error: {}", count, changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to rollback " + count + " changesets for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error during rollback for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error during rollback", e);
        }
    }

    @Override
    public void rollbackToTag(Connection connection, String changeLogPath, String tag) {
        long startTime = System.currentTimeMillis();

        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);
        LiquibaseValidationUtils.validateTag(tag);

        logger.info("Starting rollback to tag '{}' for changelog: {}", tag, changeLogPath);

        try {

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Contexts contexts = new Contexts(config.getContexts());
                    liquibase.rollback(tag, contexts);
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully rolled back to tag '{}' in {}ms for changelog: {}", tag, duration, changeLogPath);

        } catch (LiquibaseException e) {
            logger.error("Failed to rollback to tag '{}' for changelog: {}, error: {}", tag, changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to rollback to tag '" + tag + "' for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error during rollback to tag for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error during rollback to tag", e);
        }
    }

    @Override
    public boolean validateChangelog(Connection connection, String changeLogPath) {
        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Validating changelog: {}", changeLogPath);

        try {

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    liquibase.validate();
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            logger.info("Changelog validation successful for: {}", changeLogPath);
            return true;

        } catch (LiquibaseException e) {
            logger.warn("Changelog validation failed for: {}, error: {}", changeLogPath, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during changelog validation for: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error during changelog validation", e);
        }
    }

    @Override
    public List<ChangeSet> listUnrunChangeSets(Connection connection, String changeLogPath) {
        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Listing unrun changesets for changelog: {}", changeLogPath);

        try {
            final List<ChangeSet> unrunChangeSets;

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                unrunChangeSets = Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    Contexts contexts = new Contexts(config.getContexts());
                    LabelExpression labels = new LabelExpression(config.getLabels());
                    return liquibase.listUnrunChangeSets(contexts, labels);
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            logger.info("Found {} unrun changesets for changelog: {}", unrunChangeSets.size(), changeLogPath);

            return unrunChangeSets;

        } catch (LiquibaseException e) {
            logger.error("Failed to list unrun changesets for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to list unrun changesets for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error listing unrun changesets for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error while listing unrun changesets", e);
        }
    }

    @Override
    public String generateChangeLog(Connection connection, String outputPath) {
        LiquibaseValidationUtils.validateConnection(connection);
        LiquibaseValidationUtils.validateOutputPath(outputPath);

        logger.info("Generate changelog operation requested for output path: {}", outputPath);

        // Following YAGNI principle - implement only when actually needed
        // This method is not yet implemented for Liquibase 4.32.0 as noted in the review
        throw new UnsupportedOperationException(
                "Generate changelog is not yet implemented for Liquibase 4.32.0. " +
                        "This feature will be added when required. Output path: " + outputPath
        );
    }

    @Override
    public void dropAll(Connection connection, String changeLogPath) {
        long startTime = System.currentTimeMillis();

        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.warn("DESTRUCTIVE OPERATION: Dropping all database objects for changelog: {}", changeLogPath);

        try {

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PrintStream originalOut = System.out;
            try {

                System.setOut(new PrintStream(baos));

                Scope.child(services, () -> {
                    Liquibase liquibase = createLiquibase(connection, changeLogPath);
                    liquibase.dropAll();
                });

            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            long duration = System.currentTimeMillis() - startTime;

            logger.warn("DESTRUCTIVE OPERATION COMPLETED: Dropped all database objects in {}ms for changelog: {}",
                    duration, changeLogPath);

        } catch (LiquibaseException e) {
            logger.error("Failed to drop all database objects for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseMigrationException("Failed to drop all database objects for changelog: " + changeLogPath, e);
        } catch (Exception e) {
            logger.error("Unexpected error during drop all for changelog: {}, error: {}", changeLogPath, e.getMessage(), e);
            throw new LiquibaseConnectionException("Database connection error during drop all operation", e);
        }
    }

    @Override
    public LiquibaseRunnerService createRunner(Connection connection, String changeLogPath) {
        LiquibaseValidationUtils.validateBasicParameters(connection, changeLogPath);

        logger.info("Creating LiquibaseRunner for changelog: {}", changeLogPath);
        return new LiquibaseRunnerServiceImpl(connection, changeLogPath, this);
    }

    /**
     * Creates a Liquibase instance with the provided connection and changelog path.
     * This method encapsulates the common logic for creating Liquibase instances.
     *
     * @param connection    database connection
     * @param changeLogPath path to changelog file
     * @return configured Liquibase instance
     * @throws LiquibaseException if Liquibase creation fails
     */
    private Liquibase createLiquibase(Connection connection, String changeLogPath) throws LiquibaseException {
        try {
            JdbcConnection jdbcConnection = new JdbcConnection(connection);
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(jdbcConnection);

            HashMap<String, Object> services = new HashMap<>();
            services.put(Scope.Attr.ui.name(), new LoggerUIService());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Liquibase liquibase;

            PrintStream originalOut = System.out;
            try {
                System.setOut(new PrintStream(baos));
                liquibase = Scope.child(services, () -> new Liquibase(changeLogPath, defaultResourceAccessor, database));
            } finally {
                System.setOut(originalOut);
            }

            logger.info(getReplaced(baos));

            return liquibase;

        } catch (NullPointerException e) {
            // Handle various NPE cases that can occur with mock or improperly configured connections
            String errorMessage = "Invalid database connection: ";
            String stackTrace = e.getStackTrace().length > 0 ? e.getStackTrace()[0].getClassName() : "";

            if (stackTrace.contains("JdbcConnection")) {
                errorMessage += "connection URL is null. This often occurs with mock connections or improperly configured database connections.";
            } else if (stackTrace.contains("H2Database") || stackTrace.contains("Database")) {
                errorMessage += "database-specific initialization failed. The connection may be missing required SQL statement capabilities.";
            } else {
                errorMessage += "connection initialization failed. This often occurs with mock connections that don't provide complete JDBC functionality.";
            }

            throw new LiquibaseException(errorMessage, e);
        } catch (Exception e) {
            // Handle any other exceptions that might occur during Liquibase creation
            throw new LiquibaseException("Failed to create Liquibase instance: " + e.getMessage(), e);
        }
    }

    private static String getReplaced(ByteArrayOutputStream baos) {
        return baos.toString().replace("\n", " ").replace("\r", " ");
    }

    private static void disableLiquibaseAnalytics() {
        // Disable Liquibase analytics
        System.setProperty("liquibase.analytics.enabled", "false");
        System.setProperty("liquibase.hub.mode", "off");
        System.setProperty("liquibase.hub.api.url", "");
        System.setProperty("liquibase.noAnalytics", "true");
        System.setProperty("liquibase.sendAnalytics", "false");
    }
}
