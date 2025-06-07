package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessages;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmReviewPrompt;

public interface LlmMessagesMapper {
    LlmMessages map(LlmReviewPrompt prompt, LlmMessagesMapperConfiguration configuration);
}
