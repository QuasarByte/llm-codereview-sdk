package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;

public class ReviewedComment {
    private PromptRule rule;
    private Long ruleId;
    private String ruleCode;
    private Integer line;
    private Integer column;
    private String message;
    private String suggestion;

    public PromptRule getRule() {
        return rule;
    }

    public ReviewedComment setRule(PromptRule rule) {
        this.rule = rule;
        return this;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public ReviewedComment setRuleId(Long ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public ReviewedComment setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
        return this;
    }

    public Integer getLine() {
        return line;
    }

    public ReviewedComment setLine(Integer line) {
        this.line = line;
        return this;
    }

    public Integer getColumn() {
        return column;
    }

    public ReviewedComment setColumn(Integer column) {
        this.column = column;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ReviewedComment setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public ReviewedComment setSuggestion(String suggestion) {
        this.suggestion = suggestion;
        return this;
    }
}
