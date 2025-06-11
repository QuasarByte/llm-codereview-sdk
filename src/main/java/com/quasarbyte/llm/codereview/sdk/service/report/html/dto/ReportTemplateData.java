package com.quasarbyte.llm.codereview.sdk.service.report.html.dto;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Clean template data class that works with pre-sanitized ReviewResult
 * No sanitization logic here - just data transformation for presentation
 * Uses methods instead of fields for computed values
 * <p>
 * Security is handled upstream by ReviewDataSanitizationService
 */
public class ReportTemplateData {

    private static final Logger logger = LoggerFactory.getLogger(ReportTemplateData.class);

    private final ReviewResult reviewResult;
    private final DateTimeFormatter dateFormatter;

    /**
     * Constructor expects a pre-sanitized ReviewResult
     * All LLM content should already be clean at this point
     */
    public ReportTemplateData(ReviewResult reviewResult, DateTimeFormatter dateFormatter) {
        logger.debug("Creating ReportTemplateData with {} items",
                reviewResult != null && reviewResult.getItems() != null ? reviewResult.getItems().size() : 0);
        this.reviewResult = reviewResult;
        this.dateFormatter = dateFormatter;
    }

    public int getFilesCount() {
        int count = reviewResult.getItems() != null ? reviewResult.getItems().size() : 0;
        logger.trace("Files count: {}", count);
        return count;
    }

    public int getTotalIssues() {
        int totalIssues = 0;
        if (reviewResult.getItems() != null && !reviewResult.getItems().isEmpty()) {
            totalIssues = reviewResult.getItems().stream()
                    .mapToInt(item -> item != null && item.getComments() != null ? item.getComments().size() : 0)
                    .sum();
        }
        logger.debug("Total issues calculated: {}", totalIssues);
        return totalIssues;
    }

    public String getTokenUsage() {
        ReviewCompletionUsage usage = reviewResult.getCompletionUsage();
        if (usage == null) return "";
        return String.format("%d total tokens (%d completion + %d prompt)",
                usage.getTotalTokens(), usage.getCompletionTokens(), usage.getPromptTokens());
    }

    public boolean isHasUsageData() {
        return reviewResult.getCompletionUsage() != null;
    }

