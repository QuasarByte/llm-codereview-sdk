package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

public class ReviewResultDB {
    private Long id;
    private Long reviewId;
    private Long runId;
    private ReviewParameter reviewParameter;
    private ReviewResult reviewResult;

    public Long getId() {
        return id;
    }

    public ReviewResultDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ReviewResultDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public Long getRunId() {
        return runId;
    }

    public ReviewResultDB setRunId(Long runId) {
        this.runId = runId;
        return this;
    }

    public ReviewParameter getReviewParameter() {
        return reviewParameter;
    }

    public ReviewResultDB setReviewParameter(ReviewParameter reviewParameter) {
        this.reviewParameter = reviewParameter;
        return this;
    }

    public ReviewResult getReviewResult() {
        return reviewResult;
    }

    public ReviewResultDB setReviewResult(ReviewResult reviewResult) {
        this.reviewResult = reviewResult;
        return this;
    }
}
