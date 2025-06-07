package com.quasarbyte.llm.codereview.sdk.model.review;

public class ReviewCompletionUsage {
    private Long completionTokens;
    private Long promptTokens;
    private Long totalTokens;

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public ReviewCompletionUsage setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
        return this;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public ReviewCompletionUsage setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
        return this;
    }

    public Long getTotalTokens() {
        return totalTokens;
    }

    public ReviewCompletionUsage setTotalTokens(Long totalTokens) {
        this.totalTokens = totalTokens;
        return this;
    }
}
