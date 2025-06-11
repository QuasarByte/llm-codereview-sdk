package com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;

public class DBConnectionManagerFactoryImpl implements DBConnectionManagerFactory {

    private final DBConnectionContext dbConnectionContext;
    private final DataSourceManager dataSourceManager;

    public DBConnectionManagerFactoryImpl(DBConnectionContext dbConnectionContext, DataSourceManager dataSourceManager) {
        this.dbConnectionContext = dbConnectionContext;
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public DBConnectionManager create() {
        return new DBConnectionManagerImpl(dbConnectionContext, dataSourceManager);
    }
}
