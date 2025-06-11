package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.migration.LiquibaseMigrationService;
import liquibase.resource.ResourceAccessor;

public interface LiquibaseInnerRunnerFactory {

    LiquibaseInnerRunner create();

    LiquibaseInnerRunner create(ResourceAccessor resourceAccessor);

    LiquibaseInnerRunner create(LiquibaseConfig config);

    LiquibaseInnerRunner createWithChangelogPath(String changeLogPath);

    LiquibaseMigrationService createMigrationManager();

    LiquibaseMigrationService createMigrationManager(LiquibaseInnerRunner liquibaseInnerRunner);

    LiquibaseMigrationService createMigrationManager(LiquibaseConfig config);
}
