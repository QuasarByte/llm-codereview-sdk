package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;

public class ReviewTargetDB {
    private Long id;
    private Long reviewId;
    private ReviewTarget reviewTarget;

    public Long getId() {
        return id;
    }

    public ReviewTargetDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ReviewTargetDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public ReviewTarget getReviewTarget() {
        return reviewTarget;
    }

    public ReviewTargetDB setReviewTarget(ReviewTarget reviewTarget) {
        this.reviewTarget = reviewTarget;
        return this;
    }
}
