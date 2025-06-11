package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;

import java.util.List;

public interface LlmClientLoadBalancerRandom {
    
    /**
     * Finds a random LLM client from the provided list.
     * 
     * @param llmClients the list of available LLM clients
     * @return a randomly selected LLM client
     */
    LlmClient findLlmClient(List<LlmClient> llmClients);
}
