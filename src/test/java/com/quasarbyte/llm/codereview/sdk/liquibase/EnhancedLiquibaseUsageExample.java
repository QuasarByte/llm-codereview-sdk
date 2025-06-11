package com.quasarbyte.llm.codereview.sdk.liquibase;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseConnectionException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseMigrationException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl.LiquibaseMigrationManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced example demonstrating all improvements to the Liquibase architecture.
 * Shows proper error handling, logging, validation, and best practices.
 * <p>
 * Follows software development best practices:
 * - SOLID: Clear separation of concerns
 * - DRY: Reusable patterns and utilities
 * - KISS: Simple, focused examples
 * - YAGNI: Only demonstrates implemented features
 * - TDD: Examples can serve as integration tests
 */
public class EnhancedLiquibaseUsageExample {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedLiquibaseUsageExample.class);

    public static void main(String[] args) {
        logger.info("=== Enhanced Liquibase Usage Examples ===");

        // Mock connection manager for demonstration
        DBConnectionManager connectionManager = new MockDBConnectionManager();

        // Example 1: Enhanced error handling and validation
        demonstrateErrorHandling();

        // Example 2: Logging and monitoring
        demonstrateLoggingAndMonitoring(connectionManager);

        // Example 3: Advanced configuration patterns
        demonstrateAdvancedConfiguration(connectionManager);

        // Example 4: Resource management best practices
        demonstrateResourceManagement();

        // Example 5: Thread safety considerations
        demonstrateThreadSafety();

        logger.info("=== All examples completed successfully ===");
    }

    /**
     * Example 1: Demonstrates enhanced error handling with specific exception types
     */
    private static void demonstrateErrorHandling() {
        logger.info("\n=== Example 1: Enhanced Error Handling ===");

        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl();

        try (Connection connection = createTestConnection()) {

            // Demonstrate validation exceptions
            try {
                runner.runMigrations(null, "test.xml");
            } catch (NullPointerException e) {
                logger.info("✓ Caught expected validation error: {}", e.getMessage());
            }

            // Demonstrate rollback validation
            try {
                runner.rollback(connection, "test.xml", -1);
            } catch (LiquibaseValidationException e) {
                logger.info("✓ Caught expected rollback validation error: {}", e.getMessage());
            }

            // Demonstrate changelog validation
            boolean isValid = runner.validateChangelog(connection, "non-existent.xml");
            logger.info("✓ Changelog validation result for non-existent file: {}", isValid);

        } catch (LiquibaseConnectionException e) {
            logger.error("✗ Connection error: {}", e.getMessage());
        } catch (LiquibaseMigrationException e) {
            logger.error("✗ Migration error: {}", e.getMessage());
        } catch (SQLException e) {
            logger.error("✗ Database error: {}", e.getMessage());
        }
    }

    /**
     * Example 2: Demonstrates logging and monitoring capabilities
     */
    private static void demonstrateLoggingAndMonitoring(DBConnectionManager connectionManager) {
        logger.info("\n=== Example 2: Logging and Monitoring ===");

        LiquibaseMigrationManagerFactory factory = new LiquibaseMigrationManagerFactoryImpl(connectionManager);
        LiquibaseMigrationManager service = factory.create();

        try {
            // Enable detailed logging for migrations
            logger.info("Starting monitored migration process...");

            // Check status before migration
            String statusBefore = service.getStatus();
            logger.info("Status before migration:\n{}", statusBefore);

            // Check for pending migrations
            boolean hasPending = service.hasUnrunChanges();
            logger.info("Has pending migrations: {}", hasPending);

            // Validate changelog before running
            boolean isValid = service.validateChangelog();
            logger.info("Changelog validation result: {}", isValid);

            if (isValid && hasPending) {
                logger.info("All checks passed, proceeding with migration...");
                // Note: In real usage, this would run actual migrations
                // service.runMigrations();
                logger.info("Migration completed successfully");
            } else {
                logger.info("Skipping migration - conditions not met");
            }

        } catch (Exception e) {
            logger.error("Migration process failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Example 3: Demonstrates advanced configuration patterns
     */
    private static void demonstrateAdvancedConfiguration(DBConnectionManager connectionManager) {
        logger.info("\n=== Example 3: Advanced Configuration ===");

        // Production configuration
        LiquibaseConfig prodConfig = LiquibaseConfig.builder()
                .changeLogPath("environments/production-changelog.xml")
                .contexts("production,hotfix")
                .labels("version-2.0,critical")
                .resourceAccessor(new ClassLoaderResourceAccessor())
                .build();

        // Development configuration
        LiquibaseConfig devConfig = LiquibaseConfig.builder()
                .changeLogPath("environments/development-changelog.xml")
                .contexts("development,test")
                .labels("version-2.0,experimental")
                .build();

        // Demonstrate environment-specific services
        LiquibaseMigrationManagerFactory factory = new LiquibaseMigrationManagerFactoryImpl(connectionManager);

        try {
            LiquibaseMigrationManager prodService = factory.create(prodConfig);
            LiquibaseMigrationManager devService = factory.create(devConfig);

            logger.info("✓ Created production service with contexts: {}", prodConfig.getContexts());
            logger.info("✓ Created development service with contexts: {}", devConfig.getContexts());

            // Demonstrate validation for different environments
            boolean prodValid = prodService.validateChangelog();
            boolean devValid = devService.validateChangelog();

            logger.info("Production changelog valid: {}", prodValid);
            logger.info("Development changelog valid: {}", devValid);

        } catch (Exception e) {
            logger.warn("Configuration example failed (expected for demo): {}", e.getMessage());
        }
    }

    /**
     * Example 4: Demonstrates resource management best practices
     */
    private static void demonstrateResourceManagement() {
        logger.info("\n=== Example 4: Resource Management ===");

        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl();

        // Demonstrate proper resource management with try-with-resources
        try (Connection connection = createTestConnection()) {

            // Create a runner with automatic resource management
            try (LiquibaseRunnerService liquibaseRunnerService = runner.createRunner(connection, "test.xml")) {

                logger.info("✓ Created LiquibaseRunner with automatic resource management");

                // Use the runner for multiple operations
                String status = liquibaseRunnerService.getStatus();
                logger.info("Status retrieved: {}", status.contains("Database URL"));

                boolean hasChanges = liquibaseRunnerService.hasUnrunChanges();
                logger.info("Has unrun changes: {}", hasChanges);

                // Validate changelog
                try {
                    liquibaseRunnerService.validate();
                    logger.info("✓ Changelog validation passed");
                } catch (LiquibaseValidationException e) {
                    logger.info("✓ Changelog validation failed as expected: {}", e.getMessage());
                }

                logger.info("✓ LiquibaseRunner operations completed");

            } // LiquibaseRunner automatically closed here

            logger.info("✓ Resources properly cleaned up");

        } catch (SQLException e) {
            logger.error("✗ Database connection error: {}", e.getMessage());
        }
    }

    /**
     * Example 5: Demonstrates thread safety considerations
     */
    private static void demonstrateThreadSafety() {
        logger.info("\n=== Example 5: Thread Safety ===");

        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            // Submit multiple concurrent tasks
            for (int i = 0; i < 3; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    try (Connection connection = createTestConnection()) {
                        String taskName = "Task-" + taskId;
                        logger.info("{}: Starting thread-safe operation", taskName);

                        // Each thread uses its own connection - this is thread-safe
                        String status = runner.getStatus(connection, "test-thread-" + taskId + ".xml");
                        logger.info("{}: Status operation completed", taskName);

                        // Check for unrun changes (read operation - thread-safe)
                        boolean hasChanges = runner.hasUnrunChanges(connection, "test-thread-" + taskId + ".xml");
                        logger.info("{}: Unrun changes check completed: {}", taskName, hasChanges);

                    } catch (Exception e) {
                        logger.warn("Task-{}: Operation failed (expected): {}", taskId, e.getMessage());
                    }
                });
            }

            // Wait for all tasks to complete
            executor.shutdown();
            if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.info("✓ All thread-safe operations completed successfully");
            } else {
                logger.warn("Some tasks didn't complete within timeout");
            }

        } catch (InterruptedException e) {
            logger.error("Thread execution interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demonstrates unsupported operations and proper error messages
     */
    private static void demonstrateUnsupportedOperations() {
        logger.info("\n=== Example 6: Unsupported Operations ===");

        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl();

        try (Connection connection = createTestConnection()) {

            // Demonstrate unsupported operation with clear error message
            try {
                runner.generateChangeLog(connection, "output.xml");
            } catch (UnsupportedOperationException e) {
                logger.info("✓ Caught expected unsupported operation: {}", e.getMessage());
                logger.info("✓ Error message clearly explains the limitation");
            }

        } catch (SQLException e) {
            logger.error("Database connection error: {}", e.getMessage());
        }
    }

    /**
     * Creates a test connection for demonstration purposes
     */
    private static Connection createTestConnection() throws SQLException {
        // In real usage, this would be your actual database connection
        return DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
    }

    /**
     * Mock implementation of DBConnectionManager for demonstration
     */
    private static class MockDBConnectionManager implements DBConnectionManager {
        @Override
        public Connection openConnection() throws SQLException {
            return createTestConnection();
        }
    }
}
