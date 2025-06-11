package com.quasarbyte.llm.codereview.sdk.exception.db;

public class NotFoundException extends PersistenceRuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
