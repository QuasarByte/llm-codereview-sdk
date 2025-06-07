package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;

import java.time.Duration;
import java.util.List;

public interface SingleThreadTaskDispatcher {
    List<ReviewedResultItem> dispatch(
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient);

    List<ReviewedResultItem> dispatch(List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      Duration timeoutDuration);
}
