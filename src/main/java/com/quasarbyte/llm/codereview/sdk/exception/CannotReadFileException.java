package com.quasarbyte.llm.codereview.sdk.exception;

public class CannotReadFileException extends LLMCodeReviewException {
    public CannotReadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
