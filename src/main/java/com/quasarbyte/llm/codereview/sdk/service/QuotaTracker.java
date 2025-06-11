package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedCompletionUsage;

public interface QuotaTracker {

    void addUsage(ReviewedCompletionUsage usage);

    ReviewedCompletionUsage getCurrentUsage();
}
