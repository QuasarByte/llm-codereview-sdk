package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.service.impl.ReviewPromptCombiner;

public interface ReviewPromptCombinerFactory {
    ReviewPromptCombiner create();
}
