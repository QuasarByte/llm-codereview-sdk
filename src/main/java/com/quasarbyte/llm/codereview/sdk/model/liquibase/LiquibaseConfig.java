package com.quasarbyte.llm.codereview.sdk.model.liquibase;

import liquibase.resource.ResourceAccessor;

/**
 * Configuration class for Liquibase operations.
 * Provides centralized configuration for changelog paths, contexts, and other Liquibase settings.
 */
public class LiquibaseConfig {

    public static final String DEFAULT_CHANGELOG_PATH = "com/quasarbyte/llm/codereview/sdk/liquibase/changelog/db.changelog-master.yaml";

    private final String changeLogPath;
    private final String contexts;
    private final String labels;
    private final ResourceAccessor resourceAccessor;

    private LiquibaseConfig(Builder builder) {
        this.changeLogPath = builder.changeLogPath != null ? builder.changeLogPath : DEFAULT_CHANGELOG_PATH;
        this.contexts = builder.contexts;
        this.labels = builder.labels;
        this.resourceAccessor = builder.resourceAccessor;
    }

    public String getChangeLogPath() {
        return changeLogPath;
    }

    public String getContexts() {
        return contexts;
    }

    public String getLabels() {
        return labels;
    }

    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String changeLogPath;
        private String contexts;
        private String labels;
        private ResourceAccessor resourceAccessor;

        public Builder changeLogPath(String changeLogPath) {
            this.changeLogPath = changeLogPath;
            return this;
        }

        public Builder contexts(String contexts) {
            this.contexts = contexts;
            return this;
        }

        public Builder labels(String labels) {
            this.labels = labels;
            return this;
        }

        public Builder resourceAccessor(ResourceAccessor resourceAccessor) {
            this.resourceAccessor = resourceAccessor;
            return this;
        }

        public LiquibaseConfig build() {
            return new LiquibaseConfig(this);
        }
    }
}
