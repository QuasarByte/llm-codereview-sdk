package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientLoadBalancerRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class LlmClientLoadBalancerRandomImpl implements LlmClientLoadBalancerRandom {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientLoadBalancerRandomImpl.class);
    
    private final Random random = new Random();

    @Override
    public LlmClient findLlmClient(List<LlmClient> llmClients) {
        if (llmClients == null || llmClients.isEmpty()) {
            throw new ValidationException("LLM clients list cannot be null or empty");
        }

        int index = random.nextInt(llmClients.size());
        LlmClient selectedClient = llmClients.get(index);
        
        logger.debug("Random load balancer selected client at index {} out of {} clients", 
                     index, llmClients.size());
        
        return selectedClient;
    }
}
