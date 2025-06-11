package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.impl;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.LiquibaseMigrationService;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.impl.LiquibaseMigrationServiceImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunnerFactory;
import liquibase.resource.ResourceAccessor;

public class LiquibaseInnerRunnerFactoryImpl implements LiquibaseInnerRunnerFactory {

    @Override
    public LiquibaseInnerRunner create() {
        return new LiquibaseInnerRunnerImpl();
    }

    @Override
    public LiquibaseInnerRunner create(ResourceAccessor resourceAccessor) {
        LiquibaseConfig config = LiquibaseConfig.builder()
                .resourceAccessor(resourceAccessor)
                .build();
        return new LiquibaseInnerRunnerImpl(config);
    }

    @Override
    public LiquibaseInnerRunner create(LiquibaseConfig config) {
        return new LiquibaseInnerRunnerImpl(config);
    }

    @Override
    public LiquibaseInnerRunner createWithChangelogPath(String changeLogPath) {
        LiquibaseConfig config = LiquibaseConfig.builder()
                .changeLogPath(changeLogPath)
                .build();
        return new LiquibaseInnerRunnerImpl(config);
    }

    @Override
    public LiquibaseMigrationService createMigrationManager() {
        return new LiquibaseMigrationServiceImpl(create());
    }

    @Override
    public LiquibaseMigrationService createMigrationManager(LiquibaseInnerRunner liquibaseInnerRunner) {
        return new LiquibaseMigrationServiceImpl(liquibaseInnerRunner);
    }

    @Override
    public LiquibaseMigrationService createMigrationManager(LiquibaseConfig config) {
        LiquibaseInnerRunner runnerService = create(config);
        return new LiquibaseMigrationServiceImpl(runnerService);
    }
}
