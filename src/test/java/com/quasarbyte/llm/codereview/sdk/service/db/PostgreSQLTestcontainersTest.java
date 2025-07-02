package com.quasarbyte.llm.codereview.sdk.service.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test using Testcontainers with annotations for PostgreSQL database
 */
@Testcontainers
@DisplayName("PostgreSQL Testcontainers Tests")
class PostgreSQLTestcontainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Test
    @DisplayName("Should connect to PostgreSQL container")
    void testPostgreSQLConnection() throws SQLException {
        // Given
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        // When & Then
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }
    }

    @Test
    @DisplayName("Should create and query table in PostgreSQL")
    void testCreateAndQueryTable() throws SQLException {
        // Given
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        // When & Then
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            // Create table
            statement.execute("CREATE TABLE IF NOT EXISTS test_table (id SERIAL PRIMARY KEY, name VARCHAR(255))");

            // Insert data
            statement.execute("INSERT INTO test_table (name) VALUES ('test_data')");

            // Query data
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM test_table");
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("count"));
        }
    }

    @Test
    @DisplayName("Should verify container is running")
    void testContainerIsRunning() {
        // Given & When & Then
        assertTrue(postgres.isRunning());
        assertTrue(postgres.isCreated());
        assertNotNull(postgres.getContainerIpAddress());
        assertTrue(postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) > 0);
    }

    @Test
    @DisplayName("Should perform CRUD operations")
    void testCrudOperations() throws SQLException {
        // Given
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Create table
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
            }

            // Insert (Create)
            try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
                pstmt.setString(1, "John Doe");
                pstmt.setString(2, "john@example.com");
                int rowsAffected = pstmt.executeUpdate();
                assertEquals(1, rowsAffected);
            }

            // Read
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM users WHERE name = 'John Doe'")) {
                assertTrue(rs.next());
                assertEquals("John Doe", rs.getString("name"));
                assertEquals("john@example.com", rs.getString("email"));
            }

            // Update
            try (PreparedStatement pstmt = connection.prepareStatement("UPDATE users SET email = ? WHERE name = ?")) {
                pstmt.setString(1, "john.doe@example.com");
                pstmt.setString(2, "John Doe");
                int rowsAffected = pstmt.executeUpdate();
                assertEquals(1, rowsAffected);
            }

            // Verify update
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT email FROM users WHERE name = 'John Doe'")) {
                assertTrue(rs.next());
                assertEquals("john.doe@example.com", rs.getString("email"));
            }

            // Delete
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM users WHERE name = ?")) {
                pstmt.setString(1, "John Doe");
                int rowsAffected = pstmt.executeUpdate();
                assertEquals(1, rowsAffected);
            }

            // Verify delete
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM users")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt("count"));
            }
        }
    }
}