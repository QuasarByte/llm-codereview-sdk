package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonValue;
import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRhinoException;

public enum LlmMessageRoleEnum {
    USER("user"),
    SYSTEM("system");

    private final String role;
    LlmMessageRoleEnum(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    public static LlmMessageRoleEnum fromRole(String role) {
        for (LlmMessageRoleEnum value : LlmMessageRoleEnum.values()) {
            if (value.getRole().equalsIgnoreCase(role)) {
                return value;
            }
        }
        throw new LLMCodeReviewRhinoException("Unknown role: " + role);
    }
}
