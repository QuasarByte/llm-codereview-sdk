package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

import java.util.List;

public interface ReviewService {
    ReviewResult review(ReviewParameter reviewParameter, LlmClient llmClient, PersistenceConfiguration persistenceConfiguration);
    ReviewResult review(ReviewParameter reviewParameter, List<LlmClient> llmClients, PersistenceConfiguration persistenceConfiguration, ParallelExecutionParameter parallelExecutionParameter);
}
