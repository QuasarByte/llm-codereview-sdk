package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.*;
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
import com.quasarbyte.llm.codereview.sdk.service.MultiThreadTaskDispatcher;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathToPromptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class MultiThreadTaskDispatcherImpl implements MultiThreadTaskDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadTaskDispatcherImpl.class);

    private final LlmReviewProcessor llmReviewProcessor;
    private final ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper;

    public MultiThreadTaskDispatcherImpl(LlmReviewProcessor llmReviewProcessor, ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper) {
        this.llmReviewProcessor = llmReviewProcessor;
        this.resolvedFilePathToPromptMapper = resolvedFilePathToPromptMapper;
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            ExecutorService executorService) {

        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");
        Objects.requireNonNull(executorService, "executorService must not be null");

        if (concurrency < 1) {
            throw new LLMCodeReviewException("Thread count cannot be less than 1");
        }

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        logger.info("Starting multi-thread dispatch of {} file batches (concurrency = {}, no timeout)",
                resolvedFilePathBatches.size(), concurrency);

        // Optional: warn if executorService may not have enough threads for desired concurrency
        if (executorService instanceof ThreadPoolExecutor) {
            int poolSize = ((ThreadPoolExecutor) executorService).getMaximumPoolSize();
            if (poolSize < concurrency) {
                logger.warn("Provided ExecutorService maximum pool size ({}) is less than requested concurrency ({})", poolSize, concurrency);
            }
        }

        List<ReviewedResultItem> results = new ArrayList<>();
        Instant startTime = Instant.now();

        List<ResolvedFilesRules> resolvedFilesRulesList = new ArrayList<>();

        if (rulesBatches.isEmpty()) {
            logger.info("No rules batches provided. Each file batch will be processed with empty rule set.");
            resolvedFilePathBatches.forEach(rfp ->
                    resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, new ArrayList<>()))
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

        Map<String, SourceFile> sourceFileCache = new ConcurrentHashMap<>();
        AtomicLong fileId = new AtomicLong();
        AtomicLong ruleId = new AtomicLong();

        int totalBatches = (resolvedFilesRulesList.size() + concurrency - 1) / concurrency;

        for (int i = 0, batchNum = 0; i < resolvedFilesRulesList.size(); i += concurrency, batchNum++) {

            logger.info("Processing parallel batch group #{} of {}", batchNum, totalBatches);

            Instant batchStart = Instant.now();

            List<ResolvedFilesRules> resolvedFilesRules =
                    resolvedFilesRulesList.subList(i, Math.min(i + concurrency, resolvedFilesRulesList.size()));
            List<Callable<ReviewedResultItem>> callables = new ArrayList<>();

            // Prepare tasks for this batch group
            for (int j = 0; j < resolvedFilesRules.size(); j++) {
                final int batchIndex = i + j;
                ResolvedFilesRules resolvedFRBatch = resolvedFilesRules.get(j);
                callables.add(() -> {
                    logger.debug(
                            "Processing batch #{}: {} files, {} rules",
                            batchIndex,
                            resolvedFRBatch.getResolvedFilePaths() != null ? resolvedFRBatch.getResolvedFilePaths().size() : 0,
                            resolvedFRBatch.getRules() != null ? resolvedFRBatch.getRules().size() : 0
                    );
                    ReviewPrompt reviewPrompt = resolvedFilePathToPromptMapper.map(resolvedFRBatch, fileId, ruleId, sourceFileCache);

                    logger.debug(
                            "Mapped ReviewPrompt for batch #{}: files count = {}, rules count = {}",
                            batchIndex,
                            reviewPrompt.getFiles() != null ? reviewPrompt.getFiles().size() : 0,
                            reviewPrompt.getRules() != null ? reviewPrompt.getRules().size() : 0
                    );

                    return llmReviewProcessor.process(
                            reviewPrompt,
                            llmChatCompletionConfiguration,
                            messagesMapperConfiguration,
                            llmClient
                    );
                });
            }

            // Submit tasks and collect results, now with NO timeout (wait until all complete)
            List<Future<ReviewedResultItem>> futures;
            try {
                futures = executorService.invokeAll(callables); // <--- Without timeout!
            } catch (InterruptedException e) {
                logger.error("Batch group #{} interrupted during invokeAll: {}", batchNum, e.getMessage());
                Thread.currentThread().interrupt();
                throw new RuntimeException("Batch group interrupted", e);
            }

            // Collect results
            for (int j = 0; j < futures.size(); j++) {
                Future<ReviewedResultItem> future = futures.get(j);
                Instant taskStart = Instant.now();
                try {
                    ReviewedResultItem result = future.get(); // <--- Without timeout!
                    results.add(result);
                    long taskDuration = Duration.between(taskStart, Instant.now()).toMillis();
                    logger.info("Batch #{} (parallel task in group {}) completed successfully. Duration: {} ms.", i + j, batchNum, taskDuration);
                } catch (CancellationException e) {
                    logger.warn("Batch #{} was cancelled.", i + j);
                    throw new TaskExecutorCancellationException(
                            String.format("Batch #%d was cancelled, error: '%s'", i + j, e.getMessage()), e
                    );
                } catch (ExecutionException e) {
                    logger.error("Batch #{} failed with exception: '{}'", i + j, e.getMessage(), e);
                    throw new TaskExecutorException(
                            String.format("Batch #%d failed: %s", i + j, e.getCause()), e.getCause());
                } catch (InterruptedException e) {
                    logger.error("Batch #{} was interrupted: {}", i + j, e.getMessage());
                    Thread.currentThread().interrupt();
                    throw new TaskExecutorInterruptedException(
                            String.format("Batch #%d was interrupted: %s", i + j, e.getMessage()), e);
                }
            }

            long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
            logger.info("Parallel batch group #{} of {} ({} tasks) completed. Duration: {} ms.", batchNum, totalBatches, resolvedFilesRules.size(), batchDuration);
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
    public List<ReviewedResultItem> dispatch(List<List<Rule>> rulesBatches,
                                             List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                             LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                             LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                             LlmClient llmClient,
                                             int concurrency,
                                             Duration timeoutDuration,
                                             ExecutorService executorService) {

        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");
        Objects.requireNonNull(timeoutDuration, "timeoutDuration must not be null");
        Objects.requireNonNull(executorService, "executorService must not be null");

        if (concurrency < 1) {
            throw new LLMCodeReviewException("Thread count cannot be less than 1");
        }

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        logger.info("Starting multi-thread dispatch of {} file batches (concurrency = {}, timeout = {} ms)",
                resolvedFilePathBatches.size(), concurrency, timeoutDuration.toMillis());

        // Optional: warn if executorService may not have enough threads for desired concurrency
        if (executorService instanceof ThreadPoolExecutor) {
            int poolSize = ((ThreadPoolExecutor) executorService).getMaximumPoolSize();
            if (poolSize < concurrency) {
                logger.warn("Provided ExecutorService maximum pool size ({}) is less than requested concurrency ({})", poolSize, concurrency);
            }
        }

        List<ReviewedResultItem> results = new ArrayList<>();
        Instant startTime = Instant.now();

        List<ResolvedFilesRules> resolvedFilesRulesList = new ArrayList<>();

        if (rulesBatches.isEmpty()) {
            logger.info("No rules batches provided. Each file batch will be processed with empty rule set.");
            resolvedFilePathBatches.forEach(rfp ->
                    resolvedFilesRulesList.add(new ResolvedFilesRules(rfp, new ArrayList<>()))
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

        Map<String, SourceFile> sourceFileCache = new ConcurrentHashMap<>();
        AtomicLong fileId = new AtomicLong();
        AtomicLong ruleId = new AtomicLong();

        // Total number of parallel batch groups (used for progress logging)
        int totalBatches = (resolvedFilesRulesList.size() + concurrency - 1) / concurrency;

        for (int i = 0, batchNum = 0; i < resolvedFilesRulesList.size(); i += concurrency, batchNum++) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            Duration remaining = timeoutDuration.minus(elapsed);

            if (remaining.isNegative() || remaining.isZero()) {
                logger.warn("Timeout reached before processing batch group #{}.", batchNum);
                throw new TaskExecutorTimeoutException(
                        String.format("Timeout after %d ms at batch group #%d", timeoutDuration.toMillis(), batchNum)
                );
            }

            logger.info("Processing parallel batch group #{} of {}", batchNum, totalBatches);

            Instant batchStart = Instant.now();

            List<ResolvedFilesRules> resolvedFilesRules =
                    resolvedFilesRulesList.subList(i, Math.min(i + concurrency, resolvedFilesRulesList.size()));
            List<Callable<ReviewedResultItem>> callables = new ArrayList<>();

            // Prepare tasks for this batch group
            for (int j = 0; j < resolvedFilesRules.size(); j++) {
                final int batchIndex = i + j;
                ResolvedFilesRules resolvedFRBatch = resolvedFilesRules.get(j);
                callables.add(() -> {
                    logger.debug(
                            "Processing batch #{}: {} files, {} rules",
                            batchIndex,
                            resolvedFRBatch.getResolvedFilePaths() != null ? resolvedFRBatch.getResolvedFilePaths().size() : 0,
                            resolvedFRBatch.getRules() != null ? resolvedFRBatch.getRules().size() : 0
                    );
                    ReviewPrompt reviewPrompt = resolvedFilePathToPromptMapper.map(resolvedFRBatch, fileId, ruleId, sourceFileCache);

                    logger.debug(
                            "Mapped ReviewPrompt for batch #{}: files count = {}, rules count = {}",
                            batchIndex,
                            reviewPrompt.getFiles() != null ? reviewPrompt.getFiles().size() : 0,
                            reviewPrompt.getRules() != null ? reviewPrompt.getRules().size() : 0
                    );

                    return llmReviewProcessor.process(
                            reviewPrompt,
                            llmChatCompletionConfiguration,
                            messagesMapperConfiguration,
                            llmClient
                    );
                });
            }

            // Submit tasks and collect results, enforcing remaining global timeout
            List<Future<ReviewedResultItem>> futures;
            try {
                futures = executorService.invokeAll(callables, remaining.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Batch group #{} interrupted during invokeAll: {}", batchNum, e.getMessage());
                Thread.currentThread().interrupt();
                throw new RuntimeException("Batch group interrupted", e);
            }

            // Collect results, fail-fast if any batch timed out
            for (int j = 0; j < futures.size(); j++) {
                Future<ReviewedResultItem> future = futures.get(j);
                Instant taskStart = Instant.now();
                try {
                    ReviewedResultItem result = future.get(0, TimeUnit.MILLISECONDS); // 0 means 'if done'
                    results.add(result);
                    long taskDuration = Duration.between(taskStart, Instant.now()).toMillis();
                    logger.info("Batch #{} (parallel task in group {}) completed successfully. Duration: {} ms.", i + j, batchNum, taskDuration);
                } catch (CancellationException e) {
                    logger.warn("Batch #{} was cancelled (possibly timed out)", i + j);
                    throw new TaskExecutorCancellationException(
                            String.format("Batch #%d was cancelled or timed out, error: '%s'", i + j, e.getMessage()), e
                    );
                } catch (TimeoutException e) {
                    logger.error("Batch #{} timed out after {} ms", i + j, remaining.toMillis());
                    future.cancel(true);
                    throw new TaskExecutorTimeoutException(
                            String.format("Batch #%d timed out, error: '%s'", i + j, e.getMessage()), e
                    );
                } catch (ExecutionException e) {
                    logger.error("Batch #{} failed with exception: '{}'", i + j, e.getMessage(), e);
                    throw new TaskExecutorException(
                            String.format("Batch #%d failed: %s", i + j, e.getCause()), e.getCause());
                } catch (InterruptedException e) {
                    logger.error("Batch #{} was interrupted: {}", i + j, e.getMessage());
                    Thread.currentThread().interrupt();
                    throw new TaskExecutorInterruptedException(
                            String.format("Batch #%d was interrupted: %s", i + j, e.getMessage()), e);
                }
            }

            long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
            logger.info("Parallel batch group #{} of {} ({} tasks) completed. Duration: {} ms.", batchNum, totalBatches, resolvedFilesRules.size(), batchDuration);
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
