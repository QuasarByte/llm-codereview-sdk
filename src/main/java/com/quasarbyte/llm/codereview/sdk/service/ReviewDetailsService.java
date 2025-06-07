package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;

public interface ReviewDetailsService {
    ReviewedDetailedResult review(ReviewParameter reviewParameter, LlmClient llmClient);
    ReviewedDetailedResult review(ReviewParameter reviewParameter, LlmClient llmClient, ParallelExecutionParameter parallelExecutionParameter);
}
