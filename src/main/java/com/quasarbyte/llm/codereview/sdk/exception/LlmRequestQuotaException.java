package com.quasarbyte.llm.codereview.sdk.exception;

public class LlmRequestQuotaException extends LLMCodeReviewRuntimeException {
    public LlmRequestQuotaException(String message) {
        super(message);
    }
}
