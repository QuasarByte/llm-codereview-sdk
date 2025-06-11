package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;

public interface ReviewRunContext {

    void setReviewRunDetails(ReviewRunDetails reviewRunDetails);

    ReviewRunDetails getRunDetails();
}
