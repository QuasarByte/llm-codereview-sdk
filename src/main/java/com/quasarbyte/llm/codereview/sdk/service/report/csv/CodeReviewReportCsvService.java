package com.quasarbyte.llm.codereview.sdk.service.report.csv;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

import java.time.format.DateTimeFormatter;

public interface CodeReviewReportCsvService {
    String generateCsvReport(ReviewResult reviewResult);
    String generateCsvReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter);
}
