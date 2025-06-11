package com.quasarbyte.llm.codereview.sdk.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SQLightJdbcUrlValue {

    private static final Logger logger = LoggerFactory.getLogger(SQLightJdbcUrlValue.class);

    public String getJdbcUrl(Path temporaryDirectoryPath, String databaseName) {
        String dbType = System.getProperty("test.db.type", "memory");
        logger.debug("Database type from system property: " + dbType);

        switch (dbType) {
            case "temp":
                String tempPath = temporaryDirectoryPath.resolve(databaseName).toFile().getAbsolutePath();
                logger.debug("Using temp directory database: " + tempPath);
                return "jdbc:sqlite:" + tempPath;

            case "target":
                String targetPath = Paths.get("target", databaseName).toAbsolutePath()
                        .normalize().toFile().getAbsolutePath();
                logger.debug("Using target directory database: " + targetPath);
                return "jdbc:sqlite:" + targetPath;

            case "memory":
                logger.debug("Using in memory database");
                return "jdbc:sqlite::memory:";

            default:
                logger.debug("Using in-memory database");
                return "jdbc:sqlite::memory:";
        }
    }

    public String getJdbcUrl(Path temporaryDirectoryPath) {
        return getJdbcUrl(temporaryDirectoryPath, "test.db");
    }

}
