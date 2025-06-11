package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Factory implementation for creating JDBCTemplate instances.
 */
public class JDBCTemplateFactoryImpl implements JDBCTemplateFactory {

    private static final Logger logger = LoggerFactory.getLogger(JDBCTemplateFactoryImpl.class);

    private final DBConnectionManager dbConnectionManager;

    public JDBCTemplateFactoryImpl(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = Objects.requireNonNull(dbConnectionManager, "dbConnectionManager must not be null");
    }

    @Override
    public JDBCTemplate create() {
        logger.debug("Creating JDBCTemplate instance");
        return new JDBCTemplateImpl(dbConnectionManager);
    }
}
