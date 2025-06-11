package com.quasarbyte.llm.codereview.sdk.exception;

public class ValidationException extends LLMCodeReviewRuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
