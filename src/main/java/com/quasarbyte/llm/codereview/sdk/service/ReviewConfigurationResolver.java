package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedReviewConfiguration;

public interface ReviewConfigurationResolver {
    ResolvedReviewConfiguration resolve(ReviewParameter reviewParameter);
}
