package com.quasarbyte.llm.codereview.sdk.service.liquibase;

import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.impl.LiquibaseRunnerServiceImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import liquibase.changelog.ChangeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquibaseRunnerServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private LiquibaseInnerRunner mockLiquibaseInnerRunner;

    @Mock
    private ChangeSet mockChangeSet1;

    @Mock
    private ChangeSet mockChangeSet2;

    private LiquibaseRunnerService liquibaseRunnerService;
    private static final String TEST_CHANGELOG_PATH = "test/changelog.xml";

    @BeforeEach
    void setUp() {
        liquibaseRunnerService = new LiquibaseRunnerServiceImpl(mockConnection, TEST_CHANGELOG_PATH, mockLiquibaseInnerRunner);
    }

    @Test
    void shouldCreateLiquibaseRunnerWithValidParameters() {
        // Given & When & Then
        assertNotNull(liquibaseRunnerService);
        assertEquals(TEST_CHANGELOG_PATH, liquibaseRunnerService.getChangeLogPath());
        assertEquals(mockConnection, liquibaseRunnerService.getConnection());
        assertFalse(((LiquibaseRunnerServiceImpl) liquibaseRunnerService).isClosed());
    }

    @Test
    void shouldThrowExceptionOnNullConnection() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> 
            new LiquibaseRunnerServiceImpl(null, TEST_CHANGELOG_PATH, mockLiquibaseInnerRunner));
    }

    @Test
    void shouldThrowExceptionOnNullChangelogPath() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> 
            new LiquibaseRunnerServiceImpl(mockConnection, null, mockLiquibaseInnerRunner));
    }

    @Test
    void shouldThrowExceptionOnNullLiquibaseRunnerService() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> 
            new LiquibaseRunnerServiceImpl(mockConnection, TEST_CHANGELOG_PATH, null));
    }

    @Test
    void shouldDelegateUpdateToLiquibaseRunnerService() {
        // Given & When
        liquibaseRunnerService.update();

        // Then
        verify(mockLiquibaseInnerRunner).runMigrations(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldDelegateRollbackToLiquibaseRunnerService() {
        // Given
        int count = 3;

        // When
        liquibaseRunnerService.rollback(count);

        // Then
        verify(mockLiquibaseInnerRunner).rollback(mockConnection, TEST_CHANGELOG_PATH, count);
    }

    @Test
    void shouldDelegateRollbackToTagToLiquibaseRunnerService() {
        // Given
        String tag = "version-1.0";

        // When
        liquibaseRunnerService.rollbackToTag(tag);

        // Then
        verify(mockLiquibaseInnerRunner).rollbackToTag(mockConnection, TEST_CHANGELOG_PATH, tag);
    }

    @Test
    void shouldDelegateValidateToLiquibaseRunnerService() {
        // Given
        when(mockLiquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> liquibaseRunnerService.validate());
        verify(mockLiquibaseInnerRunner).validateChangelog(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldThrowExceptionWhenValidationFails() {
        // Given
        when(mockLiquibaseInnerRunner.validateChangelog(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> liquibaseRunnerService.validate());
        assertTrue(exception.getMessage().contains("Changelog validation failed"));
    }

    @Test
    void shouldDelegateListUnrunChangeSetsToLiquibaseRunnerService() {
        // Given
        List<ChangeSet> expectedChangeSets = Arrays.asList(mockChangeSet1, mockChangeSet2);
        when(mockLiquibaseInnerRunner.listUnrunChangeSets(mockConnection, TEST_CHANGELOG_PATH))
                .thenReturn(expectedChangeSets);

        // When
        List<ChangeSet> result = liquibaseRunnerService.listUnrunChangeSets();

        // Then
        assertEquals(expectedChangeSets, result);
        verify(mockLiquibaseInnerRunner).listUnrunChangeSets(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldDelegateHasUnrunChangesToLiquibaseRunnerService() {
        // Given
        when(mockLiquibaseInnerRunner.hasUnrunChanges(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(true);

        // When
        boolean result = liquibaseRunnerService.hasUnrunChanges();

        // Then
        assertTrue(result);
        verify(mockLiquibaseInnerRunner).hasUnrunChanges(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldDelegateGetStatusToLiquibaseRunnerService() {
        // Given
        String expectedStatus = "Test status";
        when(mockLiquibaseInnerRunner.getStatus(mockConnection, TEST_CHANGELOG_PATH)).thenReturn(expectedStatus);

        // When
        String result = liquibaseRunnerService.getStatus();

        // Then
        assertEquals(expectedStatus, result);
        verify(mockLiquibaseInnerRunner).getStatus(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldDelegateDropAllToLiquibaseRunnerService() {
        // Given & When
        liquibaseRunnerService.dropAll();

        // Then
        verify(mockLiquibaseInnerRunner).dropAll(mockConnection, TEST_CHANGELOG_PATH);
    }

    @Test
    void shouldDelegateGenerateChangeLogToLiquibaseRunnerService() {
        // Given
        String outputPath = "output/changelog.xml";
        String expectedPath = "generated/changelog.xml";
        when(mockLiquibaseInnerRunner.generateChangeLog(mockConnection, outputPath)).thenReturn(expectedPath);

        // When
        String result = liquibaseRunnerService.generateChangeLog(outputPath);

        // Then
        assertEquals(expectedPath, result);
        verify(mockLiquibaseInnerRunner).generateChangeLog(mockConnection, outputPath);
    }

    @Test
    void shouldCloseSuccessfully() {
        // Given & When
        liquibaseRunnerService.close();

        // Then
        assertTrue(((LiquibaseRunnerServiceImpl) liquibaseRunnerService).isClosed());
    }

    @Test
    void shouldThrowExceptionWhenOperatingOnClosedRunner() {
        // Given
        liquibaseRunnerService.close();

        // When & Then
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.update());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.rollback(1));
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.rollbackToTag("tag"));
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.validate());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.listUnrunChangeSets());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.hasUnrunChanges());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.getStatus());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.dropAll());
        assertThrows(IllegalStateException.class, () -> liquibaseRunnerService.generateChangeLog("path"));
    }

    @Test
    void shouldAllowMultipleCloses() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            liquibaseRunnerService.close();
            liquibaseRunnerService.close();
            liquibaseRunnerService.close();
        });
    }
}
