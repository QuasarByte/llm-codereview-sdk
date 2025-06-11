package com.quasarbyte.llm.codereview.sdk.service.db.core.driver;

import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;

/**
 * Service interface for loading database drivers based on configuration.
 * This service automatically detects and loads the appropriate JDBC driver
 * before database connections are established.
 */
public interface DatabaseDriverLoader {

    /**
     * Loads the appropriate database driver based on the data source configuration.
     * This method should be called before establishing any database connections.
     *
     * @param dataSourceConfiguration the data source configuration containing driver information
     * @throws Exception if the driver cannot be loaded or is not supported
     */
    void loadDriver(DataSourceConfiguration dataSourceConfiguration) throws Exception;

    /**
     * Checks if a driver is already loaded for the specified driver class name.
     *
     * @param driverClassName the driver class name to check
     * @return true if the driver is already loaded, false otherwise
     */
    boolean isDriverLoaded(String driverClassName);
}
