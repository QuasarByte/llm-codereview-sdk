package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

public class DataSourceManagerImpl implements DataSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceManagerImpl.class);

    private static final InheritableThreadLocal<DataSource> dataSourceHolder = new InheritableThreadLocal<>();
    private final DataSourceFactory dataSourceFactory;

    public DataSourceManagerImpl(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        logger.debug("DataSourceManagerImpl initialized.");
    }

    @Override
    public DataSource getDataSource() throws Exception {
        DataSource exiting = dataSourceHolder.get();

        if (exiting == null) {
            logger.debug("No DataSource found in context, creating new one (Thread: {}).", Thread.currentThread().getName());
            DataSource dataSource = dataSourceFactory.create();
            dataSourceHolder.set(dataSource);
            logger.debug("New DataSource set in thread context (Thread: {}).", Thread.currentThread().getName());
            return dataSource;
        } else {
            logger.debug("Reusing existing DataSource from thread context (Thread: {}).", Thread.currentThread().getName());
            return exiting;
        }
    }

    @Override
    public void closeDataSource() throws Exception {
        DataSource dataSource = dataSourceHolder.get();
        if (dataSource != null) {
            logger.debug("DataSource found in thread context (Thread: {}).", Thread.currentThread().getName());
            if (dataSource instanceof Closeable) {
                logger.debug("Attempting to close DataSource (Thread: {}).", Thread.currentThread().getName());
                try {
                    ((Closeable) dataSource).close();
                    logger.info("DataSource closed successfully (Thread: {}).", Thread.currentThread().getName());
                } catch (IOException e) {
                    String msg = String.format("Failed to close DataSource (Thread: %s)", Thread.currentThread().getName());
                    logger.error(msg, e);
                    throw new PersistenceException(msg, e);
                }
            } else if (dataSource instanceof AutoCloseable) {
                    logger.debug("Attempting to close DataSource (Thread: {}).", Thread.currentThread().getName());
                    try {
                        ((AutoCloseable) dataSource).close();
                        logger.info("DataSource closed successfully (Thread: {}).", Thread.currentThread().getName());
                    } catch (Exception e) {
                        String msg = String.format("Failed to close DataSource (Thread: %s)", Thread.currentThread().getName());
                        logger.error(msg, e);
                        throw new PersistenceException(msg, e);
                    }
            } else {
                logger.warn("DataSource is not Closeable (Thread: {}).", Thread.currentThread().getName());
            }
            logger.debug("Removing DataSource from thread context (Thread: {}).", Thread.currentThread().getName());
            dataSourceHolder.remove();
        } else {
            logger.debug("No DataSource to remove from thread context (Thread: {}).", Thread.currentThread().getName());
        }
    }

    @Override
    public void close() throws Exception {
        logger.debug("Closing DataSourceManager (Thread: {}).", Thread.currentThread().getName());
        closeDataSource();
    }
}
