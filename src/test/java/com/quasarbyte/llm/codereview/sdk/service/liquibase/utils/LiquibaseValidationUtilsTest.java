package com.quasarbyte.llm.codereview.sdk.service.liquibase.utils;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.util.LiquibaseValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LiquibaseValidationUtilsTest {

    @Mock
    private Connection mockConnection;

    @Test
    void shouldValidateValidConnection() {
        // Given & When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateConnection(mockConnection));
    }

    @Test
    void shouldThrowExceptionForNullConnection() {
        // Given & When & Then
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateConnection(null));
        assertEquals("Database connection cannot be null", exception.getMessage());
    }

    @Test
    void shouldValidateValidChangeLogPath() {
        // Given
        String validPath = "db/changelog.xml";
        
        // When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateChangeLogPath(validPath));
    }

    @Test
    void shouldThrowExceptionForNullChangeLogPath() {
        // Given & When & Then
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateChangeLogPath(null));
        assertEquals("ChangeLog path cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForEmptyChangeLogPath() {
        // Given
        String emptyPath = "   ";
        
        // When & Then
        LiquibaseValidationException exception = assertThrows(LiquibaseValidationException.class,
            () -> LiquibaseValidationUtils.validateChangeLogPath(emptyPath));
        assertEquals("ChangeLog path cannot be empty", exception.getMessage());
    }

    @Test
    void shouldValidatePositiveRollbackCount() {
        // Given & When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateRollbackCount(1));
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateRollbackCount(10));
    }

    @Test
    void shouldThrowExceptionForZeroRollbackCount() {
        // Given & When & Then
        LiquibaseValidationException exception = assertThrows(LiquibaseValidationException.class,
            () -> LiquibaseValidationUtils.validateRollbackCount(0));
        assertEquals("Rollback count must be positive, got: 0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForNegativeRollbackCount() {
        // Given & When & Then
        LiquibaseValidationException exception = assertThrows(LiquibaseValidationException.class,
            () -> LiquibaseValidationUtils.validateRollbackCount(-1));
        assertEquals("Rollback count must be positive, got: -1", exception.getMessage());
    }

    @Test
    void shouldValidateValidTag() {
        // Given
        String validTag = "version-1.0";
        
        // When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateTag(validTag));
    }

    @Test
    void shouldThrowExceptionForNullTag() {
        // Given & When & Then
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateTag(null));
        assertEquals("Tag cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForEmptyTag() {
        // Given
        String emptyTag = "   ";
        
        // When & Then
        LiquibaseValidationException exception = assertThrows(LiquibaseValidationException.class,
            () -> LiquibaseValidationUtils.validateTag(emptyTag));
        assertEquals("Tag cannot be empty", exception.getMessage());
    }

    @Test
    void shouldValidateValidOutputPath() {
        // Given
        String validPath = "output/changelog.xml";
        
        // When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateOutputPath(validPath));
    }

    @Test
    void shouldThrowExceptionForNullOutputPath() {
        // Given & When & Then
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateOutputPath(null));
        assertEquals("Output path cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForEmptyOutputPath() {
        // Given
        String emptyPath = "   ";
        
        // When & Then
        LiquibaseValidationException exception = assertThrows(LiquibaseValidationException.class,
            () -> LiquibaseValidationUtils.validateOutputPath(emptyPath));
        assertEquals("Output path cannot be empty", exception.getMessage());
    }

    @Test
    void shouldValidateBasicParameters() {
        // Given
        String validPath = "db/changelog.xml";
        
        // When & Then
        assertDoesNotThrow(() -> LiquibaseValidationUtils.validateBasicParameters(mockConnection, validPath));
    }

    @Test
    void shouldThrowExceptionForInvalidBasicParameters() {
        // Given & When & Then
        assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateBasicParameters(null, "valid.xml"));
        assertThrows(NullPointerException.class, 
            () -> LiquibaseValidationUtils.validateBasicParameters(mockConnection, null));
    }
}
