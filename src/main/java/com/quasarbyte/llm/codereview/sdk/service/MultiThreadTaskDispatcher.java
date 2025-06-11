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
import java.util.concurrent.ExecutorService;

public interface MultiThreadTaskDispatcher {

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      int concurrency,
                                      ExecutorService executorService);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      int concurrency,
                                      Duration timeoutDuration,
                                      ExecutorService executorService);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      int concurrency,
                                      ExecutorService executorService,
                                      LlmTokensQuota tokensQuota);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient,
                                      int concurrency,
                                      Duration timeoutDuration,
                                      ExecutorService executorService,
                                      LlmTokensQuota tokensQuota);

    // New methods for load balancing with multiple LLM clients
    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      int concurrency,
                                      ExecutorService executorService);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      int concurrency,
                                      Duration timeoutDuration,
                                      ExecutorService executorService);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      int concurrency,
                                      ExecutorService executorService,
                                      LlmTokensQuota tokensQuota);

    List<ReviewedResultItem> dispatch(Boolean useReasoning,
                                      List<List<Rule>> rulesBatches,
                                      List<List<ResolvedFilePath>> resolvedFilePathBatches,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      List<LlmClient> llmClients,
                                      LoadBalancingStrategy loadBalancingStrategy,
                                      int concurrency,
                                      Duration timeoutDuration,
                                      ExecutorService executorService,
                                      LlmTokensQuota tokensQuota);
}
