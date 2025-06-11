package com.quasarbyte.llm.codereview.sdk.exception;

import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseConnectionException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseMigrationException;
import com.quasarbyte.llm.codereview.sdk.exception.liquibase.LiquibaseValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Liquibase exception hierarchy.
 * Validates proper inheritance and exception handling patterns.
 */
class LiquibaseExceptionTest {

    @Test
    void shouldCreateLiquibaseExceptionWithMessage() {
        // Given
        String message = "Test liquibase error";
        
        // When
        LiquibaseException exception = new LiquibaseException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof LLMCodeReviewRuntimeException);
    }

    @Test
    void shouldCreateLiquibaseExceptionWithMessageAndCause() {
        // Given
        String message = "Test liquibase error";
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        LiquibaseException exception = new LiquibaseException(message, cause);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldCreateLiquibaseExceptionWithCause() {
        // Given
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        LiquibaseException exception = new LiquibaseException(cause);
        
        // Then
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("Root cause"));
    }

    @Test
    void shouldCreateLiquibaseMigrationException() {
        // Given
        String message = "Migration failed";
        
        // When
        LiquibaseMigrationException exception = new LiquibaseMigrationException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LiquibaseException);
        assertTrue(exception instanceof LLMCodeReviewRuntimeException);
    }

    @Test
    void shouldCreateLiquibaseValidationException() {
        // Given
        String message = "Validation failed";
        
        // When
        LiquibaseValidationException exception = new LiquibaseValidationException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LiquibaseException);
        assertTrue(exception instanceof LLMCodeReviewRuntimeException);
    }

    @Test
    void shouldCreateLiquibaseConnectionException() {
        // Given
        String message = "Connection failed";
        
        // When
        LiquibaseConnectionException exception = new LiquibaseConnectionException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LiquibaseException);
        assertTrue(exception instanceof LLMCodeReviewRuntimeException);
    }

    @Test
    void shouldMaintainExceptionHierarchy() {
        // Given
        LiquibaseMigrationException migrationException = new LiquibaseMigrationException("Migration error");
        LiquibaseValidationException validationException = new LiquibaseValidationException("Validation error");
        LiquibaseConnectionException connectionException = new LiquibaseConnectionException("Connection error");
        
        // When & Then - All should be catchable as LiquibaseException
        assertTrue(migrationException instanceof LiquibaseException);
        assertTrue(validationException instanceof LiquibaseException);
        assertTrue(connectionException instanceof LiquibaseException);
        
        // All should be catchable as LLMCodeReviewException
        assertTrue(migrationException instanceof LLMCodeReviewRuntimeException);
        assertTrue(validationException instanceof LLMCodeReviewRuntimeException);
        assertTrue(connectionException instanceof LLMCodeReviewRuntimeException);
        
        // All should be catchable as RuntimeException
        assertTrue(migrationException instanceof RuntimeException);
        assertTrue(validationException instanceof RuntimeException);
        assertTrue(connectionException instanceof RuntimeException);
    }

    @Test
    void shouldAllowSpecificExceptionHandling() {
        // Given
        Exception[] exceptions = {
            new LiquibaseMigrationException("Migration failed"),
            new LiquibaseValidationException("Validation failed"),
            new LiquibaseConnectionException("Connection failed")
        };
        
        // When & Then
        for (Exception exception : exceptions) {
            assertDoesNotThrow(() -> {
                try {
                    throw exception;
                } catch (LiquibaseMigrationException e) {
                    assertEquals("Migration failed", e.getMessage());
                } catch (LiquibaseValidationException e) {
                    assertEquals("Validation failed", e.getMessage());
                } catch (LiquibaseConnectionException e) {
                    assertEquals("Connection failed", e.getMessage());
                }
            });
        }
    }

    @Test
    void shouldAllowGenericExceptionHandling() {
        // Given
        Exception[] exceptions = {
            new LiquibaseMigrationException("Migration failed"),
            new LiquibaseValidationException("Validation failed"),
            new LiquibaseConnectionException("Connection failed")
        };
        
        // When & Then - All should be catchable as generic LiquibaseException
        for (Exception exception : exceptions) {
            assertDoesNotThrow(() -> {
                try {
                    throw exception;
                } catch (LiquibaseException e) {
                    assertNotNull(e.getMessage());
                    assertTrue(e instanceof LLMCodeReviewRuntimeException);
                }
            });
        }
    }
}
