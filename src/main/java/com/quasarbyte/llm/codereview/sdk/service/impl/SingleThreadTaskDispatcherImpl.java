package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;
import com.quasarbyte.llm.codereview.sdk.exception.TaskExecutorTimeoutException;
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
import com.quasarbyte.llm.codereview.sdk.service.QuotaTracker;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreator;
import com.quasarbyte.llm.codereview.sdk.service.SingleThreadTaskDispatcher;
import com.quasarbyte.llm.codereview.sdk.service.util.LlmTokensQuotaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadTaskDispatcherImpl implements SingleThreadTaskDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadTaskDispatcherImpl.class);

    private final LlmReviewProcessor llmReviewProcessor;
    private final ReviewPromptCreator reviewPromptCreator;
    private final LlmClientLoadBalancerRoundRobin roundRobinLoadBalancer;
    private final LlmClientLoadBalancerRandom randomLoadBalancer;

    public SingleThreadTaskDispatcherImpl(LlmReviewProcessor llmReviewProcessor, 
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
            LlmClient llmClient) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, null, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            LlmTokensQuota tokensQuota) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, null, tokensQuota);
    }

    @Override
    public List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                             List<List<Rule>> rulesBatches,
                                             List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                             LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                             LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                             LlmClient llmClient,
                                             Duration timeoutDuration) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, timeoutDuration, null);
    }

    @Override
    public List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                             List<List<Rule>> rulesBatches,
                                             List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                             LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                             LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                             LlmClient llmClient,
                                             Duration timeoutDuration,
                                             LlmTokensQuota tokensQuota) {

        return processInternal(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClient, timeoutDuration, tokensQuota);
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
            LoadBalancingStrategy loadBalancingStrategy) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, null, null);
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
            Duration timeoutDuration) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, timeoutDuration, null);
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
            LlmTokensQuota tokensQuota) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, null, tokensQuota);
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
            Duration timeoutDuration,
            LlmTokensQuota tokensQuota) {

        return processInternalWithLoadBalancing(useReasoning, rulesBatches, resolvedFilePathBatches,
                llmChatCompletionConfiguration, messagesMapperConfiguration,
                llmClients, loadBalancingStrategy, timeoutDuration, tokensQuota);
    }

    private List<ReviewedResultItem> processInternal(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            Duration timeoutDuration,
            LlmTokensQuota tokensQuota) {

        // Validate input parameters
        Objects.requireNonNull(useReasoning, "useReasoning must not be null");
        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        // Initialize quota tracker for cumulative token tracking
        final QuotaTracker quotaTracker = new SimpleQuotaTrackerImpl();

        String logMessage = timeoutDuration == null ?
                "Starting main-thread dispatch of {} file batches (no timeout)" :
                "Starting main-thread dispatch of {} file batches (timeout = {} ms)";

        if (tokensQuota != null) {
            logMessage = logMessage.replace("dispatch", "dispatch with token quota validation");
        }

        if (timeoutDuration == null) {
            logger.info(logMessage, resolvedFilePathBatches.size());
        } else {
            logger.info(logMessage, resolvedFilePathBatches.size(), timeoutDuration.toMillis());
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

        for (int i = 0; i < reviewPrompts.size(); i++) {
            if (timeoutDuration != null) {
                Duration elapsed = Duration.between(startTime, Instant.now());
                Duration remaining = timeoutDuration.minus(elapsed);

                if (remaining.isNegative() || remaining.isZero()) {
                    logger.warn("Timeout reached before processing prompt #{}", i);
                    throw new TaskExecutorTimeoutException(
                            String.format("Timeout after %d ms at prompt #%d", timeoutDuration.toMillis(), i)
                    );
                }
            }

            Instant reviewPromptStart = Instant.now();
            ReviewPrompt reviewPrompt = reviewPrompts.get(i);

            try {
                logger.debug("Processing prompt #{} with id: {}", i, reviewPrompt.getId());

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
                    // Track cumulative token usage after successful LLM call
                    if (reviewedResultItem.getCompletionUsage() != null) {
                        quotaTracker.addUsage(reviewedResultItem.getCompletionUsage());
                    }
                }

                results.add(reviewedResultItem);

                long reviewPromptDuration = Duration.between(reviewPromptStart, Instant.now()).toMillis();
                logger.info("Prompt #{} with id: {} completed successfully. Duration: {} ms.", i, reviewPrompt.getId(), reviewPromptDuration);

            } catch (Exception e) {
                logger.error("Prompt process #{} with id: {} failed with exception: '{}'", i, reviewPrompt.getId(), e.getMessage(), e);
                throw new LLMCodeReviewRuntimeException(String.format("Prompt #%d with id: %d failed: %s", i, reviewPrompt.getId(), e.getMessage()), e);
            }
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        // Log final quota usage if quota tracking was enabled
        if (tokensQuota != null) {
            ReviewedCompletionUsage finalUsage = quotaTracker.getCurrentUsage();
            logger.info("Final cumulative token usage: completion={}, prompt={}, total={}",
                    finalUsage.getCompletionTokens(), finalUsage.getPromptTokens(), finalUsage.getTotalTokens());
        }

        logger.info(
                "All prompts processed successfully. Total reviewed: {}. Total duration: {} ms.",
                reviewPrompts.size(),
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
            Duration timeoutDuration,
            LlmTokensQuota tokensQuota) {

        // Validate input parameters
        Objects.requireNonNull(useReasoning, "useReasoning must not be null");
        Objects.requireNonNull(rulesBatches, "rulesBatches must not be null");
        Objects.requireNonNull(resolvedFilePathBatches, "resolvedFilePathBatches must not be null");
        Objects.requireNonNull(llmChatCompletionConfiguration, "llmChatCompletionConfiguration must not be null");
        Objects.requireNonNull(messagesMapperConfiguration, "messagesMapperConfiguration must not be null");
        Objects.requireNonNull(llmClients, "llmClients must not be null");

        if (resolvedFilePathBatches.isEmpty()) {
            logger.info("No resolved file path batches to process. Returning empty result list.");
            return Collections.emptyList();
        }

        // Default to ROUND_ROBIN if strategy is null
        LoadBalancingStrategy strategy = loadBalancingStrategy != null ? loadBalancingStrategy : LoadBalancingStrategy.ROUND_ROBIN;
        
        // Create state for round-robin if needed
        final AtomicInteger roundRobinState = strategy == LoadBalancingStrategy.ROUND_ROBIN ? new AtomicInteger(0) : null;

        // Initialize quota tracker for cumulative token tracking
        final QuotaTracker quotaTracker = new SimpleQuotaTrackerImpl();

        String logMessage = timeoutDuration == null ?
                "Starting main-thread dispatch with load balancing of {} file batches (strategy = {}, clients = {}, no timeout)" :
                "Starting main-thread dispatch with load balancing of {} file batches (strategy = {}, clients = {}, timeout = {} ms)";

        if (tokensQuota != null) {
            logMessage = logMessage.replace("dispatch", "dispatch with token quota validation");
        }

        if (timeoutDuration == null) {
            logger.info(logMessage, resolvedFilePathBatches.size(), strategy, llmClients.size());
        } else {
            logger.info(logMessage, resolvedFilePathBatches.size(), strategy, llmClients.size(), timeoutDuration.toMillis());
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

        for (int i = 0; i < reviewPrompts.size(); i++) {
            if (timeoutDuration != null) {
                Duration elapsed = Duration.between(startTime, Instant.now());
                Duration remaining = timeoutDuration.minus(elapsed);

                if (remaining.isNegative() || remaining.isZero()) {
                    logger.warn("Timeout reached before processing prompt #{}", i);
                    throw new TaskExecutorTimeoutException(
                            String.format("Timeout after %d ms at prompt #%d", timeoutDuration.toMillis(), i)
                    );
                }
            }

            Instant reviewPromptStart = Instant.now();
            ReviewPrompt reviewPrompt = reviewPrompts.get(i);

            try {
                logger.debug("Processing prompt #{} with id: {}", i, reviewPrompt.getId());

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
                    // Track cumulative token usage after successful LLM call
                    if (reviewedResultItem.getCompletionUsage() != null) {
                        quotaTracker.addUsage(reviewedResultItem.getCompletionUsage());
                    }
                }

                results.add(reviewedResultItem);

                long reviewPromptDuration = Duration.between(reviewPromptStart, Instant.now()).toMillis();
                logger.info("Prompt #{} with id: {} completed successfully. Duration: {} ms.", i, reviewPrompt.getId(), reviewPromptDuration);

            } catch (Exception e) {
                logger.error("Prompt process #{} with id: {} failed with exception: '{}'", i, reviewPrompt.getId(), e.getMessage(), e);
                throw new LLMCodeReviewRuntimeException(String.format("Prompt #%d with id: %d failed: %s", i, reviewPrompt.getId(), e.getMessage()), e);
            }
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        // Log final quota usage if quota tracking was enabled
        if (tokensQuota != null) {
            ReviewedCompletionUsage finalUsage = quotaTracker.getCurrentUsage();
            logger.info("Final cumulative token usage: completion={}, prompt={}, total={}",
                    finalUsage.getCompletionTokens(), finalUsage.getPromptTokens(), finalUsage.getTotalTokens());
        }

        logger.info(
                "All prompts processed successfully with load balancing. Strategy: {}. Total reviewed: {}. Total duration: {} ms.",
                strategy,
                reviewPrompts.size(),
                totalDuration
        );

        return results;
    }
}
