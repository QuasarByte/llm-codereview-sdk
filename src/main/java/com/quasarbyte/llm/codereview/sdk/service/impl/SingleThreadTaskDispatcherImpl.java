package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewException;
import com.quasarbyte.llm.codereview.sdk.exception.TaskExecutorTimeoutException;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathToPromptMapper;
import com.quasarbyte.llm.codereview.sdk.service.SingleThreadTaskDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SingleThreadTaskDispatcherImpl implements SingleThreadTaskDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadTaskDispatcherImpl.class);

    private final LlmReviewProcessor llmReviewProcessor;
    private final ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper;

    public SingleThreadTaskDispatcherImpl(LlmReviewProcessor llmReviewProcessor,
                                          ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper) {
        this.llmReviewProcessor = llmReviewProcessor;
        this.resolvedFilePathToPromptMapper = resolvedFilePathToPromptMapper;
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient) {

        // Validate input parameters
        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        logger.info("Starting main-thread dispatch of {} file batches (no timeout)", resolvedFilePathBatches.size());

        List<ReviewedResultItem> results = new ArrayList<>();
        Instant startTime = Instant.now();

        List<ResolvedFilesRules> resolvedFilesRulesList = new ArrayList<>();

        if (rulesBatches.isEmpty()) {
            logger.info("No rules batches provided. Each file batch will be processed with empty rule set.");
            resolvedFilePathBatches.forEach(rfp ->
                    resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, Collections.emptyList()))
            );
        } else {
            logger.info("Preparing Cartesian product of file batches and rule batches.");
            resolvedFilePathBatches.forEach(rfp ->
                    rulesBatches.forEach(ruleBatch ->
                            resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, ruleBatch))
                    )
            );
        }

        logger.info("Prepared {} ResolvedFilesRules batches for processing.", resolvedFilesRulesList.size());

        Map<String, SourceFile> sourceFileCache = new HashMap<>();
        AtomicLong fileId = new AtomicLong();
        AtomicLong ruleId = new AtomicLong();

        for (int i = 0; i < resolvedFilesRulesList.size(); i++) {
            Instant batchStart = Instant.now();
            ResolvedFilesRules resolvedFRBatch = resolvedFilesRulesList.get(i);
            try {
                logger.debug(
                        "Processing batch #{}: {} files, {} rules",
                        i,
                        resolvedFRBatch.getResolvedFilePaths() != null ? resolvedFRBatch.getResolvedFilePaths().size() : 0,
                        resolvedFRBatch.getRules() != null ? resolvedFRBatch.getRules().size() : 0
                );
                ReviewPrompt reviewPrompt = resolvedFilePathToPromptMapper.map(resolvedFRBatch, fileId, ruleId, sourceFileCache);

                logger.debug(
                        "Mapped ReviewPrompt for batch #{}: files count = {}, rules count = {}",
                        i,
                        reviewPrompt.getFiles() != null ? reviewPrompt.getFiles().size() : 0,
                        reviewPrompt.getRules() != null ? reviewPrompt.getRules().size() : 0
                );

                ReviewedResultItem result = llmReviewProcessor.process(
                        reviewPrompt,
                        llmChatCompletionConfiguration,
                        messagesMapperConfiguration,
                        llmClient
                );
                results.add(result);

                long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
                logger.info("Batch #{} completed successfully. Duration: {} ms.", i, batchDuration);

            } catch (Exception e) {
                logger.error("Batch #{} failed with exception: '{}'", i, e.getMessage(), e);
                throw new LLMCodeReviewException(String.format("Batch #%d failed: %s", i, e.getMessage()), e);
            }
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
        logger.info(
                "All batches processed successfully. Total reviewed: {}. Total duration: {} ms.",
                results.size(),
                totalDuration
        );

        return results;
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            Duration timeoutDuration) {

        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");
        Objects.requireNonNull(timeoutDuration, "timeoutDuration must not be null");

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        logger.info("Starting main-thread dispatch of {} file batches (timeout = {} ms)",
                resolvedFilePathBatches.size(), timeoutDuration.toMillis());

        List<ReviewedResultItem> results = new ArrayList<>();
        Instant startTime = Instant.now();

        List<ResolvedFilesRules> resolvedFilesRulesList = new ArrayList<>();

        if (rulesBatches.isEmpty()) {
            logger.info("No rules batches provided. Each file batch will be processed with empty rule set.");
            resolvedFilePathBatches.forEach(rfp ->
                    resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, Collections.emptyList()))
            );
        } else {
            logger.info("Preparing Cartesian product of file batches and rule batches.");
            resolvedFilePathBatches.forEach(rfp ->
                    rulesBatches.forEach(ruleBatch ->
                            resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, ruleBatch))
                    )
            );
        }

        logger.info("Prepared {} ResolvedFilesRules batches for processing.", resolvedFilesRulesList.size());

        Map<String, SourceFile> sourceFileCache = new HashMap<>();
        AtomicLong fileId = new AtomicLong();
        AtomicLong ruleId = new AtomicLong();

        for (int i = 0; i < resolvedFilesRulesList.size(); i++) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            Duration remaining = timeoutDuration.minus(elapsed);

            if (remaining.isNegative() || remaining.isZero()) {
                logger.warn("Timeout reached before processing batch #{}", i);
                throw new TaskExecutorTimeoutException(
                        String.format("Timeout after %d ms at batch #%d", timeoutDuration.toMillis(), i)
                );
            }

            Instant batchStart = Instant.now();
            ResolvedFilesRules resolvedFRBatch = resolvedFilesRulesList.get(i);
            try {
                logger.debug(
                        "Processing batch #{}: {} files, {} rules",
                        i,
                        resolvedFRBatch.getResolvedFilePaths() != null ? resolvedFRBatch.getResolvedFilePaths().size() : 0,
                        resolvedFRBatch.getRules() != null ? resolvedFRBatch.getRules().size() : 0
                );
                ReviewPrompt reviewPrompt = resolvedFilePathToPromptMapper.map(resolvedFRBatch, fileId, ruleId, sourceFileCache);

                logger.debug(
                        "Mapped ReviewPrompt for batch #{}: files count = {}, rules count = {}",
                        i,
                        reviewPrompt.getFiles() != null ? reviewPrompt.getFiles().size() : 0,
                        reviewPrompt.getRules() != null ? reviewPrompt.getRules().size() : 0
                );

                ReviewedResultItem result = llmReviewProcessor.process(
                        reviewPrompt,
                        llmChatCompletionConfiguration,
                        messagesMapperConfiguration,
                        llmClient
                );
                results.add(result);

                long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
                logger.info("Batch #{} completed successfully. Duration: {} ms.", i, batchDuration);

            } catch (Exception e) {
                logger.error("Batch #{} failed with exception: '{}'", i, e.getMessage(), e);
                if (e instanceof TaskExecutorTimeoutException) {
                    throw e;
                }
                throw new LLMCodeReviewException(String.format("Batch #%d failed: %s", i, e.getMessage()), e);
            }
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
        logger.info(
                "All batches processed successfully. Total reviewed: {}. Total duration: {} ms.",
                results.size(),
                totalDuration
        );

        return results;
    }
}
