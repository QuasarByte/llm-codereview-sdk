package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;

public interface LlmMessMapperRhinoConfigRepository {
    LlmMessagesMapperConfigurationRhino findDefaultConfiguration();
}
