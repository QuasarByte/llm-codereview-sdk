package com.quasarbyte.llm.codereview.sdk.service.report.markdown;

import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

import java.time.format.DateTimeFormatter;

public interface CodeReviewReportMarkdownService {
    String generateMarkdownReport(ReviewResult reviewResult);
    String generateMarkdownReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter);
}
