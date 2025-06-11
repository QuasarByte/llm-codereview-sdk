package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import com.quasarbyte.llm.codereview.sdk.service.QuotaTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple quota tracker for single-threaded execution
 */
public class SimpleQuotaTrackerImpl implements QuotaTracker {
    private static final Logger logger = LoggerFactory.getLogger(SimpleQuotaTrackerImpl.class);

    private long totalCompletionTokens = 0;
    private long totalPromptTokens = 0;
    private long totalTokens = 0;

    public SimpleQuotaTrackerImpl() {

    }

    @Override
    public void addUsage(ReviewedCompletionUsage usage) {
        if (usage == null) {
            return;
        }

        // Update counters - LLM consumed tokens
        totalCompletionTokens += (usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0);
        totalPromptTokens += (usage.getPromptTokens() != null ? usage.getPromptTokens() : 0);
        totalTokens += (usage.getTotalTokens() != null ? usage.getTotalTokens() : 0);

        logger.debug("Updated cumulative token usage: completion={}, prompt={}, total={}",
                totalCompletionTokens, totalPromptTokens, totalTokens);
    }

    @Override
    public ReviewedCompletionUsage getCurrentUsage() {
        return new ReviewedCompletionUsage()
                .setCompletionTokens(totalCompletionTokens)
                .setPromptTokens(totalPromptTokens)
                .setTotalTokens(totalTokens);
    }
}
