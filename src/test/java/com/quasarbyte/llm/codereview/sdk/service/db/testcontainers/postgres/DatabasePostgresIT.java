package com.quasarbyte.llm.codereview.sdk.service.db.testcontainers.postgres;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class DatabasePostgresIT {

    // Use PostgreSQL docker image
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16.2")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Test
    void simpleQueryTest() throws Exception {
        try (
                Connection conn = DriverManager.getConnection(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                );
                Statement stmt = conn.createStatement()) {

            printContainerInfo(postgres);

            stmt.execute("CREATE TABLE demo(id INT PRIMARY KEY, val VARCHAR(32));");
            stmt.execute("INSERT INTO demo VALUES (1, 'hello')");
            ResultSet rs = stmt.executeQuery("SELECT val FROM demo WHERE id = 1");
            if (rs.next()) {
                String val = rs.getString(1);
                assertEquals("hello", val);
            }
        }
    }

    private static void printContainerInfo(org.testcontainers.containers.JdbcDatabaseContainer<?> container) {
        System.out.println("--------------------------------------------------");
        System.out.println("JDBC URL:   " + container.getJdbcUrl());
        System.out.println("Username:   " + container.getUsername());
        System.out.println("Password:   " + container.getPassword());
        System.out.println("Host:       " + container.getHost());
        System.out.println("Port:       " + container.getFirstMappedPort());
        System.out.println("Database:   " + container.getDatabaseName());
        System.out.println("--------------------------------------------------");
    }
}
