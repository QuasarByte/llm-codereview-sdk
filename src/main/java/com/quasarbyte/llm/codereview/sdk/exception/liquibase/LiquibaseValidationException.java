package com.quasarbyte.llm.codereview.sdk.exception.liquibase;

/**
 * Exception thrown when changelog validation fails or 
 * when invalid parameters are provided to Liquibase operations.
 */
public class LiquibaseValidationException extends LiquibaseException {
    
    public LiquibaseValidationException(String message) {
        super(message);
    }

    public LiquibaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseValidationException(Throwable cause) {
        super(cause);
    }
}
