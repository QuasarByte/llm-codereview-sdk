package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;

public class RunDB {
    private Long id;
    private Long reviewId;
    private ReviewParameter reviewParameter;

    public Long getId() {
        return id;
    }

    public RunDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public RunDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public ReviewParameter getReviewParameter() {
        return reviewParameter;
    }

    public RunDB setReviewParameter(ReviewParameter reviewParameter) {
        this.reviewParameter = reviewParameter;
        return this;
    }
}
