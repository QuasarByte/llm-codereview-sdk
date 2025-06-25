package com.quasarbyte.llm.codereview.sdk.model.datasource;

import java.util.Map;

public class DataSourceConfiguration {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private Map<String, String> properties;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public DataSourceConfiguration setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public DataSourceConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DataSourceConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public DataSourceConfiguration setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public DataSourceConfiguration setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}
