package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunner;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunnerFactory;

public class TransactionRunnerFactoryImpl implements TransactionRunnerFactory {

    private final DBTransactionManager dbTransactionManager;

    public TransactionRunnerFactoryImpl(DBTransactionManager dbTransactionManager) {
        this.dbTransactionManager = dbTransactionManager;
    }

    @Override
    public TransactionRunner create() {
        return new TransactionRunnerImpl(dbTransactionManager);
    }
}
