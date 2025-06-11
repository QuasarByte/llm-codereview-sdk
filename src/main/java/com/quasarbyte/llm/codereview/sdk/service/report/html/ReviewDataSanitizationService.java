package com.quasarbyte.llm.codereview.sdk.service.report.html;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

/**
 * Service responsible for sanitizing LLM-generated content in review results
 * to prevent XSS and other security issues in HTML reports.
 */
public interface ReviewDataSanitizationService {

    /**
     * Sanitizes all LLM-generated content in the review result.
     * This includes comments, suggestions, reasoning steps, and other text content.
     *
     * @param reviewResult the review result to sanitize
     * @return sanitized review result with clean content
     */
    ReviewResult sanitize(ReviewResult reviewResult);
}