package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedResult;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedComment;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;
import com.quasarbyte.llm.codereview.sdk.service.ReviewDetailsService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewParallelExecutionService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewResultAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReviewParallelExecutionServiceImpl implements ReviewParallelExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewParallelExecutionServiceImpl.class);

    private final ReviewDetailsService reviewDetailsService;
    private final ReviewResultAggregator reviewResultAggregator;

    public ReviewParallelExecutionServiceImpl(ReviewDetailsService reviewDetailsService, ReviewResultAggregator reviewResultAggregator) {
        this.reviewDetailsService = reviewDetailsService;
        this.reviewResultAggregator = reviewResultAggregator;
        logger.debug("ReviewParallelExecutionServiceImpl initialized.");
    }

    @Override
    public ReviewResult review(ReviewParameter reviewParameter, LlmClient llmClient, ParallelExecutionParameter parallelExecutionParameter) {
        logger.info("Starting parallel review process.");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");
        Objects.requireNonNull(parallelExecutionParameter, "parallelExecutionParameter must not be null");

        logger.debug("Invoking reviewDetailsService.review(...)");
        ReviewedDetailedResult reviewedDetailedResult = reviewDetailsService.review(reviewParameter, llmClient, parallelExecutionParameter);

        logger.debug("Aggregating reviewed result.");
        AggregatedResult aggregatedResult = reviewResultAggregator.aggregate(reviewedDetailedResult);

        logger.debug("Mapping aggregated files to ReviewResultItem list.");
        List<ReviewResultItem> reviewResultItems = aggregatedResult.getFiles()
                .stream()
                .sorted(Comparator.comparing(aggregatedFile -> aggregatedFile.getSourceFile().getFilePath()))
                .map(ReviewParallelExecutionServiceImpl::mapAggregatedFileToReviewResultItem)
                .collect(Collectors.toList());

        logger.info("Parallel review completed: {} files processed.", reviewResultItems.size());

        return new ReviewResult()
                .setItems(reviewResultItems)
                .setCompletionUsage(new ReviewCompletionUsage()
                        .setCompletionTokens(aggregatedResult.getCompletionUsage().getCompletionTokens())
                        .setPromptTokens(aggregatedResult.getCompletionUsage().getPromptTokens())
                        .setTotalTokens(aggregatedResult.getCompletionUsage().getTotalTokens())
                );
    }

    private static ReviewResultItem mapAggregatedFileToReviewResultItem(AggregatedFile aggregatedFile) {
        logger.debug("Mapping AggregatedFile '{}' to ReviewResultItem.", aggregatedFile.getSourceFile().getFilePath());
        ReviewFile reviewFile = mapSourceFileToReviewFile(aggregatedFile.getSourceFile());
        List<ReviewComment> comments = aggregatedFile.getComments().stream()
                .map(ReviewParallelExecutionServiceImpl::mapAggregatedCommentToReviewComment)
                .collect(Collectors.toList());
        logger.debug("Mapped {} comments for file '{}'.", comments.size(), aggregatedFile.getSourceFile().getFilePath());
        return new ReviewResultItem()
                .setFile(reviewFile)
                .setComments(comments);
    }

    private static ReviewFile mapSourceFileToReviewFile(SourceFile sourceFile) {
        logger.trace("Mapping SourceFile '{}' to ReviewFile.", sourceFile.getFilePath());
        return new ReviewFile()
                .setFileName(sourceFile.getFileName())
                .setFilePath(sourceFile.getFilePath())
                .setSize(sourceFile.getSize())
                .setCreatedAt(sourceFile.getCreatedAt())
                .setModifiedAt(sourceFile.getModifiedAt())
                .setAccessedAt(sourceFile.getAccessedAt());
    }

    private static ReviewComment mapAggregatedCommentToReviewComment(ReviewedComment comment) {
        logger.trace("Mapping ReviewedComment (line {}, column {}) to ReviewComment.", comment.getLine(), comment.getColumn());
        Rule rule = new Rule()
                .setCode(comment.getRule().getRuleKey().getCode())
                .setDescription(comment.getRule().getDescription())
                .setSeverity(comment.getRule().getSeverity());

        return new ReviewComment()
                .setRule(rule)
                .setLine(comment.getLine())
                .setColumn(comment.getColumn())
                .setMessage(comment.getMessage())
                .setSuggestion(comment.getSuggestion());
    }
}
