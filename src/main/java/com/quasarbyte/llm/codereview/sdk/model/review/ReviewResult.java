package com.quasarbyte.llm.codereview.sdk.model.review;

import java.util.List;

public class ReviewResult {
    private List<ReviewResultItem> items;
    private ReviewCompletionUsage completionUsage;

    public List<ReviewResultItem> getItems() {
        return items;
    }

    public ReviewResult setItems(List<ReviewResultItem> items) {
        this.items = items;
        return this;
    }

    public ReviewCompletionUsage getCompletionUsage() {
        return completionUsage;
    }

    public ReviewResult setCompletionUsage(ReviewCompletionUsage completionUsage) {
        this.completionUsage = completionUsage;
        return this;
    }
}
