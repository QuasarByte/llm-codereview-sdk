package com.quasarbyte.llm.codereview.sdk.exception;

public class LlmTokensQuotaException extends LLMCodeReviewRuntimeException {
    public LlmTokensQuotaException(String message) {
        super(message);
    }
}
