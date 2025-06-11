package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.statistics.SeverityStatistics;

public interface SeverityStatisticsCalculator {
    SeverityStatistics calculate(ReviewResult reviewResult);
}
