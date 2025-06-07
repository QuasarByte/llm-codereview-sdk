package com.quasarbyte.llm.codereview.sdk.model.parameter;

public class Rule {
    private String code;
    private String description;
    private RuleSeverityEnum severity;

    public String getCode() {
        return code;
    }

    public Rule setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Rule setDescription(String description) {
        this.description = description;
        return this;
    }

    public RuleSeverityEnum getSeverity() {
        return severity;
    }

    public Rule setSeverity(RuleSeverityEnum severity) {
        this.severity = severity;
        return this;
    }
}
