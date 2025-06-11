package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.inner;

import com.quasarbyte.llm.codereview.sdk.service.liquibase.runner.LiquibaseRunnerService;
import liquibase.changelog.ChangeSet;

import java.sql.Connection;
import java.util.List;

public interface LiquibaseInnerRunner {

    void runMigrations(Connection connection, String changeLogPath);

    boolean hasUnrunChanges(Connection connection, String changeLogPath);

    String getStatus(Connection connection, String changeLogPath);

    void rollback(Connection connection, String changeLogPath, int count);

    void rollbackToTag(Connection connection, String changeLogPath, String tag);

    boolean validateChangelog(Connection connection, String changeLogPath);

    List<ChangeSet> listUnrunChangeSets(Connection connection, String changeLogPath);

    String generateChangeLog(Connection connection, String outputPath);

    void dropAll(Connection connection, String changeLogPath);

    LiquibaseRunnerService createRunner(Connection connection, String changeLogPath);
}
