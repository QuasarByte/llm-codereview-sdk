package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepository;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LlmMessMapperRhinoConfigRepositoryFactoryImpl implements LlmMessMapperRhinoConfigRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(LlmMessMapperRhinoConfigRepositoryFactoryImpl.class);

    @Override
    public LlmMessMapperRhinoConfigRepository create() {
        logger.info("Creating LlmMessMapperRhinoConfigRepository...");
        logger.debug("Instantiating ResourceLoaderImpl.");
        ResourceLoader resourceLoader = new ResourceLoaderImpl();

        logger.debug("Instantiating LlmMessMapperRhinoConfigRepositoryImpl.");
        LlmMessMapperRhinoConfigRepository repo = new LlmMessMapperRhinoConfigRepositoryImpl(resourceLoader);

        logger.info("LlmMessMapperRhinoConfigRepository created successfully.");
        return repo;
    }
}
