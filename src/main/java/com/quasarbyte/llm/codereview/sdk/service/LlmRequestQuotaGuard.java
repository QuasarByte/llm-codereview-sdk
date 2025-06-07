package com.quasarbyte.llm.codereview.sdk.service;

public interface LlmRequestQuotaGuard {
    long plannedRequestValue(long rulesBatchesCount, long resolvedFilePathBatches, long quota);
}
