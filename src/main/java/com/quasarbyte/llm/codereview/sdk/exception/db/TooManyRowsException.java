package com.quasarbyte.llm.codereview.sdk.exception.db;

public class TooManyRowsException extends PersistenceRuntimeException {
    public TooManyRowsException(String message) {
        super(message);
    }

    public TooManyRowsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyRowsException(Throwable cause) {
        super(cause);
    }
}
