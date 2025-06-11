package com.quasarbyte.llm.codereview.sdk.repository;

/**
 * Factory interface for creating RunRepository instances.
 */
public interface RunRepositoryFactory {
    
    /**
     * Creates a new RunRepository instance.
     * 
     * @return a new RunRepository instance
     */
    RunRepository create();
}
