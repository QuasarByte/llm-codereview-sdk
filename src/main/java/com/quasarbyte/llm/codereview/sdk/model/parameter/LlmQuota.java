package com.quasarbyte.llm.codereview.sdk.model.parameter;

public class LlmQuota {
    private Long requestQuota;

    public Long getRequestQuota() {
        return requestQuota;
    }

    public LlmQuota setRequestQuota(Long requestQuota) {
        this.requestQuota = requestQuota;
        return this;
    }
}
