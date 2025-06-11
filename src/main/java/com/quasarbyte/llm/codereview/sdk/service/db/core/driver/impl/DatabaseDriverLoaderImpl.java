package com.quasarbyte.llm.codereview.sdk.service.db.core.driver.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceException;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.db.SupportedDatabaseDriver;
import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.DatabaseDriverLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of DatabaseDriverLoader that handles automatic loading of JDBC drivers
 * based on data source configuration. This implementation ensures drivers are loaded
 * only once and provides thread-safe operation.
 */
public class DatabaseDriverLoaderImpl implements DatabaseDriverLoader {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDriverLoaderImpl.class);

    /**
     * Thread-safe set to track which drivers have already been loaded
     * to avoid redundant Class.forName() calls
     */
    private static final Set<String> loadedDrivers = ConcurrentHashMap.newKeySet();

    @Override
    public void loadDriver(DataSourceConfiguration dataSourceConfiguration) throws Exception {
        if (dataSourceConfiguration == null) {
            logger.warn("DataSourceConfiguration is null, skipping driver loading");
            return;
        }

        String driverClassName = dataSourceConfiguration.getDriverClassName();
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            logger.debug("No driver class name specified in configuration, skipping driver loading");
            return;
        }

        driverClassName = driverClassName.trim();

        // Check if driver is already loaded
        if (isDriverLoaded(driverClassName)) {
            logger.debug("Driver '{}' is already loaded, skipping", driverClassName);
            return;
        }

        // Check if this is a supported driver
        SupportedDatabaseDriver supportedDriver = SupportedDatabaseDriver.findByDriverClassName(driverClassName);
        if (supportedDriver == null) {
            logger.warn("Driver class '{}' is not in the list of officially supported drivers, but attempting to load anyway", driverClassName);
        } else {
            logger.debug("Loading supported {} database driver: '{}'", supportedDriver.getDisplayName(), driverClassName);
        }

        try {
            // Load the driver class
            Class.forName(driverClassName);

            // Mark as loaded
            loadedDrivers.add(driverClassName);

            if (supportedDriver != null) {
                logger.info("Successfully loaded {} database driver: '{}'", supportedDriver.getDisplayName(), driverClassName);
            } else {
                logger.info("Successfully loaded database driver: '{}'", driverClassName);
            }

        } catch (ClassNotFoundException e) {
            String errorMessage = String.format("Failed to load database driver '%s'. Please ensure the appropriate JDBC driver dependency is included in your classpath.", driverClassName);
            logger.error(errorMessage, e);
            throw new PersistenceException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format("Unexpected error while loading database driver '%s'", driverClassName);
            logger.error(errorMessage, e);
            throw new PersistenceException(errorMessage, e);
        }
    }

    @Override
    public boolean isDriverLoaded(String driverClassName) {
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            return false;
        }

        return loadedDrivers.contains(driverClassName.trim());
    }
}
