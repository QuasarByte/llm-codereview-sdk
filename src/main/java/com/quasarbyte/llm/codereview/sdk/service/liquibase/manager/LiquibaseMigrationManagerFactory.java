package com.quasarbyte.llm.codereview.sdk.service.liquibase.manager;

import com.quasarbyte.llm.codereview.sdk.model.liquibase.LiquibaseConfig;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner.LiquibaseInnerRunner;

public interface LiquibaseMigrationManagerFactory {

    LiquibaseMigrationManager create();

    LiquibaseMigrationManager create(LiquibaseInnerRunner liquibaseRunner);

    LiquibaseMigrationManager createWithChangelogPath(String changeLogPath);

    LiquibaseMigrationManager create(LiquibaseConfig config);
}
