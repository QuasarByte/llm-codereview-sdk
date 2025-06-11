package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewRunContextFactoryImpl implements ReviewRunContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewRunContextFactoryImpl.class);

    @Override
    public ReviewRunContext create() {
        logger.info("Instantiating ReviewRunContext");
        return new ReviewRunContextImpl();
    }

}
