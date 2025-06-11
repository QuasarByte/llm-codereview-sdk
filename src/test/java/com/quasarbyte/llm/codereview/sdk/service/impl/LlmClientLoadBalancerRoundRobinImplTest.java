package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmClientLoadBalancerRoundRobinImplTest {

    private LlmClientLoadBalancerRoundRobinImpl loadBalancer;
    private List<LlmClient> llmClients;
    private LlmClient client1, client2, client3;

    @BeforeEach
    void setUp() {
        loadBalancer = new LlmClientLoadBalancerRoundRobinImpl();
        client1 = mock(LlmClient.class);
        client2 = mock(LlmClient.class);
        client3 = mock(LlmClient.class);

        when(client1.toString()).thenReturn("Client1");
        when(client2.toString()).thenReturn("Client2");
        when(client3.toString()).thenReturn("Client3");

        llmClients = Arrays.asList(client1, client2, client3);
    }

    @Test
    void findLlmClient_withValidInputs_returnsClientsInRoundRobinOrder() {
        AtomicInteger state = new AtomicInteger(0);

        LlmClient result1 = loadBalancer.findLlmClient(llmClients, state);
        LlmClient result2 = loadBalancer.findLlmClient(llmClients, state);
        LlmClient result3 = loadBalancer.findLlmClient(llmClients, state);
        LlmClient result4 = loadBalancer.findLlmClient(llmClients, state);

        assertEquals(client1, result1);
        assertEquals(client2, result2);
        assertEquals(client3, result3);
        assertEquals(client1, result4); // Should wrap around
    }

    @Test
    void findLlmClient_withEmptyList_throwsValidationException() {
        List<LlmClient> emptyList = Collections.emptyList();
        AtomicInteger state = new AtomicInteger(0);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                loadBalancer.findLlmClient(emptyList, state)
        );
        assertEquals("LLM clients list cannot be null or empty", exception.getMessage());
    }
}
