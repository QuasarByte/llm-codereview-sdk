package com.quasarbyte.llm.codereview.sdk.exception.liquibase;

/**
 * Exception thrown when database migration operations fail.
 * This includes running migrations, rollbacks, and status checks.
 */
public class LiquibaseMigrationException extends LiquibaseException {
    
    public LiquibaseMigrationException(String message) {
        super(message);
    }

    public LiquibaseMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseMigrationException(Throwable cause) {
        super(cause);
    }
}
