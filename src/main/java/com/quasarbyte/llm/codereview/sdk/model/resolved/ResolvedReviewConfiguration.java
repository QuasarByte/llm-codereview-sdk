package com.quasarbyte.llm.codereview.sdk.model.resolved;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;

import java.util.List;

public class ResolvedReviewConfiguration {
    private ReviewParameter reviewParameter;
    private List<ResolvedReviewTarget> resolvedReviewTargets;

    public ReviewParameter getReviewConfiguration() {
        return reviewParameter;
    }

    public ResolvedReviewConfiguration setReviewConfiguration(ReviewParameter reviewParameter) {
        this.reviewParameter = reviewParameter;
        return this;
    }

    public List<ResolvedReviewTarget> getResolvedReviewTargets() {
        return resolvedReviewTargets;
    }

    public ResolvedReviewConfiguration setResolvedReviewTargets(List<ResolvedReviewTarget> resolvedReviewTargets) {
        this.resolvedReviewTargets = resolvedReviewTargets;
        return this;
    }
}
