package com.quasarbyte.llm.codereview.sdk.model.aggregated;

public class AggregatedCompletionUsage {
    private Long completionTokens;
    private Long promptTokens;
    private Long totalTokens;

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public AggregatedCompletionUsage setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public AggregatedCompletionUsage setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public AggregatedCompletionUsage setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }
}
