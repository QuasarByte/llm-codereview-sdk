package com.quasarbyte.llm.codereview.sdk.liquibase;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl.LiquibaseMigrationManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunnerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerFactoryImpl;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Example class demonstrating various usage patterns of the improved Liquibase architecture.
 * Shows both traditional LiquibaseService usage and new flexible LiquibaseRunnerService usage.
 */
public class LiquibaseUsageExample {

    public static void main(String[] args) {
        // Mock connection manager for demonstration
        DBConnectionManager connectionManager = new MockDBConnectionManager();

        // Example 1: Traditional usage with LiquibaseService
        traditionalUsageExample(connectionManager);

        // Example 2: Flexible usage with LiquibaseRunnerService
        flexibleUsageExample();

        // Example 3: Custom configuration usage
        customConfigurationExample(connectionManager);

        // Example 4: Direct connection usage
        directConnectionExample();
    }

    /**
     * Example 1: Traditional usage pattern with LiquibaseService
     */
    private static void traditionalUsageExample(DBConnectionManager connectionManager) {
        System.out.println("=== Example 1: Traditional LiquibaseService Usage ===");

        // Create service using factory
        LiquibaseMigrationManagerFactory factory = new LiquibaseMigrationManagerFactoryImpl(connectionManager);
        LiquibaseMigrationManager service = factory.create();

        try {
            // Check for pending migrations
            if (service.hasUnrunChanges()) {
                System.out.println("Found pending migrations, running them...");
                service.runMigrations();
                System.out.println("Migrations completed successfully!");
            } else {
                System.out.println("No pending migrations found.");
            }

            // Get status
            String status = service.getStatus();
            System.out.println("Migration Status:\n" + status);

        } catch (RuntimeException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }

    /**
     * Example 2: Flexible usage with LiquibaseRunnerService and external connection
     */
    private static void flexibleUsageExample() {
        System.out.println("\n=== Example 2: Flexible LiquibaseRunnerService Usage ===");

        // Create runner service
        LiquibaseInnerRunnerFactory factory = new LiquibaseInnerRunnerFactoryImpl();
        LiquibaseInnerRunner runner = factory.create();

        // Use with external connection and custom changelog
        try (Connection connection = createTestConnection()) {
            String customChangelogPath = "custom/specific-changelog.xml";

            // Run migrations on specific changelog
            System.out.println("Running migrations from custom changelog...");
            runner.runMigrations(connection, customChangelogPath);

            // Check status
            String status = runner.getStatus(connection, customChangelogPath);
            System.out.println("Custom Changelog Status:\n" + status);

        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }

    /**
     * Example 3: Usage with custom configuration
     */
    private static void customConfigurationExample(DBConnectionManager connectionManager) {
        System.out.println("\n=== Example 3: Custom Configuration Usage ===");

        // Create custom configuration
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath("environments/production-changelog.xml")
                .contexts("production,hotfix")
                .labels("version-2.0")
                .resourceAccessor(new ClassLoaderResourceAccessor())
                .build();

        // Create service with custom config
        LiquibaseMigrationManagerFactory factory = new LiquibaseMigrationManagerFactoryImpl(connectionManager);
        LiquibaseMigrationManager service = factory.create(config);

        try {
            // Validate changelog first
            if (service.validateChangelog()) {
                System.out.println("Changelog validation passed.");

                // Run migrations with production contexts
                service.runMigrations();
                System.out.println("Production migrations completed!");
            } else {
                System.err.println("Changelog validation failed!");
            }

        } catch (RuntimeException e) {
            System.err.println("Production migration failed: " + e.getMessage());
        }
    }

    /**
     * Example 4: Direct connection usage for maximum flexibility
     */
    private static void directConnectionExample() {
        System.out.println("\n=== Example 4: Direct Connection Usage ===");

        // Create runner with specific resource accessor
        LiquibaseInnerRunnerFactory factory = new LiquibaseInnerRunnerFactoryImpl();
        LiquibaseInnerRunner runner = factory.create(new ClassLoaderResourceAccessor());

        // Use multiple connections and changelogs
        try {
            Connection devConnection = createTestConnection();
            Connection testConnection = createTestConnection();

            // Run different migrations on different databases
            runner.runMigrations(devConnection, "environments/development-changelog.xml");
            System.out.println("Development database migrated.");

            runner.runMigrations(testConnection, "environments/test-changelog.xml");
            System.out.println("Test database migrated.");

            // Compare statuses
            String devStatus = runner.getStatus(devConnection, "environments/development-changelog.xml");
            String testStatus = runner.getStatus(testConnection, "environments/test-changelog.xml");

            System.out.println("Development Status:\n" + devStatus);
            System.out.println("Test Status:\n" + testStatus);

            devConnection.close();
            testConnection.close();

        } catch (SQLException e) {
            System.err.println("Database operations failed: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }

    /**
     * Creates a mock test connection for demonstration purposes
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
