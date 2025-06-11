package com.quasarbyte.llm.codereview.sdk.service.liquibase.runner;

import liquibase.changelog.ChangeSet;

import java.sql.Connection;
import java.util.List;

public interface LiquibaseRunnerService extends AutoCloseable {

    void update();

    void rollback(int count);

    void rollbackToTag(String tag);

    void validate();

    List<ChangeSet> listUnrunChangeSets();

    boolean hasUnrunChanges();

    String getStatus();

    void dropAll();

    String generateChangeLog(String outputPath);

    String getChangeLogPath();

    Connection getConnection();

    @Override
    void close();
}
