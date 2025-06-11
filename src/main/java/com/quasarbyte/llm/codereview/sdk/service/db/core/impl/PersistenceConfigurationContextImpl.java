package com.quasarbyte.llm.codereview.sdk.service.db.core.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceConfigurationContextImpl implements PersistenceConfigurationContext {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceConfigurationContextImpl.class);

    private final InheritableThreadLocal<PersistenceConfiguration> pcHolder = new InheritableThreadLocal<>();

    @Override
    public PersistenceConfigurationContext setPersistenceConfiguration(PersistenceConfiguration pc) {
        if (pc != null) {
            logger.debug("Setting PersistenceConfiguration in thread context (Thread: {}).", Thread.currentThread().getName());
        } else {
            logger.warn("Attempted to set null PersistenceConfiguration (Thread: {}).", Thread.currentThread().getName());
        }

        pcHolder.set(pc);
        return this;
    }

    @Override
    public PersistenceConfiguration getPersistenceConfiguration() {
        PersistenceConfiguration config = pcHolder.get();

        if (config != null) {
            logger.debug("Retrieved PersistenceConfiguration from thread context (Thread: {}).", Thread.currentThread().getName());
        } else {
            logger.warn("No PersistenceConfiguration found in thread context (Thread: {}).", Thread.currentThread().getName());
        }

        return config;
    }

    @Override
    public void close() {
        logger.debug("Closing PersistenceConfiguration from thread context (Thread: {}).", Thread.currentThread().getName());
        pcHolder.remove();
    }
}
