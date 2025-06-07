package com.quasarbyte.llm.codereview.sdk.model.resolved;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;

import java.util.List;

public class ResolvedReviewTarget {
    private ReviewTarget reviewTarget;
    private ResolvedReviewConfiguration resolvedReviewConfiguration;
    private List<ResolvedFileGroup> resolvedFileGroups;

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
