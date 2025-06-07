package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;

public interface LlmClientFactory {
    LlmClient create(LlmClientConfiguration config);
}
