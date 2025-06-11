package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson;

public class PromptDB {
    private Long id;
    private Long reviewId;
    private ReviewPromptJson reviewPrompt;

    public Long getId() {
        return id;
    }

    public PromptDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public PromptDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public ReviewPromptJson getReviewPrompt() {
        return reviewPrompt;
    }

    public PromptDB setReviewPrompt(ReviewPromptJson reviewPrompt) {
        this.reviewPrompt = reviewPrompt;
        return this;
    }
}
