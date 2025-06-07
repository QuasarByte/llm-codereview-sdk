package com.quasarbyte.llm.codereview.sdk.exception;

public class TaskExecutorTimeoutException extends TaskExecutorException {
    public TaskExecutorTimeoutException(String message) {
        super(message);
    }

    public TaskExecutorTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutorTimeoutException(Throwable cause) {
        super(cause);
    }
}
