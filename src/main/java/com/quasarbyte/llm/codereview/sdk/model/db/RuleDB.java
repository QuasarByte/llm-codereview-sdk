package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;

public class RuleDB {
    private Long id;
    private Long reviewId;
    private String code;
    private String description;
    private RuleSeverityEnum severity;

    public Long getId() {
        return id;
    }

    public RuleDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public RuleDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public RuleDB setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RuleDB setDescription(String description) {
        this.description = description;
        return this;
    }

    public RuleSeverityEnum getSeverity() {
        return severity;
    }

    public RuleDB setSeverity(RuleSeverityEnum severity) {
        this.severity = severity;
        return this;
    }
}
