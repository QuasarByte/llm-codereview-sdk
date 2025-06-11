package com.quasarbyte.llm.codereview.sdk.exception.db;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;

public class PersistenceRuntimeException extends LLMCodeReviewRuntimeException {
    public PersistenceRuntimeException(String message) {
        super(message);
    }

    public PersistenceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceRuntimeException(Throwable cause) {
        super(cause);
    }
}
