package com.quasarbyte.llm.codereview.sdk.exception.db;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewException;

public class PersistenceException extends LLMCodeReviewException {
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
