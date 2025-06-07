package com.quasarbyte.llm.codereview.sdk.exception;

public class LLMCodeReviewRhinoException extends LLMCodeReviewException {
    public LLMCodeReviewRhinoException(String message) {
        super(message);
    }

    public LLMCodeReviewRhinoException(String message, Throwable cause) {
        super(message, cause);
    }
}
