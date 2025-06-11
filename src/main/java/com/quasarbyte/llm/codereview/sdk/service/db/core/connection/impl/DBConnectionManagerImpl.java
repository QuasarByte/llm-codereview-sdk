package com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DBConnectionManagerImpl implements DBConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManagerImpl.class);

    private final DBConnectionContext dbConnectionContext;
    private final DataSourceManager dataSourceManager;
    private final ThreadLocal<Integer> connectionCounter = ThreadLocal.withInitial(() -> 0);

    public DBConnectionManagerImpl(DBConnectionContext dbConnectionContext, DataSourceManager dataSourceManager) {
        this.dbConnectionContext = dbConnectionContext;
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public Connection openConnection() throws Exception {
        final Connection connection;

        if (connectionCounter.get() > 0) {
            logger.debug("Reusing existing DB connection (nesting level {}).", connectionCounter.get());
            connection = Objects.requireNonNull(dbConnectionContext.getConnection(), "Connection cannot be null.");
        } else {
            logger.debug("Opening new DB connection.");
            DataSource dataSource = Objects.requireNonNull(dataSourceManager.getDataSource(), "DataSource cannot be null.");
            Connection delegate = Objects.requireNonNull(dataSource.getConnection(), "Connection cannot be null.");

            connection = (Connection) Proxy.newProxyInstance(
                    delegate.getClass().getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        if ("close".equals(method.getName()) && method.getParameterCount() == 0) {

                            int count = connectionCounter.get();
                            if (count == 1) {
                                logger.debug("Closing DB connection at nesting level 1.");
                                dbConnectionContext.clearConnection();
                                delegate.close();
                                connectionCounter.set(0);
                                logger.debug("DB connection closed and context cleared (Thread: {}).", Thread.currentThread().getName());
                            } else if (count == 0) {
                                logger.error("Attempted to close DB connection, but no active connection exists.");
                                if (dbConnectionContext.getConnection() != null) {
                                    throw new PersistenceException("Connection context is not empty.");
                                }
                                throw new PersistenceException("Connection already closed.");
                            } else {
                                connectionCounter.set(count - 1);
                                logger.debug("Decremented connection nesting level to {}.", connectionCounter.get());
                            }

                            return null;
                        }

                        try {
                            return method.invoke(delegate, args);
                        } catch (InvocationTargetException e) {
                            // Propagate the original (underlying) exception
                            throw e.getCause();
                        }
                    }
            );

            dbConnectionContext.setConnection(connection);
            logger.debug("DB connection established and set in context (Thread: {}).", Thread.currentThread().getName());
        }

        connectionCounter.set(connectionCounter.get() + 1);
        logger.debug("Connection nesting level incremented to {}.", connectionCounter.get());

        return connection;
    }
}
