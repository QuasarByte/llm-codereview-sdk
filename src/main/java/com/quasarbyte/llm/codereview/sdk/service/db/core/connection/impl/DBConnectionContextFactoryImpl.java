package com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnectionContextFactoryImpl implements DBConnectionContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionContextFactoryImpl.class);

    @Override
    public DBConnectionContext create() {
        logger.debug("Creating new DBConnectionContext instance.");
        try {
            DBConnectionContext context = new DBConnectionContextImpl();
            logger.info("DBConnectionContext created successfully.");
            return context;

        } catch (Exception e) {
            logger.error("Failed to create DBConnectionContext, error: '{}'", e.getMessage(), e);
            throw e;
        }
    }
}
