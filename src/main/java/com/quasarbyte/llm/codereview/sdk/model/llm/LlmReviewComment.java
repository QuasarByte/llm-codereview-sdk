package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("Single review comment that identifies an issue or suggestion in a specific location within a source file.")
public class LlmReviewComment {
    @JsonPropertyDescription("Unique identifier of the rule that triggered this comment. Should match the ruleId used in the input prompt.")
    private Long ruleId;
    @JsonPropertyDescription("Short string code of the rule that was violated or triggered. Should match the ruleCode used in the input prompt.")
    private String ruleCode;
    @JsonPropertyDescription("1-based line number in the source file where the issue starts. Optional - may be null if not applicable.")
    private Integer line;
    @JsonPropertyDescription("1-based column number in the line where the issue starts. Optional - may be null if not applicable.")
    private Integer column;
    @JsonPropertyDescription("Textual explanation of the issue found.")
    private String message;
    @JsonPropertyDescription("Suggested way to fix the issue.")
    private String suggestion;

    public Long getRuleId() {
        return ruleId;
    }

    public LlmReviewComment setRuleId(Long ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public LlmReviewComment setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
        return this;
    }

    public Integer getLine() {
        return line;
    }

    public LlmReviewComment setLine(Integer line) {
        this.line = line;
        return this;
    }

    public Integer getColumn() {
        return column;
    }

    public LlmReviewComment setColumn(Integer column) {
        this.column = column;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LlmReviewComment setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public LlmReviewComment setSuggestion(String suggestion) {
        this.suggestion = suggestion;
        return this;
    }
}