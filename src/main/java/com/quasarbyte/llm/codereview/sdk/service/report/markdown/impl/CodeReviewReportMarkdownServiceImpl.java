package com.quasarbyte.llm.codereview.sdk.service.report.markdown.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import com.quasarbyte.llm.codereview.sdk.service.report.markdown.CodeReviewReportMarkdownService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CodeReviewReportMarkdownServiceImpl implements CodeReviewReportMarkdownService {

    private static final DateTimeFormatter ISO_FULL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Generates a Markdown report from ReviewResult
     *
     * @param reviewResult The code review result object
     * @return Formatted markdown report as string
     */
    @Override
    public String generateMarkdownReport(ReviewResult reviewResult) {
        return generateMarkdownReport(reviewResult, ISO_FULL_FORMAT);
    }

    /**
     * Generates a Markdown report from ReviewResult with custom date format
     *
     * @param reviewResult      The code review result object
     * @param dateTimeFormatter Custom date formatter, if null uses ISO format
     * @return Formatted markdown report as string
     */
    @Override
    public String generateMarkdownReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(reviewResult, "reviewResult must not be null");
        Objects.requireNonNull(dateTimeFormatter, "dateTimeFormatter must not be null");
        Objects.requireNonNull(reviewResult.getItems(), "reviewResult.getItems() must not be null");

        StringBuilder report = new StringBuilder();

        // Header
        report.append("# Code Review Report\n\n");

        // Summary section
        appendSummary(report, reviewResult);

        // Files section
        appendFilesAnalysis(report, reviewResult, dateTimeFormatter);

        // Resource usage section (only if it has usage data)
        if (reviewResult.getCompletionUsage() != null) {
            report.append("\n---\n\n");
            report.append("## Resource Usage\n\n");
            appendResourceUsage(report, reviewResult);
        }

        return report.toString();
    }

    private void appendSummary(StringBuilder report, ReviewResult reviewResult) {
        int filesCount = reviewResult.getItems() != null ? reviewResult.getItems().size() : 0;
        int totalIssues = reviewResult.getItems() != null ?
                reviewResult.getItems().stream()
                        .mapToInt(item -> item != null && item.getComments() != null ? item.getComments().size() : 0)
                        .sum() : 0;

        long totalTokens = reviewResult.getCompletionUsage() != null ?
                reviewResult.getCompletionUsage().getTotalTokens() : 0;
        long completionTokens = reviewResult.getCompletionUsage() != null ?
                reviewResult.getCompletionUsage().getCompletionTokens() : 0;
        long promptTokens = reviewResult.getCompletionUsage() != null ?
                reviewResult.getCompletionUsage().getPromptTokens() : 0;

        report.append("## Summary\n\n");
        report.append("**Files Reviewed:** ").append(filesCount).append("  \n");
        report.append("**Total Issues Found:** ").append(totalIssues).append("  \n");

        if (reviewResult.getCompletionUsage() != null) {
            report.append("**Token Usage:** ").append(totalTokens)
                    .append(" total tokens (").append(completionTokens)
                    .append(" completion + ").append(promptTokens).append(" prompt)\n\n");
        }

        report.append("---\n\n");
    }

    private void appendFilesAnalysis(StringBuilder report, ReviewResult reviewResult, DateTimeFormatter dateFormatter) {
        report.append("## Files Analyzed\n\n");

        if (reviewResult.getItems() == null || reviewResult.getItems().isEmpty()) {
            report.append("No files were analyzed.\n\n");
            return;
        }

        int fileIndex = 1;
        int totalFiles = reviewResult.getItems().size();

        for (ReviewResultItem item : reviewResult.getItems()) {

            if (item == null || item.getFile() == null) {
                continue;
            }

            ReviewFile file = item.getFile();
            report.append("### ").append(fileIndex).append(". ").append(file.getFileName()).append("\n\n");

            // File details
            report.append("**File Details:**\n");
            report.append("- **Path:** `").append(file.getFilePath()).append("`\n");
            report.append("- **Size:** ").append(file.getSize()).append(" bytes\n");

            if (file.getCreatedAt() != null) {
                report.append("- **Created:** ").append(formatDateTime(file.getCreatedAt(), dateFormatter)).append("\n");
            }
            if (file.getModifiedAt() != null) {
                report.append("- **Modified:** ").append(formatDateTime(file.getModifiedAt(), dateFormatter)).append("\n");
            }

            // Issues for this file
            if (item.getComments() != null && !item.getComments().isEmpty()) {
                report.append("\n#### Issues Found\n\n");

                for (ReviewComment comment : item.getComments()) {
                    if (comment != null) {
                        appendComment(report, comment);
                    }
                }
            } else {
                report.append("\n✅ **No issues found in this file.**\n");
            }

            // Think steps section
            if (item.getThinkSteps() != null && !item.getThinkSteps().isEmpty()) {
                report.append("\n#### Reasoning Steps\n\n");
                for (ReviewThinkStep step : item.getThinkSteps()) {
                    if (step != null) {
                        report.append("- **File ID:** ").append(step.getFileId()).append("\n");
                        report.append("  - **File Name:** ").append(step.getFileName()).append("\n");
                        report.append("  - **Rule ID:** ").append(step.getRuleId()).append("\n");
                        report.append("  - **Rule Code:** ").append(step.getRuleCode()).append("\n");
                        report.append("  - **Step Text:** ").append(step.getThinkText()).append("\n\n");
                    }
                }
            }

            // Add separator only if not the last file
            if (fileIndex < totalFiles) {
                report.append("\n---\n\n");
            }

            fileIndex++;
        }
    }

    private void appendComment(StringBuilder report, ReviewComment comment) {
        // Additional protection against incorrect objects
        if (comment == null) {
            return;
        }

        String severityIcon;
        String severityText;

        try {
            severityIcon = getSeverityIcon(comment.getRule());
            severityText = getSeverityText(comment.getRule());
        } catch (Exception e) {
            // Fallback values if the object is corrupted
            severityIcon = "⚠️";
            severityText = "Unknown Issue";
        }

        report.append("**").append(severityIcon).append(" ").append(severityText);

        try {
            if (comment.getRule() != null && comment.getRule().getCode() != null) {
                report.append(" (Rule ").append(comment.getRule().getCode()).append(")");
            }
        } catch (Exception e) {
            // Skip if we can't get the rule code
        }

        report.append("**\n");

        Long ruleId = safeGetValue(comment::getRuleId, null);
        if (ruleId != null) {
            report.append("- **Rule ID:** ").append(ruleId).append("\n");
        }

        String ruleCode = safeGetValue(comment::getRuleCode, null);
        if (ruleCode != null) {
            report.append("- **Rule Code:** ").append(ruleCode).append("\n");
        }

        // Safe retrieval of values with fallback
        Object line = safeGetValue(comment::getLine, "?");
        Object column = safeGetValue(comment::getColumn, "?");

        report.append("- **Location:** Line ").append(line)
                .append(", Column ").append(column).append("\n");

        String ruleDescription = safeGetValue(() -> comment.getRule() != null ? comment.getRule().getDescription() : null, null);
        if (ruleDescription != null) {
            report.append("- **Rule:** ").append(ruleDescription).append("\n");
        }

        String message = safeGetValue(comment::getMessage, null);
        if (message != null) {
            report.append("- **Problem:** ").append(message).append("\n");
        }

        String suggestion = safeGetValue(comment::getSuggestion, null);
        if (suggestion != null) {
            report.append("- **Recommendation:** ").append(suggestion).append("\n");
        }

        report.append("\n");
    }

    /**
     * Safely extracts a value, returning fallback in case of error
     */
    private <T> T safeGetValue(java.util.function.Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallback;
        }
    }

    private void appendResourceUsage(StringBuilder report, ReviewResult reviewResult) {
        ReviewCompletionUsage usage = reviewResult.getCompletionUsage();

        report.append("- **Completion Tokens:** ").append(usage.getCompletionTokens()).append("\n");
        report.append("- **Prompt Tokens:** ").append(usage.getPromptTokens()).append("\n");
        report.append("- **Total Tokens:** ").append(usage.getTotalTokens()).append("\n");
    }

    private String getSeverityIcon(Rule rule) {
        if (rule == null || rule.getSeverity() == null) {
            return "⚠️";
        }

        switch (rule.getSeverity()) {
            case CRITICAL:
                return "🔴";
            case WARNING:
                return "🟡";
            case INFO:
                return "ℹ️";
            default:
                return "⚠️";
        }
    }

    private String getSeverityText(Rule rule) {
        if (rule == null || rule.getSeverity() == null) {
            return "Unknown";
        }
        return rule.getSeverity().name() + " Issue";
    }

    /**
     * Formats LocalDateTime to a more readable format
     * Handles formatting errors gracefully
     */
    private String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return "Unknown";
        }

        try {
            return dateTime.format(formatter);
        } catch (Exception e) {
            // Fallback to toString if formatting fails
            return dateTime.toString();
        }
    }
}