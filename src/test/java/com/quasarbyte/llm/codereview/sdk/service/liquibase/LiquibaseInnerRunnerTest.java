package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LiquibaseInnerRunnerTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseMetaData mockMetaData;

    private LiquibaseInnerRunner liquibaseInnerRunner;
    private static final String TEST_CHANGELOG_PATH = "test/changelog.xml";

    @BeforeEach
    void setUp() throws SQLException {
        LiquibaseConfig config = LiquibaseConfig.builder()
                .resourceAccessor(new ClassLoaderResourceAccessor())
                .changeLogPath("com/quasarbyte/llm/codereview/sdk/liquibase/changelog/test-changelog.xml") // Use a test-specific path
                .build();
        
        liquibaseInnerRunner = new LiquibaseInnerRunnerImpl(config);
    }

    @Test
    void shouldCreateLiquibaseRunnerWithDefaultConfig() {
        // Given & When
        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl();
        
        // Then
        assertNotNull(runner);
    }

    @Test
    void shouldCreateLiquibaseRunnerWithCustomConfig() {
        // Given
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath("custom/path.xml")
                .contexts("test")
                .labels("version1")
                .build();
        
        // When
        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl(config);
        
        // Then
        assertNotNull(runner);
    }

    @Test
    void shouldThrowExceptionOnNullConnection() {
        // Given & When & Then
        assertThrows(Exception.class, () -> 
            liquibaseInnerRunner.runMigrations(null, TEST_CHANGELOG_PATH));
    }

    @Test
    void shouldThrowExceptionOnNullChangelogPath() {
        // Given & When & Then
        assertThrows(Exception.class, () -> 
            liquibaseInnerRunner.runMigrations(mockConnection, null));
    }

    @Test
    void shouldThrowExceptionOnNegativeRollbackCount() {
        // Given & When & Then
        assertThrows(LiquibaseValidationException.class, () ->
            liquibaseInnerRunner.rollback(mockConnection, TEST_CHANGELOG_PATH, -1));
    }

    @Test
    void shouldThrowExceptionOnZeroRollbackCount() {
        // Given & When & Then
        assertThrows(LiquibaseValidationException.class, () ->
            liquibaseInnerRunner.rollback(mockConnection, TEST_CHANGELOG_PATH, 0));
    }

    @Test
    void shouldHandleGetStatusWithMockConnection() throws SQLException {
        // Given - Mock connection with proper URL for Liquibase to work
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getURL()).thenReturn("jdbc:h2:mem:testdb");
        when(mockMetaData.getDatabaseProductName()).thenReturn("H2");
        when(mockMetaData.getDatabaseMajorVersion()).thenReturn(1);
        when(mockMetaData.getDatabaseMinorVersion()).thenReturn(4);
        
        // Mock createStatement to return null (which will cause the NPE in H2Database)
        when(mockConnection.createStatement()).thenReturn(null);
        
        // When using mock connections with incomplete setup, Liquibase operations will fail
        // This test ensures the method properly handles the failure case with a meaningful error
        Exception exception = assertThrows(Exception.class, () -> 
            liquibaseInnerRunner.getStatus(mockConnection, TEST_CHANGELOG_PATH),
            "getStatus should throw an exception when used with mock connection due to Liquibase limitations");
        
        // Then - Verify that we get a meaningful error message
        assertNotNull(exception.getMessage());
        // The exception should be wrapped in our custom LiquibaseMigrationException with proper error handling
        assertTrue(exception.getMessage().contains("Failed to get migration status") || 
                  exception.getMessage().contains("Invalid database connection") ||
                  exception.getMessage().contains("database-specific initialization"), 
                  "Exception message should indicate migration status failure or connection issues: " + exception.getMessage());
    }
}
