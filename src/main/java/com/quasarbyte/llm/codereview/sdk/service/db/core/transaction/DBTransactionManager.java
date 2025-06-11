package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction;

public interface DBTransactionManager {
    void startTransaction() throws Exception;

    void commitTransaction() throws Exception;

    void rollbackTransaction() throws Exception;
}
