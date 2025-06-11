package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManagerFactory;

public class DataSourceManagerFactoryImpl implements DataSourceManagerFactory {

    private final DataSourceFactory dataSourceFactory;

    public DataSourceManagerFactoryImpl(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public DataSourceManager create() {
        return new DataSourceManagerImpl(dataSourceFactory);
    }
}
