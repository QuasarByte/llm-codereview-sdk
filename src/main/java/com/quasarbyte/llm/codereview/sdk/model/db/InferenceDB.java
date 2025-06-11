package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItemJson;

public class InferenceDB {
    private Long id;
    private Long runId;
    private Long reviewId;
    private Long promptId;
    private InferenceStatusEnum status;
    private ReviewedResultItemJson reviewedResultItem;

    public Long getId() {
        return id;
    }

    public InferenceDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getRunId() {
        return runId;
    }

    public InferenceDB setRunId(Long runId) {
        this.runId = runId;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public InferenceDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public Long getPromptId() {
        return promptId;
    }

    public InferenceDB setPromptId(Long promptId) {
        this.promptId = promptId;
        return this;
    }

    public InferenceStatusEnum getStatus() {
        return status;
    }

    public InferenceDB setStatus(InferenceStatusEnum status) {
        this.status = status;
        return this;
    }

    public ReviewedResultItemJson getReviewedResultItem() {
        return reviewedResultItem;
    }

    public InferenceDB setReviewedResultItem(ReviewedResultItemJson reviewedResultItem) {
        this.reviewedResultItem = reviewedResultItem;
        return this;
    }
}
