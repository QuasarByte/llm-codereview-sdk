package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewException;
import com.quasarbyte.llm.codereview.sdk.exception.LlmRequestQuotaException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedReviewConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedExecutionDetails;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedFile;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ReviewDetailsServiceImpl implements ReviewDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDetailsServiceImpl.class);

    private final LlmRequestQuotaGuard llmRequestQuotaGuard;
    private final MultiThreadTaskDispatcher multiThreadTaskDispatcher;
    private final ResolvedFilePathSplitter resolvedFilePathSplitter;
    private final ReviewConfigurationResolver reviewConfigurationResolver;
    private final RulesMerger rulesMerger;
    private final RulesToBatchesSplitter rulesToBatchesSplitter;
    private final SingleThreadTaskDispatcher singleThreadTaskDispatcher;

    public ReviewDetailsServiceImpl(LlmRequestQuotaGuard llmRequestQuotaGuard,
                                    MultiThreadTaskDispatcher multiThreadTaskDispatcher,
                                    ResolvedFilePathSplitter resolvedFilePathSplitter,
                                    ReviewConfigurationResolver reviewConfigurationResolver,
                                    RulesMerger rulesMerger,
                                    RulesToBatchesSplitter rulesToBatchesSplitter,
                                    SingleThreadTaskDispatcher singleThreadTaskDispatcher) {
        this.llmRequestQuotaGuard = llmRequestQuotaGuard;
        this.multiThreadTaskDispatcher = multiThreadTaskDispatcher;
        this.resolvedFilePathSplitter = resolvedFilePathSplitter;
        this.reviewConfigurationResolver = reviewConfigurationResolver;
        this.rulesMerger = rulesMerger;
        this.rulesToBatchesSplitter = rulesToBatchesSplitter;
        this.singleThreadTaskDispatcher = singleThreadTaskDispatcher;
        logger.debug("Initialized ReviewDetailsServiceImpl");
    }

    @Override
    public ReviewedDetailedResult review(ReviewParameter reviewParameter, LlmClient llmClient) {
        logger.info("Starting review with single-threaded dispatcher");
        Objects.requireNonNull(reviewParameter, "ReviewParameter must not be null");
        Objects.requireNonNull(llmClient, "LlmClient must not be null");

        logger.debug("Resolving review configuration");
        ResolvedReviewConfiguration resolvedReviewConfiguration = reviewConfigurationResolver.resolve(reviewParameter);

        final List<ReviewedFile> files;
        final List<List<ResolvedFilePath>> resolvedFilePaths;
        final List<ReviewedResultItem> reviewedResultItems;

        if (resolvedReviewConfiguration.getResolvedReviewTargets().isEmpty()) {
            logger.warn("No resolved review targets found; skipping review.");
            reviewedResultItems = Collections.emptyList();
            files = Collections.emptyList();
            resolvedFilePaths = Collections.emptyList();
        } else {

            logger.debug("Splitting resolved file paths");
            List<List<ResolvedFilePath>> resolvedFilePathBatches = resolvedFilePathSplitter.split(resolvedReviewConfiguration.getResolvedReviewTargets());
            logger.debug("File path batches created: {}", resolvedFilePathBatches.size());

            List<Rule> mergedRules = getMergedRules(reviewParameter);
            logger.debug("Merged {} rules for review", mergedRules.size());

            List<List<Rule>> rulesBatches = rulesToBatchesSplitter.split(mergedRules, reviewParameter.getRulesBatchSize());
            logger.debug("Rules batches created: {}", rulesBatches.size());

            if (resolvedFilePathBatches.isEmpty()) {
                logger.warn("No file path batches; skipping review.");
                reviewedResultItems = Collections.emptyList();
                files = Collections.emptyList();
                resolvedFilePaths = Collections.emptyList();
            } else {

                if (resolvedFilePathBatches.stream().allMatch(List::isEmpty)) {
                    logger.warn("All file path batches are empty; skipping review.");
                    reviewedResultItems = Collections.emptyList();
                    files = Collections.emptyList();
                } else {

                    LlmQuota llmQuota = reviewParameter.getLlmQuota();
                    if (llmQuota != null && llmQuota.getRequestQuota() != null && llmQuota.getRequestQuota() > 0L) {
                        logger.debug("Checking LLM quota: requestQuota={}, batches={}, filePathBatches={}",
                                llmQuota.getRequestQuota(), rulesBatches.size(), resolvedFilePathBatches.size());
                        long plannedRequestValue = llmRequestQuotaGuard.plannedRequestValue(rulesBatches.size(), resolvedFilePathBatches.size(), llmQuota.getRequestQuota());
                        if (plannedRequestValue < llmQuota.getRequestQuota()) {
                            logger.error("Planned LLM request ({}) exceeds quota ({})", plannedRequestValue, llmQuota.getRequestQuota());
                            throw new LlmRequestQuotaException(String.format(
                                    "Planned llm request is greater than allowed by llm request quota. Planned request count %d, llm request quota %d",
                                    plannedRequestValue, llmQuota.getRequestQuota()));
                        }
                        logger.debug("LLM quota check passed.");
                    }

                    logger.info("Dispatching review tasks using {}", reviewParameter.getTimeoutDuration() == null ? "single-thread dispatcher (no timeout)" : "single-thread dispatcher (with timeout)");
                    if (reviewParameter.getTimeoutDuration() == null) {
                        reviewedResultItems = singleThreadTaskDispatcher.dispatch(
                                rulesBatches,
                                resolvedFilePathBatches,
                                reviewParameter.getLlmChatCompletionConfiguration(),
                                reviewParameter.getLlmMessagesMapperConfiguration(),
                                llmClient);
                    } else {
                        reviewedResultItems = singleThreadTaskDispatcher.dispatch(
                                rulesBatches,
                                resolvedFilePathBatches,
                                reviewParameter.getLlmChatCompletionConfiguration(),
                                reviewParameter.getLlmMessagesMapperConfiguration(),
                                llmClient,
                                reviewParameter.getTimeoutDuration());
                    }

                    files = reviewedResultItems.stream()
                            .map(ReviewedResultItem::getFiles)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());

                    logger.info("Review finished: {} items reviewed, {} files processed.", reviewedResultItems.size(), files.size());
                }
                resolvedFilePaths = resolvedFilePathBatches;
            }
        }

        logger.debug("Returning ReviewedDetailedResult");
        return new ReviewedDetailedResult()
                .setExecutionDetails(new ReviewedExecutionDetails()
                        .setResolvedFilePaths(resolvedFilePaths)
                        .setReviewResultItems(reviewedResultItems))
                .setFiles(files);
    }

    @Override
    public ReviewedDetailedResult review(ReviewParameter reviewParameter, LlmClient llmClient, ParallelExecutionParameter parallelExecutionParameter) {
        logger.info("Starting review with multi-threaded dispatcher");
        Objects.requireNonNull(reviewParameter, "ReviewParameter must not be null");
        Objects.requireNonNull(llmClient, "LlmClient must not be null");
        Objects.requireNonNull(parallelExecutionParameter, "ParallelExecutionParameter must not be null");
        Objects.requireNonNull(parallelExecutionParameter.getBatchSize(), "BatchSize must not be null in ParallelExecutionParameter");
        Objects.requireNonNull(parallelExecutionParameter.getExecutorService(), "ExecutorService must not be null in ParallelExecutionParameter");

        logger.debug("Resolving review configuration");
        ResolvedReviewConfiguration resolvedReviewConfiguration = reviewConfigurationResolver.resolve(reviewParameter);

        final List<ReviewedFile> files;
        final List<List<ResolvedFilePath>> resolvedFilePaths;
        final List<ReviewedResultItem> reviewedResultItems;

        if (resolvedReviewConfiguration.getResolvedReviewTargets().isEmpty()) {
            logger.warn("No resolved review targets found; skipping review.");
            reviewedResultItems = Collections.emptyList();
            files = Collections.emptyList();
            resolvedFilePaths = Collections.emptyList();
        } else {

            logger.debug("Splitting resolved file paths");
            List<List<ResolvedFilePath>> resolvedFilePathBatches = resolvedFilePathSplitter.split(resolvedReviewConfiguration.getResolvedReviewTargets());
            logger.debug("File path batches created: {}", resolvedFilePathBatches.size());

            List<Rule> mergedRules = getMergedRules(reviewParameter);
            logger.debug("Merged {} rules for review", mergedRules.size());

            List<List<Rule>> rulesBatches = rulesToBatchesSplitter.split(mergedRules, reviewParameter.getRulesBatchSize());
            logger.debug("Rules batches created: {}", rulesBatches.size());

            if (resolvedFilePathBatches.isEmpty()) {
                logger.warn("No file path batches; skipping review.");
                reviewedResultItems = Collections.emptyList();
                files = Collections.emptyList();
                resolvedFilePaths = Collections.emptyList();
            } else {

                if (resolvedFilePathBatches.stream().allMatch(List::isEmpty)) {
                    logger.warn("All file path batches are empty; skipping review.");
                    reviewedResultItems = Collections.emptyList();
                    files = Collections.emptyList();
                } else {
                    final int llmInvocationBatchSize = parallelExecutionParameter.getBatchSize();
                    logger.debug("Using LLM invocation batch size: {}", llmInvocationBatchSize);

                    if (llmInvocationBatchSize >= 1) {
                        logger.info("Dispatching review tasks using {}", reviewParameter.getTimeoutDuration() == null ? "multi-thread dispatcher (no timeout)" : "multi-thread dispatcher (with timeout)");

                        if (reviewParameter.getTimeoutDuration() == null) {
                            reviewedResultItems = multiThreadTaskDispatcher.dispatch(
                                    rulesBatches,
                                    resolvedFilePathBatches,
                                    reviewParameter.getLlmChatCompletionConfiguration(),
                                    reviewParameter.getLlmMessagesMapperConfiguration(),
                                    llmClient,
                                    llmInvocationBatchSize,
                                    parallelExecutionParameter.getExecutorService());
                        } else {
                            reviewedResultItems = multiThreadTaskDispatcher.dispatch(
                                    rulesBatches,
                                    resolvedFilePathBatches,
                                    reviewParameter.getLlmChatCompletionConfiguration(),
                                    reviewParameter.getLlmMessagesMapperConfiguration(),
                                    llmClient,
                                    llmInvocationBatchSize,
                                    reviewParameter.getTimeoutDuration(),
                                    parallelExecutionParameter.getExecutorService());
                        }

                    } else {
                        logger.error("Llm invocation batch size is less than 1: {}", llmInvocationBatchSize);
                        throw new LLMCodeReviewException("Llm invocation batch size can not be less than 1");
                    }

                    files = reviewedResultItems.stream()
                            .map(ReviewedResultItem::getFiles)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());

                    logger.info("Review finished: {} items reviewed, {} files processed.", reviewedResultItems.size(), files.size());
                }
                resolvedFilePaths = resolvedFilePathBatches;
            }
        }

        logger.debug("Returning ReviewedDetailedResult");
        return new ReviewedDetailedResult()
                .setExecutionDetails(new ReviewedExecutionDetails()
                        .setResolvedFilePaths(resolvedFilePaths)
                        .setReviewResultItems(reviewedResultItems))
                .setFiles(files);
    }

    private List<Rule> getMergedRules(ReviewParameter reviewParameter) {
        logger.debug("Merging rules from ReviewParameter and its targets/fileGroups.");
        List<Rule> merged = rulesMerger.merge(
                Arrays.asList(
                        // Null-safe getRules()
                        Optional.ofNullable(reviewParameter.getRules()).orElse(Collections.emptyList()),

                        // Null-safe getTargets() + getRules() per target
                        Optional.ofNullable(reviewParameter.getTargets())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(target -> Optional.ofNullable(target.getRules()).orElse(Collections.emptyList()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()),

                        // Null-safe getTargets() + getFileGroups() + getRules() per fileGroup
                        Optional.ofNullable(reviewParameter.getTargets())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(target -> Optional.ofNullable(target.getFileGroups()).orElse(Collections.emptyList()))
                                .flatMap(Collection::stream)
                                .map(fileGroup -> Optional.ofNullable(fileGroup.getRules()).orElse(Collections.emptyList()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList())
                )
        );
        logger.debug("Merged rules count: {}", merged.size());
        return merged;
    }
}
