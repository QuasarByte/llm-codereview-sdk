package com.quasarbyte.llm.codereview.sdk.model.llm;

public class LlmReviewComment {
    private Long ruleId;
    private String ruleCode;
    private Integer line;
    private Integer column;
    private String message;
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
