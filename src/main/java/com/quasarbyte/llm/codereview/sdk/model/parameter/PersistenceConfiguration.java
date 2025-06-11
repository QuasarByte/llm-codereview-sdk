package com.quasarbyte.llm.codereview.sdk.model.parameter;

import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;

public class PersistenceConfiguration {
    private DataSourceConfiguration dataSourceConfiguration;
    private Boolean persistFileContent;

    public DataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }

    public PersistenceConfiguration setDataSourceConfiguration(DataSourceConfiguration dataSourceConfiguration) {
        this.dataSourceConfiguration = dataSourceConfiguration;
        return this;
    }

    public Boolean getPersistFileContent() {
        return persistFileContent;
    }

    public PersistenceConfiguration setPersistFileContent(Boolean persistFileContent) {
        this.persistFileContent = persistFileContent;
        return this;
    }
}
