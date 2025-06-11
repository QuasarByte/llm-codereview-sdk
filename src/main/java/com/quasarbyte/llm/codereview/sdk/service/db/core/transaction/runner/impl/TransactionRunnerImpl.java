package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.ThrowingRunnable;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.ThrowingSupplier;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class TransactionRunnerImpl implements TransactionRunner {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRunnerImpl.class);

    private final DBTransactionManager dbTransactionManager;

    public TransactionRunnerImpl(DBTransactionManager dbTransactionManager) {
        this.dbTransactionManager = dbTransactionManager;
    }

    // 1. Runnable: void, no checked exceptions
    @Override
    public void runRunnable(Runnable action) {
        try {
            dbTransactionManager.startTransaction();
            action.run();
            dbTransactionManager.commitTransaction();
        } catch (Exception e) {
            handleRollback(e);
        }
    }

    // 2. Supplier<T>: returns T, no checked exceptions
    @Override
    public <T> T runSupplier(Supplier<T> supplier) {
        try {
            dbTransactionManager.startTransaction();
            T result = supplier.get();
            dbTransactionManager.commitTransaction();
            return result;
        } catch (Exception e) {
            handleRollback(e);
            return null; // unreachable, just for compiler
        }
    }

    // 3. ThrowingRunnable: void, with checked exceptions
    @Override
    public void runThrowingRunnable(ThrowingRunnable action) throws Exception {
        try {
            dbTransactionManager.startTransaction();
            action.run();
            dbTransactionManager.commitTransaction();
        } catch (Exception e) {
            safeRollback(e);
            throw e;
        }
    }

    // 4. ThrowingSupplier<T>: returns T, with checked exceptions
    @Override
    public <T> T runThrowingSupplier(ThrowingSupplier<T> supplier) throws Exception {
        try {
            dbTransactionManager.startTransaction();
            T result = supplier.get();
            dbTransactionManager.commitTransaction();
            return result;
        } catch (Exception e) {
            safeRollback(e);
            throw e;
        }
    }

    // Handles rollback and rethrows RuntimeException
    private void handleRollback(Exception e) {
        try {
            dbTransactionManager.rollbackTransaction();
        } catch (Exception rollbackEx) {
            logger.error("Rollback error: {}", rollbackEx.getMessage(), rollbackEx);
        }
        logger.error("Transaction execution error: {}", e.getMessage(), e);
        throw new PersistenceRuntimeException(e);
    }

    // Handles rollback for checked exceptions, does not rethrow here
    private void safeRollback(Exception e) {
        try {
            dbTransactionManager.rollbackTransaction();
        } catch (Exception rollbackEx) {
            logger.error("Rollback error: {}", rollbackEx.getMessage(), rollbackEx);
        }
        logger.error("Transaction execution error: {}", e.getMessage(), e);
    }
}
