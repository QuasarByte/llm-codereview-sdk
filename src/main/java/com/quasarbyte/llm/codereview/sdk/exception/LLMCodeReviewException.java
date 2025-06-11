package com.quasarbyte.llm.codereview.sdk.exception;

public class LLMCodeReviewException extends Exception {
    public LLMCodeReviewException(String message) {
        super(message);
    }

    public LLMCodeReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public LLMCodeReviewException(Throwable cause) {
        super(cause);
    }
}
