package com.quasarbyte.llm.codereview.sdk.repository;

/**
 * Factory interface for creating ReviewRepository instances.
 */
public interface ReviewRepositoryFactory {
    
    /**
     * Creates a new ReviewRepository instance.
     * 
     * @return a new ReviewRepository instance
     */
    ReviewRepository create();
}
