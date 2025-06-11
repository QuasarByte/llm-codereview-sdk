package com.quasarbyte.llm.codereview.sdk.exception;

public class CannotReadResourceException extends LLMCodeReviewRuntimeException {
    public CannotReadResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
