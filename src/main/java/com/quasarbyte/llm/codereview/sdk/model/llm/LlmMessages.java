package com.quasarbyte.llm.codereview.sdk.model.llm;

import java.util.List;

public class LlmMessages {
    private List<LlmMessage> messages;

    public List<LlmMessage> getMessages() {
        return messages;
    }

    public LlmMessages setMessages(List<LlmMessage> messages) {
        this.messages = messages;
        return this;
    }
}
