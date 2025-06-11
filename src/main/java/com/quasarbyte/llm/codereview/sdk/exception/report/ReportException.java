package com.quasarbyte.llm.codereview.sdk.exception.report;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;

public class ReportException extends LLMCodeReviewRuntimeException {
    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
