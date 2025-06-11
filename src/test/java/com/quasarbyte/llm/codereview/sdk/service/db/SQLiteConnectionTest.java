package com.quasarbyte.llm.codereview.sdk.service.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify SQLite JDBC driver is working correctly
 */
@DisplayName("SQLite Connection Tests")
class SQLiteConnectionTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void loadDriver() {
        // Explicitly load SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found. Please add sqlite-jdbc dependency to pom.xml", e);
        }
    }

    @Test
    @DisplayName("Should connect to SQLite database")
    void testSQLiteConnection() throws SQLException {
        // Given
        File dbFile = tempDir.resolve("test.db").toFile();
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // When & Then
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertEquals("SQLite", connection.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    @DisplayName("Should connect to in-memory SQLite database")
    void testInMemorySQLiteConnection() throws SQLException {
        // Given
        String jdbcUrl = "jdbc:sqlite::memory:";

        // When & Then
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertEquals("SQLite", connection.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    @DisplayName("Should create and query table")
    void testCreateAndQueryTable() throws SQLException {
        // Given
        String jdbcUrl = "jdbc:sqlite::memory:";

        // When & Then
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            // Create table
            statement.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT)");

            // Insert data
            statement.execute("INSERT INTO test_table (name) VALUES ('test')");

            // Query data
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM test_table");
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("count"));
        }
    }

    @Test
    @DisplayName("Should verify driver class is available")
    void testDriverClassAvailable() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            Class<?> driverClass = Class.forName("org.sqlite.JDBC");
            assertNotNull(driverClass);
            assertEquals("org.sqlite.JDBC", driverClass.getName());
        }, "SQLite JDBC driver class should be available");
    }
}