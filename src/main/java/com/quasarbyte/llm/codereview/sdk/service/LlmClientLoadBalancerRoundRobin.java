package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface LlmClientLoadBalancerRoundRobin {
    
    /**
     * Finds the next LLM client using round-robin strategy.
     * 
     * @param llmClients the list of available LLM clients
     * @param state the atomic integer state for tracking current position
     * @return the next LLM client to use
     */
    LlmClient findLlmClient(List<LlmClient> llmClients, AtomicInteger state);
}
