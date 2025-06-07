package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

public interface ReviewService {
    ReviewResult review(ReviewParameter reviewParameter, LlmClient llmClient);
}