    public List<FileTemplateData> getFiles() {
        if (reviewResult.getItems() != null && !reviewResult.getItems().isEmpty()) {
            List<FileTemplateData> files = reviewResult.getItems().stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.getFile() != null)
                    .map(item -> new FileTemplateData(item, dateFormatter))
                    .collect(Collectors.toList());
            logger.debug("Converted {} review items to file template data", files.size());
            return files;
        }
        logger.debug("No review items found, returning empty list");
        return Collections.emptyList();
    }

    public boolean isHasFiles() {
        List<FileTemplateData> files = getFiles();
        return files != null && !files.isEmpty();
    }

    public ResourceUsageData getResourceUsage() {
        ReviewCompletionUsage usage = reviewResult.getCompletionUsage();
        return usage != null ? new ResourceUsageData(usage) : null;
    }

    /**
     * File data for template
     */
    public static class FileTemplateData {
        private static final Logger logger = LoggerFactory.getLogger(FileTemplateData.class);

        private final ReviewResultItem item;
        private final DateTimeFormatter dateFormatter;
        private int index;

        public FileTemplateData(ReviewResultItem item, DateTimeFormatter dateFormatter) {
            logger.trace("Creating FileTemplateData for file: {}",
                    item != null && item.getFile() != null ? item.getFile().getFileName() : "unknown");
            this.item = item;
            this.dateFormatter = dateFormatter;
            this.index = 0;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getFileName() {
            ReviewFile file = item.getFile();
            return file.getFileName() != null ? file.getFileName() : "Unknown";
        }

        public String getFilePath() {
            ReviewFile file = item.getFile();
            return file.getFilePath() != null ? file.getFilePath() : "";
        }

        public String getSize() {
            ReviewFile file = item.getFile();
            return file.getSize() != null ? file.getSize() + " bytes" : "0 bytes";
        }

        public String getCreatedAt() {
            ReviewFile file = item.getFile();
            return formatDateTime(file.getCreatedAt(), dateFormatter);
        }

        public String getModifiedAt() {
            ReviewFile file = item.getFile();
            return formatDateTime(file.getModifiedAt(), dateFormatter);
        }

        public boolean isHasCreatedAt() {
            ReviewFile file = item.getFile();
            return file.getCreatedAt() != null;
        }

        public boolean isHasModifiedAt() {
            ReviewFile file = item.getFile();
            return file.getModifiedAt() != null;
        }

        public List<IssueTemplateData> getIssues() {
            if (item.getComments() != null && !item.getComments().isEmpty()) {
                List<IssueTemplateData> issues = item.getComments().stream()
                        .filter(Objects::nonNull)
                        .map(IssueTemplateData::new)
                        .collect(Collectors.toList());
                logger.trace("File {} has {} issues", getFileName(), issues.size());
                return issues;
            }
            logger.trace("File {} has no issues", getFileName());
            return Collections.emptyList();
        }

        public List<ReasoningStepData> getReasoningSteps() {
            if (item.getThinkSteps() != null && !item.getThinkSteps().isEmpty()) {
                return item.getThinkSteps().stream()
                        .filter(Objects::nonNull)
                        .map(ReasoningStepData::new)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        public boolean isHasIssues() {
            List<IssueTemplateData> issues = getIssues();
            return issues != null && !issues.isEmpty();
        }

        public boolean isHasReasoningSteps() {
            List<ReasoningStepData> steps = getReasoningSteps();
            return steps != null && !steps.isEmpty();
        }

        private String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
            if (dateTime != null) {
                try {
                    return dateTime.format(formatter);
                } catch (Exception e) {
                    logger.warn("Failed to format datetime {}, falling back to toString()", dateTime, e);
                    return dateTime.toString();
                }
            }
            return "";
        }
    }

    /**
     * Issue data - all content is assumed to be pre-sanitized
     */
    public static class IssueTemplateData {
        private static final Logger logger = LoggerFactory.getLogger(IssueTemplateData.class);

        private final ReviewComment comment;

        public IssueTemplateData(ReviewComment comment) {
            logger.trace("Creating IssueTemplateData for comment at line {}",
                    comment != null ? comment.getLine() : "unknown");
            this.comment = comment;
        }

        public String getSeverityIcon() {
            Rule rule = comment.getRule();
            if (rule == null || rule.getSeverity() == null) {
                logger.trace("Rule or severity is null, using default warning icon");
                return "⚠️";
            }
            String icon;
            switch (rule.getSeverity()) {
                case CRITICAL:
                    icon = "🔴";
                    break;
                case WARNING:
                    icon = "🟡";
                    break;
                case INFO:
                    icon = "ℹ️";
                    break;
                default:
                    icon = "⚠️";
                    break;
            }
            logger.trace("Severity {} mapped to icon {}", rule.getSeverity(), icon);
            return icon;
        }

        public String getSeverityText() {
            Rule rule = comment.getRule();
            if (rule == null || rule.getSeverity() == null) return "Unknown Issue";
            String text = rule.getSeverity().name() + " Issue";
            if (rule.getCode() != null) {
                text += " (Rule " + rule.getCode() + ")";
            }
            return text;
        }

        public String getSeverityClass() {
            Rule rule = comment.getRule();
            if (rule == null || rule.getSeverity() == null) return "";
            return " " + rule.getSeverity().name();
        }

        public String getLocation() {
            return String.format("Line %s, Column %s",
                    comment.getLine() != null ? comment.getLine() : "?",
                    comment.getColumn() != null ? comment.getColumn() : "?");
        }

        public String getRuleId() {
            return comment.getRuleId() != null ? comment.getRuleId().toString() : null;
        }

        public String getRuleCode() {
            return comment.getRuleCode();
        }

        public String getRuleDescription() {
            Rule rule = comment.getRule();
            return rule != null ? (rule.getDescription() != null ? rule.getDescription() : "") : null;
        }

        public String getProblem() {
            return comment.getMessage();
        }

        public String getRecommendation() {
            return comment.getSuggestion() != null ? comment.getSuggestion() : "";
        }

        public boolean isHasRuleId() {
            return getRuleId() != null;
        }

        public boolean isHasRuleCode() {
            String ruleCode = getRuleCode();
            return ruleCode != null && !ruleCode.trim().isEmpty();
        }

        public boolean isHasRuleDescription() {
            String description = getRuleDescription();
            return description != null && !description.trim().isEmpty();
        }

        public boolean isHasProblem() {
            String problem = getProblem();
            return problem != null && !problem.trim().isEmpty();
        }
    }

    /**
     * Reasoning step data - assumes pre-sanitized content
     */
    public static class ReasoningStepData {
        private static final Logger logger = LoggerFactory.getLogger(ReasoningStepData.class);

        private final ReviewThinkStep step;

        public ReasoningStepData(ReviewThinkStep step) {
            logger.trace("Creating ReasoningStepData for rule: {}",
                    step != null ? step.getRuleCode() : "unknown");
            this.step = step;
        }

        public String getFileId() {
            return step.getFileId() != null ? step.getFileId().toString() : "Unknown";
        }

        public String getFileName() {
            return step.getFileName() != null ? step.getFileName() : "Unknown";
        }

        public String getRuleId() {
            return step.getRuleId() != null ? step.getRuleId().toString() : "Unknown";
        }

        public String getRuleCode() {
            return step.getRuleCode() != null ? step.getRuleCode() : "Unknown";
        }

        public String getStepText() {
            return step.getThinkText() != null ? step.getThinkText() : "No description available";
        }
    }

    /**
     * Resource usage data - numerical data, always safe
     */
    public static class ResourceUsageData {
        private static final Logger logger = LoggerFactory.getLogger(ResourceUsageData.class);

        private final ReviewCompletionUsage usage;

        public ResourceUsageData(ReviewCompletionUsage usage) {
            logger.debug("Creating ResourceUsageData with {} total tokens",
                    usage != null ? usage.getTotalTokens() : 0);
            this.usage = usage;
        }

        public long getCompletionTokens() {
            return usage.getCompletionTokens();
        }

        public long getPromptTokens() {
            return usage.getPromptTokens();
        }

        public long getTotalTokens() {
            return usage.getTotalTokens();
        }
    }
}