package com.quasarbyte.llm.codereview.sdk.model.review;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;

public class ReviewComment {
    private Rule rule;
    private Integer line;
    private Integer column;
    private String message;
    private String suggestion;

    public Rule getRule() {
        return rule;
    }

    public ReviewComment setRule(Rule rule) {
        this.rule = rule;
        return this;
    }

    public Integer getLine() {
        return line;
    }

    public ReviewComment setLine(Integer line) {
        this.line = line;
        return this;
    }

    public Integer getColumn() {
        return column;
    }

    public ReviewComment setColumn(Integer column) {
        this.column = column;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ReviewComment setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public ReviewComment setSuggestion(String suggestion) {
        this.suggestion = suggestion;
        return this;
    }
}
