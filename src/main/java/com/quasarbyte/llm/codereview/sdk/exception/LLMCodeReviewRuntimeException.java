package com.quasarbyte.llm.codereview.sdk.exception;

public class LLMCodeReviewRuntimeException extends RuntimeException {
    public LLMCodeReviewRuntimeException(String message) {
        super(message);
    }

    public LLMCodeReviewRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LLMCodeReviewRuntimeException(Throwable cause) {
        super(cause);
    }
}
