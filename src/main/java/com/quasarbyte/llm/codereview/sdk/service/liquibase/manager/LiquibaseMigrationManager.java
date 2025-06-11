package com.quasarbyte.llm.codereview.sdk.service.liquibase.manager;

import liquibase.changelog.ChangeSet;

import java.util.List;

public interface LiquibaseMigrationManager {

    void runMigrations();

    boolean hasUnrunChanges();

    String getStatus();

    void rollback(int count);

    void rollbackToTag(String tag);

    boolean validateChangelog();

    List<ChangeSet> listUnrunChangeSets();

    String generateChangeLog(String outputPath);

    void dropAll();
}