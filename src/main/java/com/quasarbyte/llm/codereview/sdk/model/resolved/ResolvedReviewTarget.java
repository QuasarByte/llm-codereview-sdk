package com.quasarbyte.llm.codereview.sdk.model.resolved;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;

import java.util.List;

public class ResolvedReviewTarget {
    private Long id;
    private Long reviewId;
    private ReviewTarget reviewTarget;
    private ResolvedReviewConfiguration resolvedReviewConfiguration;
    private List<ResolvedFileGroup> resolvedFileGroups;

    public Long getId() {
        return id;
    }

    public ResolvedReviewTarget setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ResolvedReviewTarget setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public ReviewTarget getReviewTarget() {
        return reviewTarget;
    }

    public ResolvedReviewTarget setReviewTarget(ReviewTarget reviewTarget) {
        this.reviewTarget = reviewTarget;
        return this;
    }

    public ResolvedReviewConfiguration getResolvedReviewConfiguration() {
        return resolvedReviewConfiguration;
    }

    public ResolvedReviewTarget setResolvedReviewConfiguration(ResolvedReviewConfiguration resolvedReviewConfiguration) {
        this.resolvedReviewConfiguration = resolvedReviewConfiguration;
        return this;
    }

    public List<ResolvedFileGroup> getResolvedFileGroups() {
        return resolvedFileGroups;
    }

    public ResolvedReviewTarget setResolvedFileGroups(List<ResolvedFileGroup> resolvedFileGroups) {
        this.resolvedFileGroups = resolvedFileGroups;
        return this;
    }
}
