package com.quasarbyte.llm.codereview.sdk.exception;

public class TaskExecutorCancellationException extends TaskExecutorException {
    public TaskExecutorCancellationException(String message) {
        super(message);
    }

    public TaskExecutorCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutorCancellationException(Throwable cause) {
        super(cause);
    }
}
