package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;

public class ReviewRunContextImpl implements ReviewRunContext {

    private final InheritableThreadLocal<ReviewRunDetails> runDetails = new InheritableThreadLocal<>();

    @Override
    public void setReviewRunDetails(ReviewRunDetails reviewRunDetails) {
        runDetails.set(reviewRunDetails);
    }

    @Override
    public ReviewRunDetails getRunDetails() {
        return runDetails.get();
    }
}
