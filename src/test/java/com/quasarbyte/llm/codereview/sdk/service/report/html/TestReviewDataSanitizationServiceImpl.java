package com.quasarbyte.llm.codereview.sdk.service.report.html;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.report.html.ReviewDataSanitizationService;

/**
 * Simple test implementation of ReviewDataSanitizationService 
 * that returns data unchanged for testing purposes.
 */
class TestReviewDataSanitizationServiceImpl implements ReviewDataSanitizationService {
    
    @Override
    public ReviewResult sanitize(ReviewResult reviewResult) {
        // For testing purposes, return the data unchanged
        // In a real implementation, this would sanitize all LLM-generated content
        return reviewResult;
    }
}