package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBTransactionManagerFactoryImpl implements DBTransactionManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(DBTransactionManagerFactoryImpl.class);

    private final DBConnectionContext dbConnectionContext;

    public DBTransactionManagerFactoryImpl(DBConnectionContext dbConnectionContext) {
        this.dbConnectionContext = dbConnectionContext;
        logger.debug("DBTransactionManagerFactoryImpl initialized.");
    }

    @Override
    public DBTransactionManager create() {
        logger.debug("Creating new DBTransactionManager with DBConnectionContext: {}", dbConnectionContext.getClass().getSimpleName());
        return new DBTransactionManagerImpl(dbConnectionContext);
    }
}
