package com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class DBConnectionContextImpl implements DBConnectionContext {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionContextImpl.class);

    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    @Override
    public Connection getConnection() {
        Connection connection = connectionHolder.get();

        if (connection == null) {
            logger.error("No DB connection found in thread context (Thread: {}). This is a lifecycle/configuration error.",
                    Thread.currentThread().getName());
            throw new IllegalStateException("DB connection not set in current thread context.");
        }

        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        connectionHolder.set(connection);
        logger.debug("DB connection set in thread context (Thread: {}).", Thread.currentThread().getName());
    }

    @Override
    public void clearConnection() {
        connectionHolder.remove();
        logger.debug("Cleared DB connection from thread context (Thread: {}).", Thread.currentThread().getName());
    }

    @Override
    public boolean hasConnection() {
        return connectionHolder.get() != null;
    }
}
