package com.quasarbyte.llm.codereview.sdk.service.db.core.connection;

import java.sql.Connection;

public interface DBConnectionContext {
    Connection getConnection();
    void setConnection(Connection connection);
    void clearConnection();
    boolean hasConnection();
}
