package com.quasarbyte.llm.codereview.sdk.service.db.driver.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceException;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.impl.DatabaseDriverLoaderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseDriverLoader Tests")
class DatabaseDriverLoaderImplTest {

    private DatabaseDriverLoaderImpl databaseDriverLoader;

    @BeforeEach
    void setUp() {
        databaseDriverLoader = new DatabaseDriverLoaderImpl();
    }

    @Test
    @DisplayName("Should successfully load SQLite driver")
    void shouldLoadSQLiteDriver() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("org.sqlite.JDBC")
                .setJdbcUrl("jdbc:sqlite::memory:");

        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(config));
        assertTrue(databaseDriverLoader.isDriverLoaded("org.sqlite.JDBC"));
    }

    @Test
    @DisplayName("Should successfully load H2 driver")
    void shouldLoadH2Driver() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("org.h2.Driver")
                .setJdbcUrl("jdbc:h2:mem:testdb");

        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(config));
        assertTrue(databaseDriverLoader.isDriverLoaded("org.h2.Driver"));
    }

    @Test
    @DisplayName("Should handle null configuration gracefully")
    void shouldHandleNullConfiguration() throws Exception {
        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(null));
    }

    @Test
    @DisplayName("Should handle null driver class name gracefully")
    void shouldHandleNullDriverClassName() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName(null)
                .setJdbcUrl("jdbc:sqlite::memory:");

        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(config));
    }

    @Test
    @DisplayName("Should handle empty driver class name gracefully")
    void shouldHandleEmptyDriverClassName() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("")
                .setJdbcUrl("jdbc:sqlite::memory:");

        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(config));
    }

    @Test
    @DisplayName("Should throw exception for non-existent driver")
    void shouldThrowExceptionForNonExistentDriver() {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("com.nonexistent.Driver")
                .setJdbcUrl("jdbc:nonexistent:test");

        // When & Then
        PersistenceException exception = assertThrows(PersistenceException.class,
                () -> databaseDriverLoader.loadDriver(config));

        assertTrue(exception.getMessage().contains("Failed to load database driver"));
        assertTrue(exception.getCause() instanceof ClassNotFoundException);
    }

    @Test
    @DisplayName("Should not reload already loaded driver")
    void shouldNotReloadAlreadyLoadedDriver() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("org.sqlite.JDBC")
                .setJdbcUrl("jdbc:sqlite::memory:");

        // When
        databaseDriverLoader.loadDriver(config);
        boolean firstLoadResult = databaseDriverLoader.isDriverLoaded("org.sqlite.JDBC");

        // Load again
        databaseDriverLoader.loadDriver(config);
        boolean secondLoadResult = databaseDriverLoader.isDriverLoaded("org.sqlite.JDBC");

        // Then
        assertTrue(firstLoadResult);
        assertTrue(secondLoadResult);
    }

    @Test
    @DisplayName("Should return false for null driver class name in isDriverLoaded")
    void shouldReturnFalseForNullDriverClassNameInIsDriverLoaded() {
        // When & Then
        assertFalse(databaseDriverLoader.isDriverLoaded(null));
        assertFalse(databaseDriverLoader.isDriverLoaded(""));
        assertFalse(databaseDriverLoader.isDriverLoaded("   "));
    }

    @Test
    @DisplayName("Should handle whitespace in driver class name")
    void shouldHandleWhitespaceInDriverClassName() throws Exception {
        // Given
        DataSourceConfiguration config = new DataSourceConfiguration()
                .setDriverClassName("  org.sqlite.JDBC  ")
                .setJdbcUrl("jdbc:sqlite::memory:");

        // When & Then
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(config));
        assertTrue(databaseDriverLoader.isDriverLoaded("org.sqlite.JDBC"));
        assertTrue(databaseDriverLoader.isDriverLoaded("  org.sqlite.JDBC  "));
    }

    @Test
    @DisplayName("Should recognize supported drivers")
    void shouldRecognizeSupportedDrivers() throws Exception {
        // Test SQLite
        DataSourceConfiguration sqliteConfig = new DataSourceConfiguration()
                .setDriverClassName("org.sqlite.JDBC");
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(sqliteConfig));

        // Test H2  
        DataSourceConfiguration h2Config = new DataSourceConfiguration()
                .setDriverClassName("org.h2.Driver");
        assertDoesNotThrow(() -> databaseDriverLoader.loadDriver(h2Config));
    }
}
