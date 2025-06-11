package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Objects;

public class DBTransactionManagerImpl implements DBTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(DBTransactionManagerImpl.class);

    private final DBConnectionContext dbConnectionContext;
    private final ThreadLocal<Integer> transactionLevelHolder = ThreadLocal.withInitial(() -> 0);

    public DBTransactionManagerImpl(DBConnectionContext dbConnectionContext) {
        this.dbConnectionContext = Objects.requireNonNull(dbConnectionContext, "dbConnectionContext must not be null.");
        logger.debug("DBTransactionManagerImpl initialized.");
    }

    @Override
    public void startTransaction() throws Exception {
        validateConnection();
        int currentLevel = transactionLevelHolder.get();

        if (currentLevel == 0) {
            Connection connection = Objects.requireNonNull(dbConnectionContext.getConnection(), "Connection is null.");
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
                logger.debug("Auto-commit disabled for transaction (Thread: {}).", Thread.currentThread().getName());
            } else {
                logger.warn("Auto-commit already disabled before starting transaction (Thread: {}).", Thread.currentThread().getName());
            }
            logger.debug("Transaction started (Thread: {}).", Thread.currentThread().getName());
        } else {
            logger.debug("Nested transaction started (level {}) (Thread: {}).", currentLevel + 1, Thread.currentThread().getName());
        }

        transactionLevelHolder.set(currentLevel + 1);
    }

    @Override
    public void commitTransaction() throws Exception {
        validateConnection();
        int currentLevel = transactionLevelHolder.get();

        if (currentLevel == 1) {
            Connection connection = Objects.requireNonNull(dbConnectionContext.getConnection(), "Connection is null.");
            connection.commit();
            logger.debug("Transaction committed (Thread: {}).", Thread.currentThread().getName());
        } else if (currentLevel == 0) {
            logger.error("Attempt to commit without an active transaction (Thread: {}).", Thread.currentThread().getName());
            throw new Exception("Transaction not started.");
        } else {
            logger.warn("Commit called within nested transaction (level {}). Only outermost commit is effective. (Thread: {}).",
                    currentLevel, Thread.currentThread().getName());
        }

        transactionLevelHolder.set(currentLevel - 1);
    }

    @Override
    public void rollbackTransaction() throws Exception {
        validateConnection();
        int currentLevel = transactionLevelHolder.get();

        if (currentLevel > 0) {
            Connection connection = Objects.requireNonNull(dbConnectionContext.getConnection(), "Connection is null.");
            connection.rollback();
            logger.debug("Transaction rolled back (Thread: {}).", Thread.currentThread().getName());

            if (!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
                logger.debug("Auto-commit re-enabled after rollback (Thread: {}).", Thread.currentThread().getName());
            } else {
                logger.warn("Auto-commit was already enabled before rollback cleanup (Thread: {}).", Thread.currentThread().getName());
            }

            transactionLevelHolder.set(0);
        } else {
            logger.warn("Rollback requested, but no active transaction exists (Thread: {}).", Thread.currentThread().getName());
        }
    }

    private void validateConnection() throws Exception {
        Connection connection = dbConnectionContext.getConnection();

        if (connection == null) {
            logger.error("Connection context is empty (Thread: {}).", Thread.currentThread().getName());
            throw new Exception("Connection context is empty.");
        } else if (connection.isClosed()) {
            logger.error("Connection is closed (Thread: {}).", Thread.currentThread().getName());
            throw new Exception("Connection is closed.");
        } else {
            logger.debug("Connection is valid (Thread: {}).", Thread.currentThread().getName());
        }
    }
}