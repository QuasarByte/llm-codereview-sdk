package com.quasarbyte.llm.codereview.sdk.model.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SupportedDatabaseDriver Tests")
class SupportedDatabaseDriverTest {

    @Test
    @DisplayName("Should find SQLite driver by class name")
    void shouldFindSQLiteDriverByClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("org.sqlite.JDBC");

        // Then
        assertNotNull(driver);
        assertEquals(SupportedDatabaseDriver.SQLITE, driver);
        assertEquals("org.sqlite.JDBC", driver.getDriverClassName());
        assertEquals("SQLite", driver.getDisplayName());
    }

    @Test
    @DisplayName("Should find H2 driver by class name")
    void shouldFindH2DriverByClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("org.h2.Driver");

        // Then
        assertNotNull(driver);
        assertEquals(SupportedDatabaseDriver.H2, driver);
        assertEquals("org.h2.Driver", driver.getDriverClassName());
        assertEquals("H2", driver.getDisplayName());
    }

    @Test
    @DisplayName("Should find PostgreSQL driver by class name")
    void shouldFindPostgreSQLDriverByClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("org.postgresql.Driver");

        // Then
        assertNotNull(driver);
        assertEquals(SupportedDatabaseDriver.POSTGRESQL, driver);
        assertEquals("org.postgresql.Driver", driver.getDriverClassName());
        assertEquals("PostgreSQL", driver.getDisplayName());
    }

    @Test
    @DisplayName("Should find MySQL driver by class name")
    void shouldFindMySQLDriverByClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("com.mysql.cj.jdbc.Driver");

        // Then
        assertNotNull(driver);
        assertEquals(SupportedDatabaseDriver.MYSQL, driver);
        assertEquals("com.mysql.cj.jdbc.Driver", driver.getDriverClassName());
        assertEquals("MySQL", driver.getDisplayName());
    }

    @Test
    @DisplayName("Should return null for non-existent driver class name")
    void shouldReturnNullForNonExistentDriverClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("com.nonexistent.Driver");

        // Then
        assertNull(driver);
    }

    @Test
    @DisplayName("Should return null for null driver class name")
    void shouldReturnNullForNullDriverClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName(null);

        // Then
        assertNull(driver);
    }

    @Test
    @DisplayName("Should return null for empty driver class name")
    void shouldReturnNullForEmptyDriverClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("");

        // Then
        assertNull(driver);
    }

    @Test
    @DisplayName("Should handle whitespace in driver class name")
    void shouldHandleWhitespaceInDriverClassName() {
        // When
        SupportedDatabaseDriver driver = SupportedDatabaseDriver.findByDriverClassName("  org.sqlite.JDBC  ");

        // Then
        assertNotNull(driver);
        assertEquals(SupportedDatabaseDriver.SQLITE, driver);
    }

    @Test
    @DisplayName("Should correctly identify supported drivers")
    void shouldCorrectlyIdentifySupportedDrivers() {
        // Test supported drivers
        assertTrue(SupportedDatabaseDriver.isSupported("org.sqlite.JDBC"));
        assertTrue(SupportedDatabaseDriver.isSupported("org.h2.Driver"));
        assertTrue(SupportedDatabaseDriver.isSupported("org.postgresql.Driver"));
        assertTrue(SupportedDatabaseDriver.isSupported("com.mysql.cj.jdbc.Driver"));
        assertTrue(SupportedDatabaseDriver.isSupported("org.mariadb.jdbc.Driver"));
        assertTrue(SupportedDatabaseDriver.isSupported("oracle.jdbc.OracleDriver"));
        assertTrue(SupportedDatabaseDriver.isSupported("com.microsoft.sqlserver.jdbc.SQLServerDriver"));

        // Test unsupported driver
        assertFalse(SupportedDatabaseDriver.isSupported("com.nonexistent.Driver"));
        assertFalse(SupportedDatabaseDriver.isSupported(null));
        assertFalse(SupportedDatabaseDriver.isSupported(""));
    }

    @Test
    @DisplayName("Should have all expected database drivers")
    void shouldHaveAllExpectedDatabaseDrivers() {
        // Verify all expected drivers are present
        SupportedDatabaseDriver[] drivers = SupportedDatabaseDriver.values();

        assertEquals(7, drivers.length);

        // Verify each driver has proper values
        for (SupportedDatabaseDriver driver : drivers) {
            assertNotNull(driver.getDriverClassName());
            assertNotNull(driver.getDisplayName());
            assertFalse(driver.getDriverClassName().isEmpty());
            assertFalse(driver.getDisplayName().isEmpty());
        }
    }
}
