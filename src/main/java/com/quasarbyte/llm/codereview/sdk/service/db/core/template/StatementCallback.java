package com.quasarbyte.llm.codereview.sdk.service.db.core.template;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Callback interface for executing operations on a Statement.
 */
@FunctionalInterface
public interface StatementCallback<T> {
    T doInStatement(Statement stmt) throws SQLException;
}
