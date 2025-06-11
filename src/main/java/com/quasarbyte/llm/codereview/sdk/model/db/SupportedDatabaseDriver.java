package com.quasarbyte.llm.codereview.sdk.model.db;

/**
 * Enumeration of supported database drivers with their corresponding JDBC driver class names.
 * This enum facilitates automatic driver loading based on the configured driver class name.
 */
public enum SupportedDatabaseDriver {

    /**
     * SQLite database driver
     */
    SQLITE("org.sqlite.JDBC", "SQLite"),

    /**
     * H2 database driver
     */
    H2("org.h2.Driver", "H2"),

    /**
     * PostgreSQL database driver
     */
    POSTGRESQL("org.postgresql.Driver", "PostgreSQL"),

    /**
     * MySQL database driver
     */
    MYSQL("com.mysql.cj.jdbc.Driver", "MySQL"),

    /**
     * MariaDB database driver
     */
    MARIADB("org.mariadb.jdbc.Driver", "MariaDB"),

    /**
     * Oracle database driver
     */
    ORACLE("oracle.jdbc.OracleDriver", "Oracle"),

    /**
     * Microsoft SQL Server database driver
     */
    SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "SQL Server");

    private final String driverClassName;
    private final String displayName;

    SupportedDatabaseDriver(String driverClassName, String displayName) {
        this.driverClassName = driverClassName;
        this.displayName = displayName;
    }

    /**
     * Gets the JDBC driver class name
     *
     * @return the driver class name
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Gets the display name of the database
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Finds a supported database driver by driver class name
     *
     * @param driverClassName the driver class name to search for
     * @return the matching SupportedDatabaseDriver, or null if not found
     */
    public static SupportedDatabaseDriver findByDriverClassName(String driverClassName) {
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            return null;
        }

        for (SupportedDatabaseDriver driver : values()) {
            if (driver.getDriverClassName().equals(driverClassName.trim())) {
                return driver;
            }
        }

        return null;
    }

    /**
     * Checks if a driver class name is supported
     *
     * @param driverClassName the driver class name to check
     * @return true if the driver is supported, false otherwise
     */
    public static boolean isSupported(String driverClassName) {
        return findByDriverClassName(driverClassName) != null;
    }
}
