package com.quasarbyte.llm.codereview.sdk.service.db.core.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceConfigurationContextFactoryImpl implements PersistenceConfigurationContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceConfigurationContextFactoryImpl.class);

    public PersistenceConfigurationContextFactoryImpl() {
        logger.debug("PersistenceConfigurationContextFactoryImpl initialized.");
    }

    @Override
    public PersistenceConfigurationContext create() {
        logger.debug("Creating new PersistenceConfigurationContext instance.");
        return new PersistenceConfigurationContextImpl();
    }
}
