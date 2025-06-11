package com.quasarbyte.llm.codereview.sdk.exception;

public class JsonSerialisationException extends LLMCodeReviewRuntimeException {
    public JsonSerialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
