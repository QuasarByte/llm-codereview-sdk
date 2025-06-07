package com.quasarbyte.llm.codereview.sdk.service;

import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;

public interface ChatCompletionCreateParamsFactory {
    ChatCompletionCreateParams.Builder create(LlmChatCompletionConfiguration configuration);
}
