package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.LlmRequestQuotaGuard;

public class LlmRequestQuotaGuardImpl implements LlmRequestQuotaGuard {
    @Override
    public long plannedRequestValue(long rulesBatchesCount, long resolvedFilePathBatches, long quota) {
        if (rulesBatchesCount == 0) {
            return resolvedFilePathBatches;
        } else {
            return rulesBatchesCount * resolvedFilePathBatches;
        }
    }
}
