package com.quasarbyte.llm.codereview.sdk.exception;

public class TaskExecutorInterruptedException extends LLMCodeReviewException {
    public TaskExecutorInterruptedException(String message) {
        super(message);
    }

    public TaskExecutorInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutorInterruptedException(Throwable cause) {
        super(cause);
    }
}
