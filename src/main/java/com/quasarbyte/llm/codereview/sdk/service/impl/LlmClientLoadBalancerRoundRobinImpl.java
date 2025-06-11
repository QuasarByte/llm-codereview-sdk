package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRoundRobin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LlmClientLoadBalancerRoundRobinImpl implements LlmClientLoadBalancerRoundRobin {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientLoadBalancerRoundRobinImpl.class);

    @Override
    public LlmClient findLlmClient(List<LlmClient> llmClients, AtomicInteger state) {
        if (llmClients == null || llmClients.isEmpty()) {
            throw new ValidationException("LLM clients list cannot be null or empty");
        }
        if (state == null) {
            throw new ValidationException("State cannot be null for round-robin load balancing");
        }
        int index = Math.floorMod(state.getAndIncrement(), llmClients.size());

        LlmClient selectedClient = llmClients.get(index);

        logger.debug("Round-robin load balancer selected client at index {} out of {} clients",
                index, llmClients.size());

        return selectedClient;
    }
}
