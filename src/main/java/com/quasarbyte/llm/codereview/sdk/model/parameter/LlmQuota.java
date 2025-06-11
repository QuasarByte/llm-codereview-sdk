package com.quasarbyte.llm.codereview.sdk.model.parameter;

public class LlmQuota {
    private Long requestQuota;

    private LlmTokensQuota tokensQuota;

    public Long getRequestQuota() {
        return requestQuota;
    }

    public LlmQuota setRequestQuota(Long requestQuota) {
        this.requestQuota = requestQuota;
        return this;
    }

    public LlmTokensQuota getTokensQuota() {
        return tokensQuota;
    }

    public LlmQuota setTokensQuota(LlmTokensQuota tokensQuota) {
        this.tokensQuota = tokensQuota;
        return this;
    }
}
