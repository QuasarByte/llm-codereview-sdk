package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;
import com.quasarbyte.llm.codereview.sdk.model.db.InferenceDB;
import com.quasarbyte.llm.codereview.sdk.model.db.InferenceStatusEnum;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItemJson;
import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepository;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class LlmReviewProcessorStatefulImpl implements LlmReviewProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LlmReviewProcessorStatefulImpl.class);

    private final InferenceRepository inferenceRepository;
    private final LlmReviewProcessor llmReviewProcessor;
    private final ReviewRunContext reviewRunContext;
    private final ReviewedResultItemJsonMapper reviewedResultItemJsonMapper;

    public LlmReviewProcessorStatefulImpl(InferenceRepository inferenceRepository, LlmReviewProcessor llmReviewProcessor, ReviewRunContext reviewRunContext, ReviewedResultItemJsonMapper reviewedResultItemJsonMapper) {
        this.inferenceRepository = inferenceRepository;
        this.llmReviewProcessor = llmReviewProcessor;
        this.reviewRunContext = reviewRunContext;
        this.reviewedResultItemJsonMapper = reviewedResultItemJsonMapper;
    }

    @Override
    public ReviewedResultItem process(ReviewPrompt prompt,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient) {
        ReviewRunDetails reviewRunDetails = Objects.requireNonNull(reviewRunContext.getRunDetails(), "reviewRunDetails cannot be null");
        Long reviewId = reviewRunDetails.getReviewId();

        boolean reviewIsNew = Objects.requireNonNull(reviewRunDetails.getReviewIsNew(), "reviewIsNew cannot be null");

        ReviewedResultItem reviewedResultItem = null;
        Exception exception = null;
        ReviewedResultItemJson reviewedResultItemJson;
        InferenceStatusEnum inferenceStatus;

        try {
            reviewedResultItem = llmReviewProcessor.process(prompt, llmChatCompletionConfiguration, messagesMapperConfiguration, llmClient);
            reviewedResultItemJson = reviewedResultItemJsonMapper.toJson(reviewedResultItem);
            inferenceStatus = InferenceStatusEnum.FINISHED;
        } catch (Exception e) {
            logger.error("Inference failed, error message: '{}'", e.getMessage(), e);
            exception = e;
            
            // Create minimal usage data for failed operations to maintain tracking
            ReviewedCompletionUsage failedUsage = new ReviewedCompletionUsage()
                    .setPromptTokens(0L)
                    .setCompletionTokens(0L)
                    .setTotalTokens(0L);
            
            // Try to extract any partial token usage from the exception or other sources
            if (reviewedResultItem != null && reviewedResultItem.getCompletionUsage() != null) {
                failedUsage = reviewedResultItem.getCompletionUsage();
                logger.debug("Captured partial token usage from failed operation: {}", failedUsage);
            }
            
            reviewedResultItemJson = new ReviewedResultItemJson()
                    .setCompletionUsage(failedUsage)
                    .setFiles(new ArrayList<>())
                    .setThinkSteps(new ArrayList<>());
            inferenceStatus = InferenceStatusEnum.FAILED;
        }

        InferenceDB inferenceDB = new InferenceDB()
                .setReviewId(reviewId)
                .setRunId(reviewRunDetails.getRunId())
                .setPromptId(prompt.getId())
                .setReviewedResultItem(reviewedResultItemJson)
                .setStatus(inferenceStatus);

        Long inferenceId = inferenceRepository.save(inferenceDB);

        logger.debug("Inference saved for inference id '{}'", inferenceId);

        if (exception == null) {
            return reviewedResultItem;
        } else {
            logger.error("Rethrowing exception, error message: '{}'", exception.getMessage(), exception);
            throw new LLMCodeReviewRuntimeException(String.format("Inference failed, error message: '%s'", exception.getMessage()), exception);
        }
    }
}
