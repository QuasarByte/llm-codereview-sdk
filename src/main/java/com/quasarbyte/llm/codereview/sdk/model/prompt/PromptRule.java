package com.quasarbyte.llm.codereview.sdk.model.prompt;

import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;

public class PromptRule {
    private RuleKey ruleKey;
    private String description;
    private RuleSeverityEnum severity;

    public RuleKey getRuleKey() {
        return ruleKey;
    }

    public PromptRule setRuleKey(RuleKey ruleKey) {
        this.ruleKey = ruleKey;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PromptRule setDescription(String description) {
        this.description = description;
        return this;
    }

    public RuleSeverityEnum getSeverity() {
        return severity;
    }

    public PromptRule setSeverity(RuleSeverityEnum severity) {
        this.severity = severity;
        return this;
    }
}
