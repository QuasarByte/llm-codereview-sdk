package com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl;

import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.DatabaseDriverLoader;
import com.quasarbyte.llm.codereview.sdk.service.db.core.driver.impl.DatabaseDriverLoaderFactoryImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Objects;

public class DataSourceFactoryImpl implements DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactoryImpl.class);

    private final PersistenceConfigurationContext persistenceConfigurationContext;
    private final DatabaseDriverLoader databaseDriverLoader;

    public DataSourceFactoryImpl(PersistenceConfigurationContext persistenceConfigurationContext) {
        logger.debug("Initializing DataSourceFactoryImpl with persistenceConfigurationContext");
        this.persistenceConfigurationContext = persistenceConfigurationContext;
        this.databaseDriverLoader = new DatabaseDriverLoaderFactoryImpl().create();
        logger.info("DataSourceFactoryImpl initialized successfully");
    }

    @Override
    public DataSource create() throws Exception {
        logger.info("Starting DataSource creation process");

        try {
            logger.debug("Validating persistenceConfigurationContext.");
            Objects.requireNonNull(persistenceConfigurationContext, "PersistenceConfigurationContext cannot be null.");

            logger.debug("Retrieving persistence configuration.");
            PersistenceConfiguration persistenceConfiguration = persistenceConfigurationContext.getPersistenceConfiguration();
            Objects.requireNonNull(persistenceConfiguration, "PersistenceConfiguration cannot be null.");
            logger.debug("Retrieved persistence configuration.");

            logger.debug("Retrieving data source configuration.");
            DataSourceConfiguration dataSourceConfiguration = persistenceConfiguration.getDataSourceConfiguration();
            Objects.requireNonNull(dataSourceConfiguration, "DataSourceConfiguration cannot be null.");
            logger.debug("Retrieved data source configuration for JDBC URL: {}.", dataSourceConfiguration.getJdbcUrl());

            // Load database driver before creating data source
            logger.debug("Loading database driver before creating data source");
            databaseDriverLoader.loadDriver(dataSourceConfiguration);

            DataSource dataSource = create(dataSourceConfiguration);
            logger.info("DataSource created successfully.");
            return dataSource;

        } catch (Exception e) {
            logger.error("Failed to create DataSource, error: '{}'.", e.getMessage(), e);
            throw e;
        }
    }

    private HikariDataSource create(DataSourceConfiguration configuration) {
        logger.debug("Creating HikariDataSource with configuration");

        try {
            HikariConfig hikariConfig = new HikariConfig();

            logger.debug("Setting JDBC URL: {}", configuration.getJdbcUrl());
            hikariConfig.setJdbcUrl(configuration.getJdbcUrl());

            logger.debug("Setting username: {}", configuration.getUsername());
            hikariConfig.setUsername(configuration.getUsername());

            // Don't log the actual password for security reasons
            logger.debug("Setting password: [PROTECTED]");
            hikariConfig.setPassword(configuration.getPassword());

            // Set driver class name if provided
            if (configuration.getDriverClassName() != null && !configuration.getDriverClassName().trim().isEmpty()) {
                logger.debug("Setting driver class name: {}", configuration.getDriverClassName());
                hikariConfig.setDriverClassName(configuration.getDriverClassName());
            }

            if (configuration.getProperties() != null && !configuration.getProperties().isEmpty()) {
                logger.debug("Adding {} custom data source properties", configuration.getProperties().size());
                configuration.getProperties().forEach((key, value) -> {
                    logger.trace("Adding property: {} = {}", key, value);
                    hikariConfig.addDataSourceProperty(key, value);
                });
            } else {
                logger.debug("No custom data source properties to add");
            }

            logger.debug("Creating HikariDataSource instance");
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            logger.info("HikariDataSource created successfully for URL: {}", configuration.getJdbcUrl());

            return dataSource;

        } catch (Exception e) {
            logger.error("Failed to create HikariDataSource for URL: '{}', error: '{}'", configuration.getJdbcUrl(), e.getMessage(), e);
            throw e;
        }
    }

}
