package com.quasarbyte.llm.codereview.sdk.exception.liquibase;

/**
 * Exception thrown when database connection issues occur during Liquibase operations.
 * This includes connection timeouts, database unavailability, and SQL errors.
 */
public class LiquibaseConnectionException extends LiquibaseException {
    
    public LiquibaseConnectionException(String message) {
        super(message);
    }

    public LiquibaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseConnectionException(Throwable cause) {
        super(cause);
    }
}
