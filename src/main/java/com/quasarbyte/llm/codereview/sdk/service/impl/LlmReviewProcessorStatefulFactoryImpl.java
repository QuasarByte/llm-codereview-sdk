package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepository;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessorStatefulFactory;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapper;

public class LlmReviewProcessorStatefulFactoryImpl implements LlmReviewProcessorStatefulFactory {

    private final InferenceRepository inferenceRepository;
    private final LlmReviewProcessor llmReviewProcessor;
    private final ReviewRunContext reviewRunContext;
    private final ReviewedResultItemJsonMapper reviewedResultItemJsonMapper;

    public LlmReviewProcessorStatefulFactoryImpl(InferenceRepository inferenceRepository, LlmReviewProcessor llmReviewProcessor, ReviewRunContext reviewRunContext, ReviewedResultItemJsonMapper reviewedResultItemJsonMapper) {
        this.inferenceRepository = inferenceRepository;
        this.llmReviewProcessor = llmReviewProcessor;
        this.reviewRunContext = reviewRunContext;
        this.reviewedResultItemJsonMapper = reviewedResultItemJsonMapper;
    }

    @Override
    public LlmReviewProcessor create() {
        return new LlmReviewProcessorStatefulImpl(inferenceRepository, llmReviewProcessor, reviewRunContext, reviewedResultItemJsonMapper);
    }

}
