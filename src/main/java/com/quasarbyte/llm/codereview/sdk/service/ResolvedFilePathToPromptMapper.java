package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;

public interface ResolvedFilePathToPromptMapper {
    ReviewPrompt map(ResolvedFilesRules filesRules, Boolean useReasoning);
}
