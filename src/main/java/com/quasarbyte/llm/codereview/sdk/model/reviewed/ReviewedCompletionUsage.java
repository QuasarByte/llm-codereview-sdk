package com.quasarbyte.llm.codereview.sdk.model.reviewed;

public class ReviewedCompletionUsage {
    private Long completionTokens;
    private Long promptTokens;
    private Long totalTokens;

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public ReviewedCompletionUsage setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public ReviewedCompletionUsage setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public ReviewedCompletionUsage setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }
}
