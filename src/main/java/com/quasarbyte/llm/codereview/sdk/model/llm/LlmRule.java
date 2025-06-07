package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;

public class LlmRule {
    private RuleKey ruleKey;
    private String description;
    private RuleSeverityEnum severity;

    public RuleKey getRuleKey() {
        return ruleKey;
    }

    public LlmRule setRuleKey(RuleKey ruleKey) {
        this.ruleKey = ruleKey;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LlmRule setDescription(String description) {
        this.description = description;
        return this;
    }

    public RuleSeverityEnum getSeverity() {
        return severity;
    }

    public LlmRule setSeverity(RuleSeverityEnum severity) {
        this.severity = severity;
        return this;
    }
}
