package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LlmClientLoadBalancerRandomImplTest {

    private LlmClientLoadBalancerRandomImpl loadBalancer;
    private List<LlmClient> llmClients;
    private LlmClient client1, client2, client3;

    @BeforeEach
    void setUp() {
        loadBalancer = new LlmClientLoadBalancerRandomImpl();
        client1 = mock(LlmClient.class);
        client2 = mock(LlmClient.class);
        client3 = mock(LlmClient.class);
        llmClients = Arrays.asList(client1, client2, client3);
    }

    @Test
    void findLlmClient_withValidInputs_returnsValidClient() {
        LlmClient result = loadBalancer.findLlmClient(llmClients);

        assertNotNull(result);
        assertTrue(llmClients.contains(result));
    }

    @Test
    void findLlmClient_withSingleClient_alwaysReturnsSameClient() {
        List<LlmClient> singleClientList = Arrays.asList(client1);

        LlmClient result1 = loadBalancer.findLlmClient(singleClientList);
        LlmClient result2 = loadBalancer.findLlmClient(singleClientList);
        LlmClient result3 = loadBalancer.findLlmClient(singleClientList);

        assertEquals(client1, result1);
        assertEquals(client1, result2);
        assertEquals(client1, result3);
    }

    @Test
    void findLlmClient_withNullList_throwsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
                loadBalancer.findLlmClient(null)
        );

        assertEquals("LLM clients list cannot be null or empty", exception.getMessage());
    }

    @Test
    void findLlmClient_withEmptyList_throwsValidationException() {
        List<LlmClient> emptyList = Collections.emptyList();

        ValidationException exception = assertThrows(ValidationException.class, () ->
                loadBalancer.findLlmClient(emptyList)
        );

        assertEquals("LLM clients list cannot be null or empty", exception.getMessage());
    }

    @Test
    void findLlmClient_multipleCallsDistributeRandomly() {
        int numCalls = 1000;
        List<LlmClient> results = new ArrayList<>();

        for (int i = 0; i < numCalls; i++) {
            results.add(loadBalancer.findLlmClient(llmClients));
        }

        // Verify all results are valid
        for (LlmClient result : results) {
            assertTrue(llmClients.contains(result));
        }

        // Verify that each client was used (with high probability for 1000 calls)
        for (LlmClient client : llmClients) {
            long usage = results.stream().filter(c -> c == client).count();
            assertTrue(usage > 0, "Each client should be used at least once in 1000 random calls");
            
            // With 3 clients and 1000 calls, each should get roughly 333 calls
            // Allow for reasonable variation in random selection
            assertTrue(usage >= 200 && usage <= 500, 
                    "Random distribution should be roughly balanced: actual usage " + usage);
        }
    }

    @Test
    void findLlmClient_concurrentAccess_isThreadSafe() throws InterruptedException {
        List<LlmClient> results = Collections.synchronizedList(new ArrayList<>());
        int numThreads = 10;
        int callsPerThread = 100;

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < callsPerThread; j++) {
                    LlmClient client = loadBalancer.findLlmClient(llmClients);
                    results.add(client);
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify results
        assertEquals(numThreads * callsPerThread, results.size());
        for (LlmClient result : results) {
            assertTrue(llmClients.contains(result));
        }

        // Verify that each client was used
        for (LlmClient client : llmClients) {
            long actualUsage = results.stream().filter(c -> c == client).count();
            assertTrue(actualUsage > 0, "Each client should be used at least once");
        }
    }
}
