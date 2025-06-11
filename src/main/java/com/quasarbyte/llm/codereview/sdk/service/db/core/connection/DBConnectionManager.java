package com.quasarbyte.llm.codereview.sdk.service.db.core.connection;

import java.sql.Connection;

public interface DBConnectionManager {
    Connection openConnection() throws Exception;
}
