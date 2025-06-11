package com.quasarbyte.llm.codereview.sdk.model.parameter;

public class LlmTokensQuota {
    private Long completionTokens;
    private Long promptTokens;
    private Long totalTokens;

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public LlmTokensQuota setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public LlmTokensQuota setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public LlmTokensQuota setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }
}
