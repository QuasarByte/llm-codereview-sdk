package com.quasarbyte.llm.codereview.sdk.service.report.html;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

import java.time.format.DateTimeFormatter;

public interface CodeReviewReportHtmlService {
    String generateHtmlReport(ReviewResult reviewResult);
    String generateHtmlReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter);
}
