package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LoadBalancingStrategy;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRandom;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRoundRobin;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiThreadTaskDispatcherImplLoadBalancingTest {

    @Mock private LlmReviewProcessor llmReviewProcessor;
    @Mock private ReviewPromptCreator reviewPromptCreator;
    @Mock private LlmClientLoadBalancerRoundRobin roundRobinLoadBalancer;
    @Mock private LlmClientLoadBalancerRandom randomLoadBalancer;
    @Mock private LlmChatCompletionConfiguration llmChatCompletionConfiguration;
    @Mock private LlmMessagesMapperConfiguration messagesMapperConfiguration;
    @Mock private LlmClient llmClient1;
    @Mock private LlmClient llmClient2;
    @Mock private LlmClient llmClient3;
    @Mock private ReviewPrompt reviewPrompt;
    @Mock private ReviewedResultItem reviewedResultItem;
    @Mock private ResolvedFilePath resolvedFilePath;
    @Mock private Rule rule;

    private MultiThreadTaskDispatcherImpl dispatcher;
    private List<LlmClient> llmClients;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        dispatcher = new MultiThreadTaskDispatcherImpl(
                llmReviewProcessor, reviewPromptCreator, roundRobinLoadBalancer, randomLoadBalancer
        );

        llmClients = Arrays.asList(llmClient1, llmClient2, llmClient3);
        executorService = Executors.newFixedThreadPool(2);

        // Setup mocks - using lenient to avoid unnecessary stubbing errors
        lenient().when(reviewPromptCreator.create(any(), anyBoolean())).thenReturn(Arrays.asList(reviewPrompt));
        lenient().when(reviewPrompt.getId()).thenReturn(1L);
        lenient().when(llmReviewProcessor.process(any(), any(), any(), any())).thenReturn(reviewedResultItem);
    }

    @Test
    void dispatch_withRoundRobinStrategy_usesRoundRobinLoadBalancer() {
        when(roundRobinLoadBalancer.findLlmClient(eq(llmClients), any(AtomicInteger.class))).thenReturn(llmClient1);

        List<ReviewedResultItem> result = dispatcher.dispatch(
                true,
                Arrays.asList(Arrays.asList(rule)),
                Arrays.asList(Arrays.asList(resolvedFilePath)),
                llmChatCompletionConfiguration,
                messagesMapperConfiguration,
                llmClients,
                LoadBalancingStrategy.ROUND_ROBIN,
                1,
                executorService
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roundRobinLoadBalancer).findLlmClient(eq(llmClients), any(AtomicInteger.class));
        verify(randomLoadBalancer, never()).findLlmClient(any());
        verify(llmReviewProcessor).process(eq(reviewPrompt), eq(llmChatCompletionConfiguration), 
                eq(messagesMapperConfiguration), eq(llmClient1));
    }

    @Test
    void dispatch_withRandomStrategy_usesRandomLoadBalancer() {
        when(randomLoadBalancer.findLlmClient(llmClients)).thenReturn(llmClient2);

        List<ReviewedResultItem> result = dispatcher.dispatch(
                true,
                Arrays.asList(Arrays.asList(rule)),
                Arrays.asList(Arrays.asList(resolvedFilePath)),
                llmChatCompletionConfiguration,
                messagesMapperConfiguration,
                llmClients,
                LoadBalancingStrategy.RANDOM,
                1,
                executorService
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(randomLoadBalancer).findLlmClient(llmClients);
        verify(roundRobinLoadBalancer, never()).findLlmClient(any(), any());
        verify(llmReviewProcessor).process(eq(reviewPrompt), eq(llmChatCompletionConfiguration), 
                eq(messagesMapperConfiguration), eq(llmClient2));
    }

    @Test
    void dispatch_withNullStrategy_defaultsToRoundRobin() {
        when(roundRobinLoadBalancer.findLlmClient(eq(llmClients), any(AtomicInteger.class))).thenReturn(llmClient3);

        List<ReviewedResultItem> result = dispatcher.dispatch(
                true,
                Arrays.asList(Arrays.asList(rule)),
                Arrays.asList(Arrays.asList(resolvedFilePath)),
                llmChatCompletionConfiguration,
                messagesMapperConfiguration,
                llmClients,
                null, // null strategy should default to ROUND_ROBIN
                1,
                executorService
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roundRobinLoadBalancer).findLlmClient(eq(llmClients), any(AtomicInteger.class));
        verify(randomLoadBalancer, never()).findLlmClient(any());
    }

    @Test
    void dispatch_withMultiplePrompts_callsLoadBalancerMultipleTimes() {
        ReviewPrompt prompt1 = mock(ReviewPrompt.class);
        ReviewPrompt prompt2 = mock(ReviewPrompt.class);
        when(prompt1.getId()).thenReturn(1L);
        when(prompt2.getId()).thenReturn(2L);
        when(reviewPromptCreator.create(any(), anyBoolean())).thenReturn(Arrays.asList(prompt1, prompt2));
        
        when(roundRobinLoadBalancer.findLlmClient(eq(llmClients), any(AtomicInteger.class)))
                .thenReturn(llmClient1, llmClient2);

        List<ReviewedResultItem> result = dispatcher.dispatch(
                true,
                Arrays.asList(Arrays.asList(rule)),
                Arrays.asList(Arrays.asList(resolvedFilePath)),
                llmChatCompletionConfiguration,
                messagesMapperConfiguration,
                llmClients,
                LoadBalancingStrategy.ROUND_ROBIN,
                2, // concurrency = 2, so both prompts run in parallel
                executorService
        );

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roundRobinLoadBalancer, times(2)).findLlmClient(eq(llmClients), any(AtomicInteger.class));
    }

    @Test
    void dispatch_withEmptyFilePathBatches_returnsEmptyList() {
        List<ReviewedResultItem> result = dispatcher.dispatch(
                true,
                Arrays.asList(Arrays.asList(rule)),
                Arrays.asList(), // empty file path batches
                llmChatCompletionConfiguration,
                messagesMapperConfiguration,
                llmClients,
                LoadBalancingStrategy.ROUND_ROBIN,
                1,
                executorService
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roundRobinLoadBalancer, never()).findLlmClient(any(), any());
        verify(randomLoadBalancer, never()).findLlmClient(any());
    }

    @Test
    void dispatch_requiresNonNullParameters() {
        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(null, Arrays.asList(), Arrays.asList(), llmChatCompletionConfiguration,
                        messagesMapperConfiguration, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, null, Arrays.asList(), llmChatCompletionConfiguration,
                        messagesMapperConfiguration, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, Arrays.asList(), null, llmChatCompletionConfiguration,
                        messagesMapperConfiguration, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, Arrays.asList(), Arrays.asList(), null,
                        messagesMapperConfiguration, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, Arrays.asList(), Arrays.asList(), llmChatCompletionConfiguration,
                        null, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, Arrays.asList(), Arrays.asList(), llmChatCompletionConfiguration,
                        messagesMapperConfiguration, null, LoadBalancingStrategy.ROUND_ROBIN, 1, executorService)
        );

        assertThrows(NullPointerException.class, () ->
                dispatcher.dispatch(true, Arrays.asList(), Arrays.asList(), llmChatCompletionConfiguration,
                        messagesMapperConfiguration, llmClients, LoadBalancingStrategy.ROUND_ROBIN, 1, null)
        );
    }
}
