package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource;

import javax.sql.DataSource;

public interface DataSourceFactory {
    DataSource create() throws Exception;
}
