package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource;

import javax.sql.DataSource;

public interface DataSourceManager extends AutoCloseable {
    DataSource getDataSource() throws Exception;

    void closeDataSource() throws Exception;
}
