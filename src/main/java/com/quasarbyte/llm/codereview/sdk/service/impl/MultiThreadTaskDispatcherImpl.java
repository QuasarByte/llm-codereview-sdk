package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.*;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmTokensQuota;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LoadBalancingStrategy;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRoundRobin;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRandom;
import com.quasarbyte.llm.codereview.sdk.service.MultiThreadTaskDispatcher;
import com.quasarbyte.llm.codereview.sdk.service.QuotaTracker;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreator;
import com.quasarbyte.llm.codereview.sdk.service.util.LlmTokensQuotaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadTaskDispatcherImpl implements MultiThreadTaskDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadTaskDispatcherImpl.class);

    private final LlmReviewProcessor llmReviewProcessor;
    private final ReviewPromptCreator reviewPromptCreator;
    private final LlmClientLoadBalancerRoundRobin roundRobinLoadBalancer;
    private final LlmClientLoadBalancerRandom randomLoadBalancer;

    public MultiThreadTaskDispatcherImpl(LlmReviewProcessor llmReviewProcessor, 
                                         ReviewPromptCreator reviewPromptCreator,
                                         LlmClientLoadBalancerRoundRobin roundRobinLoadBalancer,
                                         LlmClientLoadBalancerRandom randomLoadBalancer) {
        this.llmReviewProcessor = llmReviewProcessor;
        this.reviewPromptCreator = reviewPromptCreator;
        this.roundRobinLoadBalancer = roundRobinLoadBalancer;
        this.randomLoadBalancer = randomLoadBalancer;
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            ExecutorService executorService) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, concurrency, null, executorService, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, concurrency, null, executorService, tokensQuota);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, concurrency, timeoutDuration, executorService, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, concurrency, timeoutDuration, executorService, tokensQuota);
    }

    // New load balancing methods
    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            List<LlmClient> llmClients,
            LoadBalancingStrategy loadBalancingStrategy,
            int concurrency,
            ExecutorService executorService) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, concurrency, null, executorService, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            List<LlmClient> llmClients,
            LoadBalancingStrategy loadBalancingStrategy,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, concurrency, timeoutDuration, executorService, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            List<LlmClient> llmClients,
            LoadBalancingStrategy loadBalancingStrategy,
            int concurrency,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, concurrency, null, executorService, tokensQuota);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            List<LlmClient> llmClients,
            LoadBalancingStrategy loadBalancingStrategy,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, concurrency, timeoutDuration, executorService, tokensQuota);
    }

    private List<ReviewedResultItem> processInternal(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        Objects.requireNonNull(useReasoning, "useReasoning must not be null");
        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");
        Objects.requireNonNull(executorService, "executorService must not be null");

        if (concurrency < 1) {
            throw new LLMCodeReviewRuntimeException("Thread count cannot be less than 1");
        }

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        // Initialize thread-safe quota tracker
        final QuotaTracker quotaTracker = new ThreadSafeQuotaTrackerImpl();

        String logMessage = timeoutDuration == null ?
                "Starting multi-thread dispatch of {} file batches (concurrency = {}, no timeout)" :
                "Starting multi-thread dispatch of {} file batches (concurrency = {}, timeout = {} ms)";

        if (tokensQuota != null) {
            logMessage = logMessage.replace("dispatch", "dispatch with thread-safe token quota validation");
        }

        if (timeoutDuration == null) {
            logger.info(logMessage, resolvedFilePathBatches.size(), concurrency);
        } else {
            logger.info(logMessage, resolvedFilePathBatches.size(), concurrency, timeoutDuration.toMillis());
        }

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

        final List<ReviewPrompt> reviewPrompts = reviewPromptCreator.create(resolvedFilesRulesList, useReasoning);

        int totalBatches = (reviewPrompts.size() + concurrency - 1) / concurrency;

        for (int i = 0, batchNum = 0; i < reviewPrompts.size(); i += concurrency, batchNum++) {

            if (timeoutDuration != null) {
                Duration elapsed = Duration.between(startTime, Instant.now());
                Duration remaining = timeoutDuration.minus(elapsed);

                if (remaining.isNegative() || remaining.isZero()) {
                    logger.warn("Timeout reached before processing batch group #{} of {} batches.", batchNum, totalBatches);
                    throw new TaskExecutorTimeoutException(
                            String.format("Timeout after %d ms at batch group #%d of %d batches", timeoutDuration.toMillis(), batchNum, totalBatches)
                    );
                }
            }

            logger.info("Processing parallel batch group #{} of {}", batchNum, totalBatches);

            Instant batchStart = Instant.now();

            List<ReviewPrompt> prompts = reviewPrompts.subList(i, Math.min(i + concurrency, reviewPrompts.size()));
            List<Callable<ReviewedResultItem>> callables = new ArrayList<>();

            // Prepare tasks for this batch group
            for (int j = 0; j < prompts.size(); j++) {
                final int batchIndex = i + j;
                ReviewPrompt reviewPrompt = prompts.get(j);
                callables.add(() -> {
                    logger.debug("Processing batch #{} of {} batches, prompt id: {}", batchIndex, totalBatches, reviewPrompt.getId());

                    // Pre-validation: Check if we've already exceeded quota before making LLM call
                    if (tokensQuota != null) {
                        ReviewedCompletionUsage currentUsage = quotaTracker.getCurrentUsage();
                        LlmTokensQuotaValidator.validateTokenUsage(currentUsage, tokensQuota);
                    }

                    final ReviewedResultItem reviewedResultItem = llmReviewProcessor.process(
                            reviewPrompt,
                            llmChatCompletionConfiguration,
                            messagesMapperConfiguration,
                            llmClient);

                    if (tokensQuota != null) {
                        // Thread-safe quota tracking after successful LLM call
                        if (reviewedResultItem.getCompletionUsage() != null) {
                            quotaTracker.addUsage(reviewedResultItem.getCompletionUsage());
                        }
                    }

                    logger.debug("Processed batch #{} of {} batches, prompt id: {}", batchIndex, totalBatches, reviewPrompt.getId());
                    return reviewedResultItem;
                });
            }

            // Submit tasks and collect results
            List<Future<ReviewedResultItem>> futures;
            try {
                if (timeoutDuration != null) {
                    Duration elapsed = Duration.between(startTime, Instant.now());
                    Duration remaining = timeoutDuration.minus(elapsed);
                    futures = executorService.invokeAll(callables, remaining.toMillis(), TimeUnit.MILLISECONDS);
                } else {
                    futures = executorService.invokeAll(callables);
                }
            } catch (InterruptedException e) {
                logger.error("Batch group #{} of {} batches interrupted during invokeAll: {}", batchNum, totalBatches, e.getMessage());
                Thread.currentThread().interrupt();
                throw new RuntimeException(String.format("Batch group #%d of %d batches interrupted", batchNum, totalBatches), e);
            }

            // Collect results
            for (int j = 0; j < futures.size(); j++) {
                Future<ReviewedResultItem> future = futures.get(j);
                Instant taskStart = Instant.now();
                try {
                    ReviewedResultItem result;
                    if (timeoutDuration != null) {
                        result = future.get(0, TimeUnit.MILLISECONDS); // 0 means 'if done'
                    } else {
                        result = future.get();
                    }
                    results.add(result);
                    long taskDuration = Duration.between(taskStart, Instant.now()).toMillis();
                    logger.info("Batch #{} of {} batches (parallel task in group {}) completed successfully. Duration: {} ms.", i + j, totalBatches, batchNum, taskDuration);
                } catch (CancellationException e) {
                    logger.warn("Batch #{} of {} batches was cancelled (possibly timed out)", i + j, totalBatches);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorCancellationException(
                            String.format("Batch #%d of %d batches was cancelled or timed out, error: '%s'", i + j, totalBatches, e.getMessage()), e
                    );
                } catch (TimeoutException e) {
                    logger.error("Batch #{} of {} batches timed out", i + j, totalBatches);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorTimeoutException(
                            String.format("Batch #%d of %d batches timed out, error: '%s'", i + j, totalBatches, e.getMessage()), e
                    );
                } catch (ExecutionException e) {
                    logger.error("Batch #{} of {} batches failed with exception: '{}'", i + j, totalBatches, e.getMessage(), e);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorException(
                            String.format("Batch #%d of %d batches failed: %s", i + j, totalBatches, e.getCause()), e.getCause());
                } catch (InterruptedException e) {
                    logger.error("Batch #{} of {} batches was interrupted: {}", i + j, totalBatches, e.getMessage());
                    Thread.currentThread().interrupt();
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorInterruptedException(
                            String.format("Batch #%d of %d batches was interrupted: %s", i + j, totalBatches, e.getMessage()), e);
                }
            }

            long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
            logger.info("Parallel batch group #{} of {} batches ({} tasks) completed. Duration: {} ms.", batchNum, totalBatches, prompts.size(), batchDuration);
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        // Log final quota usage if quota tracking was enabled
        if (tokensQuota != null) {
            ReviewedCompletionUsage finalUsage = quotaTracker.getCurrentUsage();
            logger.info("Final cumulative token usage: completion={}, prompt={}, total={}",
                    finalUsage.getCompletionTokens(), finalUsage.getPromptTokens(), finalUsage.getTotalTokens());
        }

        logger.info(
                "All batches processed successfully. Total batches: {}. Total prompts: {}. Total reviewed: {}. Total duration: {} ms.",
                totalBatches,
                reviewPrompts.size(),
                results.size(),
                totalDuration
        );
        return results;
    }

    private List<ReviewedResultItem> processInternalWithLoadBalancing(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            List<LlmClient> llmClients,
            LoadBalancingStrategy loadBalancingStrategy,
            int concurrency,
            Duration timeoutDuration,
            ExecutorService executorService,
            LlmTokensQuota tokensQuota) {

        Objects.requireNonNull(useReasoning, "useReasoning must not be null");
        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClients, "llmClients must not be null");
        Objects.requireNonNull(executorService, "executorService must not be null");

        if (concurrency < 1) {
            throw new LLMCodeReviewRuntimeException("Thread count cannot be less than 1");
        }

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        // Default to ROUND_ROBIN if strategy is null
        LoadBalancingStrategy strategy = loadBalancingStrategy != null ? loadBalancingStrategy : LoadBalancingStrategy.ROUND_ROBIN;
        
        // Create state for round-robin if needed
        final AtomicInteger roundRobinState = strategy == LoadBalancingStrategy.ROUND_ROBIN ? new AtomicInteger(0) : null;

        // Initialize thread-safe quota tracker
        final QuotaTracker quotaTracker = new ThreadSafeQuotaTrackerImpl();

        String logMessage = timeoutDuration == null ?
                "Starting multi-thread dispatch with load balancing of {} file batches (concurrency = {}, strategy = {}, clients = {}, no timeout)" :
                "Starting multi-thread dispatch with load balancing of {} file batches (concurrency = {}, strategy = {}, clients = {}, timeout = {} ms)";

        if (tokensQuota != null) {
            logMessage = logMessage.replace("dispatch", "dispatch with thread-safe token quota validation");
        }

        if (timeoutDuration == null) {
            logger.info(logMessage, resolvedFilePathBatches.size(), concurrency, strategy, llmClients.size());
        } else {
            logger.info(logMessage, resolvedFilePathBatches.size(), concurrency, strategy, llmClients.size(), timeoutDuration.toMillis());
        }

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

        final List<ReviewPrompt> reviewPrompts = reviewPromptCreator.create(resolvedFilesRulesList, useReasoning);

        int totalBatches = (reviewPrompts.size() + concurrency - 1) / concurrency;

        for (int i = 0, batchNum = 0; i < reviewPrompts.size(); i += concurrency, batchNum++) {

            if (timeoutDuration != null) {
                Duration elapsed = Duration.between(startTime, Instant.now());
                Duration remaining = timeoutDuration.minus(elapsed);

                if (remaining.isNegative() || remaining.isZero()) {
                    logger.warn("Timeout reached before processing batch group #{} of {} batches.", batchNum, totalBatches);
                    throw new TaskExecutorTimeoutException(
                            String.format("Timeout after %d ms at batch group #%d of %d batches", timeoutDuration.toMillis(), batchNum, totalBatches)
                    );
                }
            }

            logger.info("Processing parallel batch group #{} of {}", batchNum, totalBatches);

            Instant batchStart = Instant.now();

            List<ReviewPrompt> prompts = reviewPrompts.subList(i, Math.min(i + concurrency, reviewPrompts.size()));
            List<Callable<ReviewedResultItem>> callables = new ArrayList<>();

            // Prepare tasks for this batch group
            for (int j = 0; j < prompts.size(); j++) {
                final int batchIndex = i + j;
                ReviewPrompt reviewPrompt = prompts.get(j);
                callables.add(() -> {
                    logger.debug("Processing batch #{} of {} batches, prompt id: {}", batchIndex, totalBatches, reviewPrompt.getId());

                    // Pre-validation: Check if we've already exceeded quota before making LLM call
                    if (tokensQuota != null) {
                        ReviewedCompletionUsage currentUsage = quotaTracker.getCurrentUsage();
                        LlmTokensQuotaValidator.validateTokenUsage(currentUsage, tokensQuota);
                    }

                    // Select LLM client using load balancer
                    LlmClient selectedClient;
                    if (strategy == LoadBalancingStrategy.ROUND_ROBIN) {
                        selectedClient = roundRobinLoadBalancer.findLlmClient(llmClients, roundRobinState);
                    } else {
                        selectedClient = randomLoadBalancer.findLlmClient(llmClients);
                    }

                    final ReviewedResultItem reviewedResultItem = llmReviewProcessor.process(
                            reviewPrompt,
                            llmChatCompletionConfiguration,
                            messagesMapperConfiguration,
                            selectedClient);

                    if (tokensQuota != null) {
                        // Thread-safe quota tracking after successful LLM call
                        if (reviewedResultItem.getCompletionUsage() != null) {
                            quotaTracker.addUsage(reviewedResultItem.getCompletionUsage());
                        }
                    }

                    logger.debug("Processed batch #{} of {} batches, prompt id: {}", batchIndex, totalBatches, reviewPrompt.getId());
                    return reviewedResultItem;
                });
            }

            // Submit tasks and collect results
            List<Future<ReviewedResultItem>> futures;
            try {
                if (timeoutDuration != null) {
                    Duration elapsed = Duration.between(startTime, Instant.now());
                    Duration remaining = timeoutDuration.minus(elapsed);
                    futures = executorService.invokeAll(callables, remaining.toMillis(), TimeUnit.MILLISECONDS);
                } else {
                    futures = executorService.invokeAll(callables);
                }
            } catch (InterruptedException e) {
                logger.error("Batch group #{} of {} batches interrupted during invokeAll: {}", batchNum, totalBatches, e.getMessage());
                Thread.currentThread().interrupt();
                throw new RuntimeException(String.format("Batch group #%d of %d batches interrupted", batchNum, totalBatches), e);
            }

            // Collect results
            for (int j = 0; j < futures.size(); j++) {
                Future<ReviewedResultItem> future = futures.get(j);
                Instant taskStart = Instant.now();
                try {
                    ReviewedResultItem result;
                    if (timeoutDuration != null) {
                        result = future.get(0, TimeUnit.MILLISECONDS); // 0 means 'if done'
                    } else {
                        result = future.get();
                    }
                    results.add(result);
                    long taskDuration = Duration.between(taskStart, Instant.now()).toMillis();
                    logger.info("Batch #{} of {} batches (parallel task in group {}) completed successfully. Duration: {} ms.", i + j, totalBatches, batchNum, taskDuration);
                } catch (CancellationException e) {
                    logger.warn("Batch #{} of {} batches was cancelled (possibly timed out)", i + j, totalBatches);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorCancellationException(
                            String.format("Batch #%d of %d batches was cancelled or timed out, error: '%s'", i + j, totalBatches, e.getMessage()), e
                    );
                } catch (TimeoutException e) {
                    logger.error("Batch #{} of {} batches timed out", i + j, totalBatches);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorTimeoutException(
                            String.format("Batch #%d of %d batches timed out, error: '%s'", i + j, totalBatches, e.getMessage()), e
                    );
                } catch (ExecutionException e) {
                    logger.error("Batch #{} of {} batches failed with exception: '{}'", i + j, totalBatches, e.getMessage(), e);
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorException(
                            String.format("Batch #%d of %d batches failed: %s", i + j, totalBatches, e.getCause()), e.getCause());
                } catch (InterruptedException e) {
                    logger.error("Batch #{} of {} batches was interrupted: {}", i + j, totalBatches, e.getMessage());
                    Thread.currentThread().interrupt();
                    // Cancel all remaining futures in this batch
                    cancelRemainingFutures(futures, j);
                    throw new TaskExecutorInterruptedException(
                            String.format("Batch #%d of %d batches was interrupted: %s", i + j, totalBatches, e.getMessage()), e);
                }
            }

            long batchDuration = Duration.between(batchStart, Instant.now()).toMillis();
            logger.info("Parallel batch group #{} of {} batches ({} tasks) completed. Duration: {} ms.", batchNum, totalBatches, prompts.size(), batchDuration);
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        // Log final quota usage if quota tracking was enabled
        if (tokensQuota != null) {
            ReviewedCompletionUsage finalUsage = quotaTracker.getCurrentUsage();
            logger.info("Final cumulative token usage: completion={}, prompt={}, total={}",
                    finalUsage.getCompletionTokens(), finalUsage.getPromptTokens(), finalUsage.getTotalTokens());
        }

        logger.info(
                "All batches processed successfully with load balancing. Strategy: {}. Total batches: {}. Total prompts: {}. Total reviewed: {}. Total duration: {} ms.",
                strategy,
                totalBatches,
                reviewPrompts.size(),
                results.size(),
                totalDuration
        );
        return results;
    }

    /**
     * Cancels all remaining futures starting from the specified index to prevent resource leaks
     */
    private void cancelRemainingFutures(List<Future<ReviewedResultItem>> futures, int startIndex) {
        for (int k = startIndex; k < futures.size(); k++) {
            Future<ReviewedResultItem> futureToCancel = futures.get(k);
            if (!futureToCancel.isDone()) {
                boolean cancelled = futureToCancel.cancel(true);
                logger.debug("Cancelled future #{}: {}", k, cancelled);
            }
        }
    }
}
