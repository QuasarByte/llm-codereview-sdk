package com.quasarbyte.llm.codereview.sdk.service.mapper.impl;

import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ReviewPromptJsonMapperFactory
 */
public class ReviewPromptJsonMapperFactoryImpl implements ReviewPromptJsonMapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPromptJsonMapperFactoryImpl.class);

    @Override
    public ReviewPromptJsonMapper create() {
        logger.debug("Instantiating ReviewPromptJsonMapper");
        return new ReviewPromptJsonMapperImpl();
    }
}
