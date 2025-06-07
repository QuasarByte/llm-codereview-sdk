package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

public interface ReviewParallelExecutionService {
    ReviewResult review(ReviewParameter reviewParameter, LlmClient llmClient, ParallelExecutionParameter parallelExecutionParameter);
}
