package com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl.LiquibaseInnerRunnerImpl;

/**
 * Implementation of LiquibaseMigrationManagerFactory.
 * Provides various factory methods for creating LiquibaseMigrationManager instances with different configurations.
 */
public class LiquibaseMigrationManagerFactoryImpl implements LiquibaseMigrationManagerFactory {

    private final DBConnectionManager dbConnectionManager;

    public LiquibaseMigrationManagerFactoryImpl(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    @Override
    public LiquibaseMigrationManager create() {
        return new LiquibaseMigrationManagerImpl(dbConnectionManager);
    }

    @Override
    public LiquibaseMigrationManager create(LiquibaseInnerRunner liquibaseRunner) {
        return new LiquibaseMigrationManagerImpl(dbConnectionManager, liquibaseRunner);
    }

    @Override
    public LiquibaseMigrationManager createWithChangelogPath(String changeLogPath) {
        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl(
            LiquibaseConfig.builder()
                .changeLogPath(changeLogPath)
                .build()
        );
        return new LiquibaseMigrationManagerImpl(dbConnectionManager, runner, changeLogPath);
    }

    @Override
    public LiquibaseMigrationManager create(LiquibaseConfig config) {
        LiquibaseInnerRunner runner = new LiquibaseInnerRunnerImpl(config);
        return new LiquibaseMigrationManagerImpl(dbConnectionManager, runner, config.getChangeLogPath());
    }
}
