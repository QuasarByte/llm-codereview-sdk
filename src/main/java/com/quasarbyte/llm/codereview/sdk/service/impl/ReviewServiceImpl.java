package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedResult;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedComment;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;
import com.quasarbyte.llm.codereview.sdk.service.ReviewDetailsService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewResultAggregator;
import com.quasarbyte.llm.codereview.sdk.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDetailsService reviewDetailsService;
    private final ReviewResultAggregator reviewResultAggregator;

    public ReviewServiceImpl(ReviewDetailsService reviewDetailsService, ReviewResultAggregator reviewResultAggregator) {
        this.reviewDetailsService = reviewDetailsService;
        this.reviewResultAggregator = reviewResultAggregator;
        logger.debug("ReviewServiceImpl initialized.");
    }

    @Override
    public ReviewResult review(ReviewParameter reviewParameter, LlmClient llmClient) {
        logger.info("Starting review process.");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");

        logger.debug("Invoking reviewDetailsService.review(...)");
        ReviewedDetailedResult reviewedDetailedResult = reviewDetailsService.review(reviewParameter, llmClient);

        logger.debug("Aggregating reviewed result.");
        AggregatedResult aggregatedResult = reviewResultAggregator.aggregate(reviewedDetailedResult);

        logger.debug("Mapping aggregated files to ReviewResultItem list.");
        List<ReviewResultItem> reviewResultItems = aggregatedResult.getFiles()
                .stream()
                .sorted(Comparator.comparing(aggregatedFile -> aggregatedFile.getSourceFile().getFilePath()))
                .map(ReviewServiceImpl::mapAggregatedFileToReviewResultItem)
                .collect(Collectors.toList());

        logger.info("Review completed: {} files processed.", reviewResultItems.size());

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
        List<ReviewComment> comments = aggregatedFile
                .getComments()
                .stream()
                .map(ReviewServiceImpl::mapAggregatedCommentToReviewComment)
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
        if (comment == null) {
            logger.warn("ReviewedComment is null; returning empty ReviewComment.");
            return new ReviewComment();
        }
        Optional<ReviewedComment> reviewedCommentOptional = Optional.of(comment);
        Optional<PromptRule> promptRuleOptional = reviewedCommentOptional.map(ReviewedComment::getRule);

        Rule rule = new Rule()
                .setCode(promptRuleOptional.map(PromptRule::getRuleKey).map(RuleKey::getCode).orElse(null))
                .setDescription(promptRuleOptional.map(PromptRule::getDescription).orElse(null))
                .setSeverity(promptRuleOptional.map(PromptRule::getSeverity).orElse(null));

        logger.trace("Mapping ReviewedComment at line {} col {} to ReviewComment.",
                reviewedCommentOptional.map(ReviewedComment::getLine).orElse(null),
                reviewedCommentOptional.map(ReviewedComment::getColumn).orElse(null));

        return new ReviewComment()
                .setRule(rule)
                .setLine(reviewedCommentOptional.map(ReviewedComment::getLine).orElse(null))
                .setColumn(reviewedCommentOptional.map(ReviewedComment::getColumn).orElse(null))
                .setMessage(reviewedCommentOptional.map(ReviewedComment::getMessage).orElse(null))
                .setSuggestion(reviewedCommentOptional.map(ReviewedComment::getSuggestion).orElse(null));
    }
}
