package com.quasarbyte.llm.codereview.sdk.service.db.core.driver.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.DatabaseDriverLoader;
import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.DatabaseDriverLoaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of DatabaseDriverLoaderFactory that creates DatabaseDriverLoader instances.
 */
public class DatabaseDriverLoaderFactoryImpl implements DatabaseDriverLoaderFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDriverLoaderFactoryImpl.class);

    @Override
    public DatabaseDriverLoader create() {
        logger.debug("Creating DatabaseDriverLoader instance");
        return new DatabaseDriverLoaderImpl();
    }
}
