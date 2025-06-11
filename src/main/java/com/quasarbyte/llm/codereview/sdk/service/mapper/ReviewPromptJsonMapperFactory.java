package com.quasarbyte.llm.codereview.sdk.service.mapper;

/**
 * Factory interface for creating ReviewPromptJsonMapper instances
 */
public interface ReviewPromptJsonMapperFactory {
    
    /**
     * Creates a new ReviewPromptJsonMapper instance
     * @return configured ReviewPromptJsonMapper instance
     */
    ReviewPromptJsonMapper create();
}
