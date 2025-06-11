package com.quasarbyte.llm.codereview.sdk.service.db.core.driver;

/**
 * Factory interface for creating DatabaseDriverLoader instances.
 */
public interface DatabaseDriverLoaderFactory {

    /**
     * Creates a new DatabaseDriverLoader instance.
     *
     * @return a configured DatabaseDriverLoader instance
     */
    DatabaseDriverLoader create();
}
