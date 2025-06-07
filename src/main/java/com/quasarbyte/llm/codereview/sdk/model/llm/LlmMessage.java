package com.quasarbyte.llm.codereview.sdk.model.llm;

public class LlmMessage {
    private LlmMessageRoleEnum role;
    private String content;

    public LlmMessageRoleEnum getRole() {
        return role;
    }

    public LlmMessage setRole(LlmMessageRoleEnum role) {
        this.role = role;
        return this;
    }

    public String getContent() {
        return content;
    }

    public LlmMessage setContent(String content) {
        this.content = content;
        return this;
    }
}
