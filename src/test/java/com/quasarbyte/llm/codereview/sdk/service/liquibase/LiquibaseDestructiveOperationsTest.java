package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl.LiquibaseMigrationManagerFactoryImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for destructive LiquibaseService operations
 * These tests are separated to avoid affecting the main test suite
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiquibaseDestructiveOperationsTest {

    @TempDir
    Path tempDir;

    private DBConnectionManager dbConnectionManager;
    private LiquibaseMigrationManager liquibaseMigrationManager;
    private String databaseUrl;

    @BeforeAll
    static void loadDriver() {
        // Load SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found. Add sqlite-jdbc dependency to pom.xml", e);
        }
    }

    @BeforeEach
    void setUp() {
        // Create temporary database file for each test
        File dbFile = tempDir.resolve("test-destructive.db").toFile();
        databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // Create connection manager
        dbConnectionManager = new TestDBConnectionManager(databaseUrl);

        // Create factory and service
        LiquibaseMigrationManagerFactory factory = new LiquibaseMigrationManagerFactoryImpl(dbConnectionManager);
        liquibaseMigrationManager = factory.create();
    }

    @Test
    @Order(1)
    @DisplayName("Should drop all database objects")
    void testDropAll() throws SQLException {
        // Given - run migrations first to create tables
        liquibaseMigrationManager.runMigrations();

        // Verify tables exist
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            assertTrue(connection.getMetaData()
                    .getTables(null, null, "review", null)
                    .next(), "Review table should exist before drop");
        }

        // When
        assertDoesNotThrow(() -> liquibaseMigrationManager.dropAll(),
                "DropAll should not throw exceptions");

        // Then - verify tables are dropped
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            assertFalse(connection.getMetaData()
                    .getTables(null, null, "review", null)
                    .next(), "Review table should not exist after drop");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should recreate database after drop all")
    void testRecreateAfterDropAll() {
        // Given
        liquibaseMigrationManager.runMigrations();
        liquibaseMigrationManager.dropAll();

        // When
        assertDoesNotThrow(() -> liquibaseMigrationManager.runMigrations(),
                "Should be able to recreate database after drop all");

        // Then
        assertFalse(liquibaseMigrationManager.hasUnrunChanges(),
                "Should have no unrun changes after recreation");
    }

    @Test
    @Order(3)
    @DisplayName("Should handle drop all on empty database")
    void testDropAllOnEmptyDatabase() {
        // Given - fresh database without migrations

        // When & Then
        assertDoesNotThrow(() -> liquibaseMigrationManager.dropAll(),
                "DropAll should handle empty database gracefully");
    }

    @Test
    @Order(4)
    @DisplayName("Should handle rollback after migrations")
    void testRollbackAfterMigrations() {
        // Given
        liquibaseMigrationManager.runMigrations();
        assertFalse(liquibaseMigrationManager.hasUnrunChanges(),
                "Should have no unrun changes after migration");

        // When & Then
        assertDoesNotThrow(() -> liquibaseMigrationManager.rollback(1),
                "Rollback should not throw exceptions");

        // Note: The actual effect of rollback depends on rollback instructions in changelog
        // Some changesets might not have rollback instructions, which is okay
    }

    @Test
    @Order(5)
    @DisplayName("Should handle multiple drop all operations")
    void testMultipleDropAll() {
        // Given
        liquibaseMigrationManager.runMigrations();

        // When & Then
        assertDoesNotThrow(() -> {
            liquibaseMigrationManager.dropAll();
            liquibaseMigrationManager.dropAll(); // Second drop all on empty database
            liquibaseMigrationManager.dropAll(); // Third drop all
        }, "Multiple drop all operations should be safe");
    }

    @Test
    @Order(6)
    @DisplayName("Should handle rollback on fresh database")
    void testRollbackOnFreshDatabase() {
        // Given - fresh database without any migrations

        // When & Then
        assertDoesNotThrow(() -> liquibaseMigrationManager.rollback(1),
                "Rollback on fresh database should not throw exceptions");
    }

    @Test
    @Order(7)
    @DisplayName("Should handle excessive rollback count")
    void testExcessiveRollbackCount() {
        // Given
        liquibaseMigrationManager.runMigrations();

        // When & Then
        assertDoesNotThrow(() -> liquibaseMigrationManager.rollback(999),
                "Excessive rollback count should be handled gracefully");
    }

    /**
     * Test implementation of DBConnectionManager for testing purposes
     */
    private static class TestDBConnectionManager implements DBConnectionManager {
        private final String url;

        public TestDBConnectionManager(String url) {
            this.url = url;
        }

        @Override
        public Connection openConnection() throws SQLException {
            return DriverManager.getConnection(url);
        }
    }
}