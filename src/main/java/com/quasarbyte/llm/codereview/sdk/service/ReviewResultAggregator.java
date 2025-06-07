package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedResult;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;

public interface ReviewResultAggregator {
    AggregatedResult aggregate(ReviewedDetailedResult result);
}
