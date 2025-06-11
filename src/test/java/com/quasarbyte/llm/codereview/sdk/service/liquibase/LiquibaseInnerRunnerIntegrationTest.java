package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests using real database connections.
 * These tests validate the complete functionality with an actual H2 database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiquibaseInnerRunnerIntegrationTest {

    @TempDir
    Path tempDir;

    private LiquibaseInnerRunner liquibaseInnerRunner;
    private String databaseUrl;
    private static final String TEST_CHANGELOG = "com/quasarbyte/llm/codereview/sdk/liquibase/changelog/test-changelog.xml";

    @BeforeAll
    static void loadDriver() {
        // Load H2 JDBC driver for integration testing
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC driver not found. Add H2 dependency for testing", e);
        }
    }

    @BeforeEach
    void setUp() {
        // Create a temporary database file for each test
        File dbFile = tempDir.resolve("integration-test.db").toFile();
        databaseUrl = "jdbc:h2:" + dbFile.getAbsolutePath() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

        // Create runner service with test configuration
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath("com/quasarbyte/llm/codereview/sdk/liquibase/changelog/integration-changelog.xml")
                .contexts("test")
                .build();

        liquibaseInnerRunner = new LiquibaseInnerRunnerImpl(config);
    }

    @Test
    @Order(1)
    @DisplayName("Should validate connection and changelog parameters")
    void testParameterValidation() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Test null connection validation
            assertThrows(NullPointerException.class, () ->
                    liquibaseInnerRunner.runMigrations(null, TEST_CHANGELOG));

            // Test null changelog validation  
            assertThrows(NullPointerException.class, () ->
                    liquibaseInnerRunner.runMigrations(connection, null));

            // Test empty changelog validation
            assertThrows(LiquibaseValidationException.class, () ->
                    liquibaseInnerRunner.runMigrations(connection, "   "));

            // Test invalid rollback count
            assertThrows(LiquibaseValidationException.class, () ->
                    liquibaseInnerRunner.rollback(connection, TEST_CHANGELOG, 0));

            assertThrows(LiquibaseValidationException.class, () ->
                    liquibaseInnerRunner.rollback(connection, TEST_CHANGELOG, -1));
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should handle non-existent changelog gracefully")
    void testNonExistentChangelog() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {
            String nonExistentChangelog = "non-existent-changelog.xml";

            // Should throw exception for non-existent changelog
            assertThrows(Exception.class, () ->
                    liquibaseInnerRunner.runMigrations(connection, nonExistentChangelog));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should create and validate changelog programmatically")
    void testProgrammaticChangelogCreation() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Create a simple changelog content for testing
            String simpleChangelog = "com/quasarbyte/llm/codereview/sdk/liquibase/changelog/test-simple.xml";

            // Test status on empty database
            String initialStatus = liquibaseInnerRunner.getStatus(connection, simpleChangelog);
            assertNotNull(initialStatus);
            assertTrue(initialStatus.contains("Database URL"));

            // Test has unrun changes on empty database - should handle gracefully
            assertDoesNotThrow(() ->
                    liquibaseInnerRunner.hasUnrunChanges(connection, simpleChangelog));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should handle rollback operations safely")
    void testRollbackOperations() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Test rollback on an empty database (should not crash)
            assertDoesNotThrow(() ->
                    liquibaseInnerRunner.rollback(connection, TEST_CHANGELOG, 1));

            // Test rollback to tag on an empty database (should throw expected exception)
            Exception exception = assertThrows(Exception.class, () ->
                    liquibaseInnerRunner.rollbackToTag(connection, TEST_CHANGELOG, "non-existent-tag"));

            // Verify it's the expected "tag not found" type of exception
            assertTrue(exception.getMessage().contains("tag") ||
                            exception.getMessage().contains("rollback") ||
                            exception.getCause() != null,
                    "Should throw a meaningful exception about missing tag");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should handle dropAll operation safely")
    void testDropAllOperation() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Test dropAll on an empty database (should not crash)
            assertDoesNotThrow(() ->
                    liquibaseInnerRunner.dropAll(connection, TEST_CHANGELOG));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle concurrent operations properly")
    void testConcurrentOperations() throws SQLException, InterruptedException {
        // Test thread safety by running multiple operations concurrently

        Thread[] threads = new Thread[3];
        Exception[] exceptions = new Exception[3];

        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // Each thread uses its own unique database to avoid conflicts
                String uniqueDbUrl = "jdbc:h2:" + tempDir.resolve("concurrent-test-" + threadId + ".db").toAbsolutePath() +
                        ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=REGULAR";
                try (Connection connection = DriverManager.getConnection(uniqueDbUrl, "sa", "")) {
                    // Test basic operations that should be thread-safe
                    String status = liquibaseInnerRunner.getStatus(connection, TEST_CHANGELOG);
                    assertNotNull(status);

                    // Test has unrun changes
                    boolean hasUnrun = liquibaseInnerRunner.hasUnrunChanges(connection, TEST_CHANGELOG);
                    // Should be true for a fresh database
                    assertTrue(hasUnrun, "Fresh database should have unrun changes");

                } catch (Exception e) {
                    exceptions[threadId] = e;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeouts
        }

        // Check that no exceptions occurred
        for (int i = 0; i < exceptions.length; i++) {
            if (exceptions[i] != null) {
                fail("Thread " + i + " threw exception: " + exceptions[i].getMessage());
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("Should handle database connection errors properly")
    void testConnectionErrorHandling() {
        String invalidUrl = "jdbc:h2:mem:invalid;MODE=BROKEN";

        assertThrows(Exception.class, () -> {
            try (Connection connection = DriverManager.getConnection(invalidUrl, "sa", "")) {
                liquibaseInnerRunner.getStatus(connection, TEST_CHANGELOG);
            }
        });
    }

    @Test
    @Order(8)
    @DisplayName("Should validate changelog format")
    void testChangelogValidation() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Test validation with non-existent changelog
            boolean isValid = liquibaseInnerRunner.validateChangelog(connection, "non-existent.xml");
            assertFalse(isValid, "Non-existent changelog should not be valid");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Should handle unsupported operations properly")
    void testUnsupportedOperations() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            // Test generateChangeLog (should throw UnsupportedOperationException)
            assertThrows(UnsupportedOperationException.class, () ->
                    liquibaseInnerRunner.generateChangeLog(connection, "output.xml"));
        }
    }

    @Test
    @Order(10)
    @DisplayName("Should provide meaningful error messages")
    void testErrorMessages() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl, "sa", "")) {

            try {
                liquibaseInnerRunner.rollback(connection, TEST_CHANGELOG, -5);
                fail("Should have thrown exception for negative rollback count");
            } catch (LiquibaseValidationException e) {
                assertTrue(e.getMessage().contains("-5"),
                        "Error message should contain the invalid value");
                assertTrue(e.getMessage().contains("positive"),
                        "Error message should explain the requirement");
            }
        }
    }

    @Test
    @Order(11)
    @DisplayName("Should test complete migration and rollback workflow")
    void testCompleteWorkflow() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl + "_workflow", "sa", "")) {

            // First, run migrations to set up the database
            assertDoesNotThrow(() ->
                    liquibaseInnerRunner.runMigrations(connection, TEST_CHANGELOG));

            // Verify migrations were applied
            assertTrue(liquibaseInnerRunner.hasUnrunChanges(connection, TEST_CHANGELOG) == false,
                    "After migration, there should be no unrun changes");

            // Test rollback of 1 changeset (should work now that we have data)
            assertDoesNotThrow(() ->
                    liquibaseInnerRunner.rollback(connection, TEST_CHANGELOG, 1));

            // Now we should have unrun changes again
            assertTrue(liquibaseInnerRunner.hasUnrunChanges(connection, TEST_CHANGELOG),
                    "After rollback, there should be unrun changes");
        }
    }
}
