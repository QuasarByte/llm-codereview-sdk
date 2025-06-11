package com.quasarbyte.llm.codereview.sdk.service.report.csv.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewComment;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewFile;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResultItem;
import com.quasarbyte.llm.codereview.sdk.service.report.csv.CodeReviewReportCsvService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CodeReviewReportCsvServiceImpl implements CodeReviewReportCsvService {

    private static final DateTimeFormatter ISO_FULL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_LINE_SEPARATOR = "\n";

    /**
     * Generates a CSV report from ReviewResult
     *
     * @param reviewResult The code review result object
     * @return Formatted CSV report as string
     */
    @Override
    public String generateCsvReport(ReviewResult reviewResult) {
        return generateCsvReport(reviewResult, ISO_FULL_FORMAT);
    }

    /**
     * Generates a CSV report from ReviewResult with custom date format
     *
     * @param reviewResult      The code review result object
     * @param dateTimeFormatter Custom date formatter, if null uses ISO format
     * @return Formatted CSV report as string
     */
    @Override
    public String generateCsvReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(reviewResult, "reviewResult must not be null");
        Objects.requireNonNull(dateTimeFormatter, "dateTimeFormatter must not be null");
        Objects.requireNonNull(reviewResult.getItems(), "reviewResult.getItems() must not be null");

        StringBuilder csv = new StringBuilder();

        // CSV Header
        appendCsvHeader(csv);

        // Process each file and its issues
        if (reviewResult.getItems() != null && !reviewResult.getItems().isEmpty()) {
            for (ReviewResultItem item : reviewResult.getItems()) {
                if (item == null || item.getFile() == null) {
                    continue;
                }

                appendFileData(csv, item, dateTimeFormatter);
            }
        }

        return csv.toString();
    }

    private void appendCsvHeader(StringBuilder csv) {
        csv.append("file_path").append(CSV_SEPARATOR)
                .append("file_name").append(CSV_SEPARATOR)
                .append("file_size_bytes").append(CSV_SEPARATOR)
                .append("file_created_at").append(CSV_SEPARATOR)
                .append("file_modified_at").append(CSV_SEPARATOR)
                .append("issue_severity").append(CSV_SEPARATOR)
                .append("issue_severity_level").append(CSV_SEPARATOR)
                .append("rule_code").append(CSV_SEPARATOR)
                .append("rule_description").append(CSV_SEPARATOR)
                .append("issue_line").append(CSV_SEPARATOR)
                .append("issue_column").append(CSV_SEPARATOR)
                .append("issue_message").append(CSV_SEPARATOR)
                .append("issue_suggestion").append(CSV_LINE_SEPARATOR);
    }

    private void appendFileData(StringBuilder csv, ReviewResultItem item, DateTimeFormatter dateFormatter) {
        ReviewFile file = item.getFile();

        // Extract file information once
        String filePath = safeGetValue(file::getFilePath, "");
        String fileName = safeGetValue(file::getFileName, "");
        String fileSize = String.valueOf(safeGetValue(file::getSize, 0L));
        String createdAt = formatDateTime(file.getCreatedAt(), dateFormatter);
        String modifiedAt = formatDateTime(file.getModifiedAt(), dateFormatter);

        // If file has issues, add a row for each issue
        if (item.getComments() != null && !item.getComments().isEmpty()) {
            for (ReviewComment comment : item.getComments()) {
                if (comment != null) {
                    appendIssueRow(csv, filePath, fileName, fileSize, createdAt, modifiedAt, comment);
                }
            }
        } else {
            // If no issues, add a single row with empty issue fields
            appendFileOnlyRow(csv, filePath, fileName, fileSize, createdAt, modifiedAt);
        }
    }

    private void appendIssueRow(StringBuilder csv, String filePath, String fileName, String fileSize,
                                String createdAt, String modifiedAt, ReviewComment comment) {

        // File information (wrap all string fields in quotes)
        csv.append(escapeStringField(filePath)).append(CSV_SEPARATOR)
                .append(escapeStringField(fileName)).append(CSV_SEPARATOR)
                .append(fileSize).append(CSV_SEPARATOR)  // Number - no quotes
                .append(escapeStringField(createdAt)).append(CSV_SEPARATOR)
                .append(escapeStringField(modifiedAt)).append(CSV_SEPARATOR);

        // Issue information
        String severity = getSeverityText(comment.getRule());
        int severityLevel = getSeverityLevel(comment.getRule());
        String ruleCode = safeGetValue(() ->
                comment.getRule() != null ? comment.getRule().getCode() : "", "");
        String ruleDescription = safeGetValue(() ->
                comment.getRule() != null ? comment.getRule().getDescription() : "", "");
        String line = String.valueOf(safeGetValue(comment::getLine, 0));
        String column = String.valueOf(safeGetValue(comment::getColumn, 0));
        String message = safeGetValue(comment::getMessage, "");
        String suggestion = safeGetValue(comment::getSuggestion, "");

        csv.append(escapeStringField(severity)).append(CSV_SEPARATOR)
                .append(severityLevel).append(CSV_SEPARATOR)  // Number - no quotes
                .append(escapeStringField(ruleCode)).append(CSV_SEPARATOR)
                .append(escapeStringField(ruleDescription)).append(CSV_SEPARATOR)
                .append(line).append(CSV_SEPARATOR)  // Number - no quotes
                .append(column).append(CSV_SEPARATOR)  // Number - no quotes
                .append(escapeStringField(message)).append(CSV_SEPARATOR)
                .append(escapeStringField(suggestion)).append(CSV_LINE_SEPARATOR);
    }

    private void appendFileOnlyRow(StringBuilder csv, String filePath, String fileName, String fileSize,
                                   String createdAt, String modifiedAt) {
        // File information (wrap all string fields in quotes)
        csv.append(escapeStringField(filePath)).append(CSV_SEPARATOR)
                .append(escapeStringField(fileName)).append(CSV_SEPARATOR)
                .append(fileSize).append(CSV_SEPARATOR)  // Number - no quotes
                .append(escapeStringField(createdAt)).append(CSV_SEPARATOR)
                .append(escapeStringField(modifiedAt)).append(CSV_SEPARATOR);

        // Empty issue fields (wrap all string fields in quotes)
        csv.append(escapeStringField("CLEAN")).append(CSV_SEPARATOR)
                .append("0").append(CSV_SEPARATOR)       // Number - no quotes
                .append(escapeStringField("")).append(CSV_SEPARATOR)
                .append(escapeStringField("")).append(CSV_SEPARATOR)
                .append("0").append(CSV_SEPARATOR)       // Number - no quotes
                .append("0").append(CSV_SEPARATOR)       // Number - no quotes
                .append(escapeStringField("")).append(CSV_SEPARATOR)
                .append(escapeStringField("")).append(CSV_LINE_SEPARATOR);
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

    private String getSeverityText(Rule rule) {
        if (rule == null || rule.getSeverity() == null) {
            return "UNKNOWN";
        }
        return rule.getSeverity().name();
    }

    private int getSeverityLevel(Rule rule) {
        if (rule == null || rule.getSeverity() == null) {
            return 999; // Unknown gets the highest number for sorting
        }

        switch (rule.getSeverity()) {
            case CRITICAL:
                return 1;
            case WARNING:
                return 2;
            case INFO:
                return 3;
            default:
                return 999;
        }
    }

    /**
     * Formats LocalDateTime to a more readable format
     * Handles formatting errors gracefully
     */
    private String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) {
            return "";
        }

        try {
            return dateTime.format(formatter);
        } catch (Exception e) {
            // Fallback to toString if formatting fails
            return dateTime.toString();
        }
    }

    /**
     * Wraps string fields in quotes for better CSV compatibility and data preservation
     * Always escapes internal quotes by doubling them
     */
    private String escapeStringField(String field) {
        if (field == null) {
            return "\"\"";
        }

        // Always wrap string fields in quotes and escape internal quotes
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}