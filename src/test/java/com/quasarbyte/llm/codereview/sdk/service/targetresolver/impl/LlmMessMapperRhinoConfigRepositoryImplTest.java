package com.quasarbyte.llm.codereview.sdk.service.targetresolver.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepository;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.impl.LlmMessMapperRhinoConfigRepositoryFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LlmMessMapperRhinoConfigRepositoryImplTest {

    @Test
    void doTest() {
        LlmMessMapperRhinoConfigRepositoryFactory factory = new LlmMessMapperRhinoConfigRepositoryFactoryImpl();
        LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository = factory.create();
        LlmMessagesMapperConfigurationRhino configurationRhino = llmMessMapperRhinoConfigRepository.findDefaultConfiguration();
        Assertions.assertNotNull(configurationRhino);
        Assertions.assertNotNull(configurationRhino.getScriptBody());
        Assertions.assertNotNull(configurationRhino.getFunctionName());
    }

}
