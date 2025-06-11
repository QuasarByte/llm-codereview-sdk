package com.quasarbyte.llm.codereview.sdk.exception;

public class TaskExecutorException extends LLMCodeReviewRuntimeException {
    public TaskExecutorException(String message) {
        super(message);
    }

    public TaskExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutorException(Throwable cause) {
        super(cause);
    }
}
