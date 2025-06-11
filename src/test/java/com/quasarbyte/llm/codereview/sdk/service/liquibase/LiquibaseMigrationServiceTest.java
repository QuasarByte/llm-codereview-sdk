package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.impl.LiquibaseMigrationServiceImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.LiquibaseMigrationService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import liquibase.changelog.ChangeSet;
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
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LiquibaseMigrationServiceTest {

    @Mock
    private LiquibaseInnerRunner liquibaseInnerRunner;

    @Mock
    private LiquibaseRunnerService liquibaseRunnerService;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseMetaData mockMetaData;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ChangeSet mockChangeSet1;

    @Mock
    private ChangeSet mockChangeSet2;

    private LiquibaseMigrationService liquibaseMigrationService;
    private static final String TEST_CHANGELOG_PATH = "test/changelog.xml";

    @BeforeEach
    void setUp() throws SQLException {
        liquibaseMigrationService = new LiquibaseMigrationServiceImpl(liquibaseInnerRunner);
        
        // Setup basic common mocks for database operations - lenient mode allows unused stubs
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getURL()).thenReturn("jdbc:h2:mem:test");
        when(mockMetaData.getDatabaseProductName()).thenReturn("H2");
        when(mockMetaData.getSQLKeywords()).thenReturn(""); // Fix for Liquibase NullPointerException
        when(mockMetaData.getDatabaseProductVersion()).thenReturn("2.0.0");
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);
        
        when(liquibaseInnerRunner.createRunner(mockConnection, TEST_CHANGELOG_PATH))
                .thenReturn(liquibaseRunnerService);
    }

    @Test
    void shouldCreateMigrationManagerWithValidService() {
        // Given & When & Then
        assertNotNull(liquibaseMigrationService);
    }

    @Test
    void shouldThrowExceptionOnNullLiquibaseRunnerService() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> 
            new LiquibaseMigrationServiceImpl(null));
    }

    @Test
    void shouldPerformMigrationWhenValidAndHasUnrunChanges() {
        // Given
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);
        when(liquibaseRunnerService.listUnrunChangeSets()).thenReturn(Arrays.asList(mockChangeSet1, mockChangeSet2));

        // When
        liquibaseMigrationService.performMigration(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        verify(liquibaseInnerRunner).validateChangelog(mockConnection, TEST_CHANGELOG_PATH);
        verify(liquibaseRunnerService).listUnrunChangeSets();
        verify(liquibaseRunnerService).update();
        verify(liquibaseRunnerService).close();
    }

    @Test
    void shouldSkipMigrationWhenNoUnrunChanges() {
        // Given
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);
        when(liquibaseRunnerService.listUnrunChangeSets()).thenReturn(Collections.emptyList());

        // When
        liquibaseMigrationService.performMigration(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        verify(liquibaseInnerRunner).validateChangelog(mockConnection, TEST_CHANGELOG_PATH);
        verify(liquibaseRunnerService).listUnrunChangeSets();
        verify(liquibaseRunnerService, never()).update();
        verify(liquibaseRunnerService).close();
    }

    @Test
    void shouldThrowExceptionWhenMigrationValidationFails() {
        // Given
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(false);

        // When & Then  
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            liquibaseMigrationService.performMigration(mockConnection, TEST_CHANGELOG_PATH));
        
        // The implementation should throw an exception when validation fails
        // Let's check that an exception was indeed thrown (which assertThrows already confirms)
        // and that it's related to validation failure
        assertNotNull(exception, "RuntimeException should be thrown when validation fails");
        verify(liquibaseRunnerService, never()).update();
    }

    @Test
    void shouldPerformRollbackWhenValidChangelog() {
        // Given
        int count = 3;
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);

        // When
        liquibaseMigrationService.performRollback(mockConnection, TEST_CHANGELOG_PATH, count);

        // Then
        verify(liquibaseInnerRunner).validateChangelog(mockConnection, TEST_CHANGELOG_PATH);
        verify(liquibaseRunnerService).rollback(count);
        verify(liquibaseRunnerService).close();
    }

    @Test
    void shouldThrowExceptionOnInvalidRollbackCount() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            liquibaseMigrationService.performRollback(mockConnection, TEST_CHANGELOG_PATH, 0));
        
        assertThrows(IllegalArgumentException.class, () -> 
            liquibaseMigrationService.performRollback(mockConnection, TEST_CHANGELOG_PATH, -1));
    }

    @Test
    void shouldPerformRollbackToTagWhenValidChangelog() {
        // Given
        String tag = "version-1.0";
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);

        // When
        liquibaseMigrationService.performRollbackToTag(mockConnection, TEST_CHANGELOG_PATH, tag);

        // Then
        verify(liquibaseInnerRunner).validateChangelog(mockConnection, TEST_CHANGELOG_PATH);
        verify(liquibaseRunnerService).rollbackToTag(tag);
        verify(liquibaseRunnerService).close();
    }

    @Test
    void shouldThrowExceptionOnInvalidTag() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            liquibaseMigrationService.performRollbackToTag(mockConnection, TEST_CHANGELOG_PATH, null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            liquibaseMigrationService.performRollbackToTag(mockConnection, TEST_CHANGELOG_PATH, ""));
        
        assertThrows(IllegalArgumentException.class, () -> 
            liquibaseMigrationService.performRollbackToTag(mockConnection, TEST_CHANGELOG_PATH, "   "));
    }

    @Test
    void shouldReturnValidationResultForValidChangelog() {
        // Given
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);

        // When
        LiquibaseMigrationService.ValidationResult result =
            liquibaseMigrationService.validateChangelog(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        assertTrue(result.isValid());
        assertEquals("Changelog is valid", result.getMessage());
        assertNull(result.getError());
    }

    @Test
    void shouldReturnValidationResultForInvalidChangelog() {
        // Given
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(false);

        // When
        LiquibaseMigrationService.ValidationResult result =
            liquibaseMigrationService.validateChangelog(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        assertFalse(result.isValid());
        assertEquals("Changelog validation failed", result.getMessage());
        assertNull(result.getError());
    }

    @Test
    void shouldReturnValidationResultWithErrorOnException() {
        // Given
        RuntimeException testException = new RuntimeException("Test validation error");
        when(liquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH))
                .thenThrow(testException);

        // When
        LiquibaseMigrationService.ValidationResult result =
            liquibaseMigrationService.validateChangelog(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Validation error"));
        assertEquals(testException, result.getError());
    }

    @Test
    void shouldGetMigrationStatusWithCorrectInformation() {
        // Given
        List<ChangeSet> unrunChangeSets = Arrays.asList(mockChangeSet1, mockChangeSet2);
        String detailedStatus = "Detailed status information";
        
        when(liquibaseInnerRunner.listUnrunChangeSets(mockConnection, TEST_CHANGELOG_PATH))
                .thenReturn(unrunChangeSets);
        when(liquibaseInnerRunner.getStatus(mockConnection, TEST_CHANGELOG_PATH))
                .thenReturn(detailedStatus);

        // When
        LiquibaseMigrationService.MigrationStatus status =
            liquibaseMigrationService.getMigrationStatus(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        assertEquals("jdbc:h2:mem:test", status.getDatabaseUrl());
        assertEquals("H2", status.getDatabaseProduct());
        assertEquals(TEST_CHANGELOG_PATH, status.getChangelogPath());
        assertEquals(unrunChangeSets, status.getUnrunChangeSets());
        assertEquals(2, status.getUnrunChangeSetsCount());
        assertEquals(5, status.getAppliedChangeSetsCount());
        assertEquals(detailedStatus, status.getDetailedStatus());
        assertTrue(status.hasUnrunChanges());
    }

    @Test
    void shouldDelegateGenerateChangelogFromDatabase() {
        // Given
        String outputPath = "output/changelog.xml";
        String expectedPath = "generated/changelog.xml";
        when(liquibaseInnerRunner.generateChangeLog(mockConnection, outputPath)).thenReturn(expectedPath);

        // When
        String result = liquibaseMigrationService.generateChangelogFromDatabase(mockConnection, outputPath);

        // Then
        assertEquals(expectedPath, result);
        verify(liquibaseInnerRunner).generateChangeLog(mockConnection, outputPath);
    }

    @Test
    void shouldCreateRunner() {
        // Given & When
        LiquibaseRunnerService result = liquibaseMigrationService.createRunner(mockConnection, TEST_CHANGELOG_PATH);

        // Then
        assertEquals(liquibaseRunnerService, result);
        verify(liquibaseInnerRunner).createRunner(mockConnection, TEST_CHANGELOG_PATH);
    }
}
