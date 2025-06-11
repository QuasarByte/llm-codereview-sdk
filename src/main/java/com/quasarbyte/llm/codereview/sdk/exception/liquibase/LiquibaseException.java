package com.quasarbyte.llm.codereview.sdk.exception.liquibase;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;

/**
 * Base exception for all Liquibase-related operations.
 * Follows the existing exception hierarchy pattern in the project.
 */
public class LiquibaseException extends LLMCodeReviewRuntimeException {
    
    public LiquibaseException(String message) {
        super(message);
    }

    public LiquibaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseException(Throwable cause) {
        super(cause);
    }
}
