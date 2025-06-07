package com.quasarbyte.llm.codereview.sdk.model.parameter;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRhinoException;

public enum RuleSeverityEnum {
    INFO,
    WARNING,
    CRITICAL;

    public static RuleSeverityEnum fromName(String name) {
        for (RuleSeverityEnum value : RuleSeverityEnum.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new LLMCodeReviewRhinoException("Unknown rule severity: " + name);
    }

}
