package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.CannotReadResourceException;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepository;
import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LlmMessMapperRhinoConfigRepositoryImpl implements LlmMessMapperRhinoConfigRepository {

    public static final String MAP_PROMPT_TO_MESSAGES_FUNCTION_NAME = "mapPromptToMessages";
    public static final String SCRIPT_BODY_LOCATION = "classpath:com/quasarbyte/llm/codereview/sdk/rhino/script/rhino-llm-messages-mapper.js";

    private static final Logger logger = LoggerFactory.getLogger(LlmMessMapperRhinoConfigRepositoryImpl.class);

    private final ResourceLoader resourceLoader;

    public LlmMessMapperRhinoConfigRepositoryImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public LlmMessagesMapperConfigurationRhino findDefaultConfiguration() {
        logger.debug("Attempting to load Rhino messages mapper script from resource: {}", SCRIPT_BODY_LOCATION);
        try {
            String scriptBody = resourceLoader.load(SCRIPT_BODY_LOCATION);
            logger.debug("Successfully loaded script body (length={} characters)", scriptBody != null ? scriptBody.length() : 0);

            logger.trace("Script body:\n{}", scriptBody);

            logger.debug("Map Prompt to messages function name: '{}'", MAP_PROMPT_TO_MESSAGES_FUNCTION_NAME);

            return new LlmMessagesMapperConfigurationRhino()
                    .setScriptBody(scriptBody)
                    .setFunctionName(MAP_PROMPT_TO_MESSAGES_FUNCTION_NAME);

        } catch (IOException e) {
            logger.error("Failed to load resource: {}", SCRIPT_BODY_LOCATION, e);
            throw new CannotReadResourceException(
                    String.format("Can not read resource: '%s', error message: '%s'", SCRIPT_BODY_LOCATION, e.getMessage()), e);
        }
    }
}
