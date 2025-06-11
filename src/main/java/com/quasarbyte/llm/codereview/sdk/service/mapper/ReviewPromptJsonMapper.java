package com.quasarbyte.llm.codereview.sdk.service.mapper;

import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson;

/**
 * Mapper interface for converting between ReviewPrompt and ReviewPromptJson objects
 */
public interface ReviewPromptJsonMapper {
    
    /**
     * Maps ReviewPrompt to ReviewPromptJson
     * @param reviewPrompt the source ReviewPrompt object
     * @return mapped ReviewPromptJson object
     */
    ReviewPromptJson toJson(ReviewPrompt reviewPrompt);
    
    /**
     * Maps ReviewPromptJson to ReviewPrompt
     * @param reviewPromptJson the source ReviewPromptJson object
     * @return mapped ReviewPrompt object
     */
    ReviewPrompt fromJson(ReviewPromptJson reviewPromptJson);
}
