package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;

public interface LlmReviewProcessor {
    ReviewedResultItem process(ReviewPrompt prompt,
                               LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                               LlmMessagesMapperConfiguration messagesMapperConfiguration,
                               LlmClient llmClient);
}
