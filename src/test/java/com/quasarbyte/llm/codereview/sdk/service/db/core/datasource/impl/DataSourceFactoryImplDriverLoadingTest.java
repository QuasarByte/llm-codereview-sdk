package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl;

import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DataSourceFactory Driver Loading Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DataSourceFactoryImplDriverLoadingTest {

    private DataSourceFactoryImpl dataSourceFactory;
    private PersistenceConfigurationContext persistenceConfigurationContext;
    private String testDbName;
    private final List<DataSource> createdDataSources = new ArrayList<>();

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        persistenceConfigurationContext = mock(PersistenceConfigurationContext.class);
        dataSourceFactory = new DataSourceFactoryImpl(persistenceConfigurationContext);
        testDbName = "testdb_" + System.currentTimeMillis();
    }

    @AfterEach
    void tearDown() {
        // Clean up all created DataSources first
        for (DataSource dataSource : createdDataSources) {
            try {
                if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                    com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                    if (!hikariDataSource.isClosed()) {
                        hikariDataSource.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to close DataSource: " + e.getMessage());
            }
        }
        createdDataSources.clear();
        
        // Clean up H2 databases to prevent extension context issues
        try {
            // Force close and drop all H2 databases
            String[] dbsToClean = {
                "jdbc:h2:mem:" + testDbName + ";DROP_ALL_OBJECTS=TRUE;DB_CLOSE_ON_EXIT=TRUE",
                "jdbc:h2:mem:testdb;DROP_ALL_OBJECTS=TRUE;DB_CLOSE_ON_EXIT=TRUE"
            };
            
            for (String dbUrl : dbsToClean) {
                try (Connection conn = DriverManager.getConnection(dbUrl)) {
                    // Connection will auto-close and clean up
                } catch (Exception e) {
                    // Ignore cleanup errors - database may not exist
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        
        // Additional cleanup - try to shutdown H2 if it's still running
        try {
            // This helps ensure H2 releases all resources
            Class.forName("org.h2.Driver");
            java.sql.Driver h2Driver = DriverManager.getDriver("jdbc:h2:mem:test");
            if (h2Driver != null) {
                // Force H2 cleanup
                System.gc();
                Thread.sleep(100); // Give it a moment
            }
        } catch (Exception e) {
            // Ignore - H2 driver may not be loaded
        }
    }

    @Test
    @DisplayName("Should automatically load SQLite driver and create connection")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldAutoLoadSQLiteDriverAndCreateConnection() throws Exception {
        // Given
        File dbFile = tempDir.resolve("test.db").toFile();
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration()
                .setJdbcUrl(jdbcUrl)
                .setDriverClassName("org.sqlite.JDBC");

        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                .setDataSourceConfiguration(dataSourceConfig);

        when(persistenceConfigurationContext.getPersistenceConfiguration())
                .thenReturn(persistenceConfig);

        // When
        DataSource dataSource = dataSourceFactory.create();
        createdDataSources.add(dataSource); // Register for cleanup

        // Then
        assertNotNull(dataSource);

        // Verify we can actually get a connection
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    @DisplayName("Should automatically load H2 driver and create connection")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldAutoLoadH2DriverAndCreateConnection() throws Exception {
        // Given - Use unique database name and proper cleanup settings
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration()
                .setJdbcUrl("jdbc:h2:mem:" + testDbName + ";DB_CLOSE_ON_EXIT=TRUE")
                .setDriverClassName("org.h2.Driver")
                .setUsername("sa")
                .setPassword("");

        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                .setDataSourceConfiguration(dataSourceConfig);

        when(persistenceConfigurationContext.getPersistenceConfiguration())
                .thenReturn(persistenceConfig);

        // When
        DataSource dataSource = dataSourceFactory.create();
        createdDataSources.add(dataSource); // Register for cleanup

        // Then
        assertNotNull(dataSource);

        // Verify we can actually get a connection
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    @DisplayName("Should handle configuration without driver class name")
    void shouldHandleConfigurationWithoutDriverClassName() throws Exception {
        // Given - Use unique database name
        String uniqueDbName = "testdb_nodriver_" + System.currentTimeMillis();
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration()
                .setJdbcUrl("jdbc:h2:mem:" + uniqueDbName + ";DB_CLOSE_ON_EXIT=TRUE")
                .setUsername("sa")
                .setPassword("");
        // Note: No driver class name set

        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                .setDataSourceConfiguration(dataSourceConfig);

        when(persistenceConfigurationContext.getPersistenceConfiguration())
                .thenReturn(persistenceConfig);

        // When & Then
        assertDoesNotThrow(() -> {
            DataSource dataSource = dataSourceFactory.create();
            createdDataSources.add(dataSource); // Register for cleanup
            assertNotNull(dataSource);
            
            // Ensure we can get a connection and close it properly
            try (Connection connection = dataSource.getConnection()) {
                assertNotNull(connection);
            }
        });
    }

    @Test
    @DisplayName("Should handle SQLite in-memory database")
    void shouldHandleSQLiteInMemoryDatabase() throws Exception {
        // Given
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration()
                .setJdbcUrl("jdbc:sqlite::memory:")
                .setDriverClassName("org.sqlite.JDBC");

        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                .setDataSourceConfiguration(dataSourceConfig);

        when(persistenceConfigurationContext.getPersistenceConfiguration())
                .thenReturn(persistenceConfig);

        // When
        DataSource dataSource = dataSourceFactory.create();
        createdDataSources.add(dataSource); // Register for cleanup

        // Then
        assertNotNull(dataSource);

        // Verify we can actually get a connection and create a table
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());

            // Test that we can actually use the database
            connection.createStatement().execute("CREATE TABLE test (id INTEGER, name TEXT)");
            connection.createStatement().execute("INSERT INTO test VALUES (1, 'test')");
        }
    }

    @Test
    @DisplayName("Should fail gracefully for non-existent driver")
    void shouldFailGracefullyForNonExistentDriver() {
        // Given
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration()
                .setJdbcUrl("jdbc:nonexistent:test")
                .setDriverClassName("com.nonexistent.Driver");

        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration()
                .setDataSourceConfiguration(dataSourceConfig);

        when(persistenceConfigurationContext.getPersistenceConfiguration())
                .thenReturn(persistenceConfig);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> dataSourceFactory.create());
        assertTrue(exception.getMessage().contains("Failed to load database driver") ||
                exception.getCause().getMessage().contains("Failed to load database driver"));
    }
}
