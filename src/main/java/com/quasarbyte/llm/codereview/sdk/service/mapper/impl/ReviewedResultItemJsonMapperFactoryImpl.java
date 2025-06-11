package com.quasarbyte.llm.codereview.sdk.service.mapper.impl;

import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapper;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ReviewedResultItemJsonMapperFactory
 */
public class ReviewedResultItemJsonMapperFactoryImpl implements ReviewedResultItemJsonMapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewedResultItemJsonMapperFactoryImpl.class);

    @Override
    public ReviewedResultItemJsonMapper create() {
        logger.debug("Instantiating ReviewedResultItemJsonMapper");
        return new ReviewedResultItemJsonMapperImpl();
    }
}
