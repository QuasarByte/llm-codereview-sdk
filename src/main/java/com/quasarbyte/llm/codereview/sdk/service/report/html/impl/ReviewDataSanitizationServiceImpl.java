package com.quasarbyte.llm.codereview.sdk.service.report.html.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewComment;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResultItem;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewThinkStep;
import com.quasarbyte.llm.codereview.sdk.service.html.HtmlSanitizerService;
import com.quasarbyte.llm.codereview.sdk.service.report.html.ReviewDataSanitizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for sanitizing LLM-generated content in ReviewResult
 * Creates a clean copy with all potentially dangerous content sanitized
 * <p>
 * This approach separates security concerns from presentation logic
 */
public class ReviewDataSanitizationServiceImpl implements ReviewDataSanitizationService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewDataSanitizationServiceImpl.class);

    private final HtmlSanitizerService htmlSanitizer;

    public ReviewDataSanitizationServiceImpl(HtmlSanitizerService htmlSanitizer) {
        this.htmlSanitizer = htmlSanitizer;
    }

    /**
     * Creates a sanitized copy of ReviewResult with all LLM-generated text content cleaned
     *
     * @param original Original ReviewResult from LLM
     * @return Sanitized copy safe for use in reports
     */
    public ReviewResult sanitize(ReviewResult original) {
        if (original == null) {
            return null;
        }

        logger.debug("Sanitizing ReviewResult with {} items",
                original.getItems() != null ? original.getItems().size() : 0);

        return new ReviewResult()
                .setItems(sanitizeItems(original.getItems()))
                .setCompletionUsage(original.getCompletionUsage());
    }

    private List<ReviewResultItem> sanitizeItems(List<ReviewResultItem> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(this::sanitizeItem)
                .collect(Collectors.toList());
    }

    private ReviewResultItem sanitizeItem(ReviewResultItem item) {
        if (item == null) {
            return null;
        }

        return new ReviewResultItem()
                .setFile(item.getFile()) // File metadata is trusted (from your system)
                .setComments(sanitizeComments(item.getComments()))
                .setThinkSteps(sanitizeThinkSteps(item.getThinkSteps()));
    }

    private List<ReviewComment> sanitizeComments(List<ReviewComment> comments) {
        if (comments == null) {
            return null;
        }

        return comments.stream()
                .map(this::sanitizeComment)
                .collect(Collectors.toList());
    }

    private ReviewComment sanitizeComment(ReviewComment comment) {
        if (comment == null) {
            return null;
        }

        return new ReviewComment()
                .setRule(sanitizeRule(comment.getRule()))
                .setRuleId(comment.getRuleId())
                .setRuleCode(htmlSanitizer.sanitize(comment.getRuleCode()))
                .setLine(comment.getLine())
                .setColumn(comment.getColumn())
                .setMessage(htmlSanitizer.sanitize(comment.getMessage()))
                .setSuggestion(htmlSanitizer.sanitize(comment.getSuggestion()));
    }

    private Rule sanitizeRule(Rule rule) {
        if (rule == null) {
            return null;
        }

        return new Rule()
                .setCode(htmlSanitizer.sanitize(rule.getCode()))
                .setDescription(htmlSanitizer.sanitize(rule.getDescription()))
                .setSeverity(rule.getSeverity());
    }

    private List<ReviewThinkStep> sanitizeThinkSteps(List<ReviewThinkStep> steps) {
        if (steps == null) {
            return null;
        }

        return steps.stream()
                .map(this::sanitizeThinkStep)
                .collect(Collectors.toList());
    }

    private ReviewThinkStep sanitizeThinkStep(ReviewThinkStep step) {
        if (step == null) {
            return null;
        }

        return new ReviewThinkStep()
                .setFileId(step.getFileId())
                .setFileName(htmlSanitizer.sanitize(step.getFileName()))
                .setRuleId(step.getRuleId())
                .setRuleCode(htmlSanitizer.sanitize(step.getRuleCode()))
                .setThinkText(htmlSanitizer.sanitize(step.getThinkText()));
    }
}