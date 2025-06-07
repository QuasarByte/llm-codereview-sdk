package com.quasarbyte.llm.codereview.sdk.model.llm;

import java.util.Objects;

public class LlmCommentKey {
    private final Long ruleId;
    private final String ruleCode;
    private final Integer line;
    private final Integer column;
    private final String message;
    private final String suggestion;

    public LlmCommentKey(LlmReviewComment c) {
        this.ruleId = c.getRuleId();
        this.ruleCode = c.getRuleCode();
        this.line = c.getLine();
        this.column = c.getColumn();
        this.message = c.getMessage();
        this.suggestion = c.getSuggestion();
    }

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LlmCommentKey that = (LlmCommentKey) o;
        return Objects.equals(ruleId, that.ruleId) && Objects.equals(ruleCode, that.ruleCode) && Objects.equals(line, that.line) && Objects.equals(column, that.column) && Objects.equals(message, that.message) && Objects.equals(suggestion, that.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, ruleCode, line, column, message, suggestion);
    }
}
