package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;

import java.util.List;

public interface ReviewPromptCreator {
    List<ReviewPrompt> create(List<ResolvedFilesRules> resolvedFilesRulesList, boolean useReasoning);
}
