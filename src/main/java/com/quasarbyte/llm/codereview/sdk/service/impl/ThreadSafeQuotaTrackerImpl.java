package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;
import com.quasarbyte.llm.codereview.sdk.service.QuotaTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe quota tracker for parallel execution
 */
public class ThreadSafeQuotaTrackerImpl implements QuotaTracker {

    private static final Logger logger = LoggerFactory.getLogger(ThreadSafeQuotaTrackerImpl.class);

    private long totalCompletionTokens = 0;
    private long totalPromptTokens = 0;
    private long totalTokens = 0;

    public ThreadSafeQuotaTrackerImpl() {

    }

    @Override
    public synchronized void addUsage(ReviewedCompletionUsage usage) {
        if (usage == null) {
            return;
        }

        // Extract the values to add, handling null values
        long completionTokensToAdd = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
        long promptTokensToAdd = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
        long totalTokensToAdd = usage.getTotalTokens() != null ? usage.getTotalTokens() : 0;

        // Update counters - LLM consumed tokens
        totalCompletionTokens += completionTokensToAdd;
        totalPromptTokens += promptTokensToAdd;
        totalTokens += totalTokensToAdd;

        logger.debug("Updated cumulative token usage: completion={}, prompt={}, total={}",
                totalCompletionTokens, totalPromptTokens, totalTokens);
    }

    @Override
    public synchronized ReviewedCompletionUsage getCurrentUsage() {
        return new ReviewedCompletionUsage()
                .setCompletionTokens(totalCompletionTokens)
                .setPromptTokens(totalPromptTokens)
                .setTotalTokens(totalTokens);
    }
}
