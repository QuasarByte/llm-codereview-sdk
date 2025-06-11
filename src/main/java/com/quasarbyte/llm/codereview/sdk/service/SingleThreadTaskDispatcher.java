package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmTokensQuota;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LoadBalancingStrategy;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;

import java.time.Duration;
import java.util.List;

public interface SingleThreadTaskDispatcher {
    List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      Duration timeoutDuration);
    
    List<ReviewedResultItem> dispatch(
            Boolean useReasoning,
            List<List<Rule>> rulesBatches,
            List<List<ResolvedFilePath>> resolvedFilePathBatches,
            LlmChatCompletionConfiguration llmChatCompletionConfiguration,
            LlmMessagesMapperConfiguration messagesMapperConfiguration,
            LlmClient llmClient,
            LlmTokensQuota tokensQuota);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      Duration timeoutDuration,
                                      LlmTokensQuota tokensQuota);

    // New methods for load balancing with multiple LLM clients
    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      Duration timeoutDuration);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      LlmTokensQuota tokensQuota);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      Duration timeoutDuration,
                                      LlmTokensQuota tokensQuota);
}
