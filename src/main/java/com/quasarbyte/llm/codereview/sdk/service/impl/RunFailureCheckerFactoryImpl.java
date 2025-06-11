package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.RunFailureChecker;
import com.quasarbyte.llm.codereview.sdk.service.RunFailureCheckerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunFailureCheckerFactoryImpl implements RunFailureCheckerFactory {

    private static final Logger logger = LoggerFactory.getLogger(RunFailureCheckerFactoryImpl.class);

    @Override
    public RunFailureChecker create() {
        logger.debug("Instantiating RunFailureChecker");
        return new RunFailureCheckerImpl();
    }
}
