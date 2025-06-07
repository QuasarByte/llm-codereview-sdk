package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface ResolvedFilePathToPromptMapper {
    ReviewPrompt map(ResolvedFilesRules filesRules, AtomicLong fileId, AtomicLong ruleId, Map<String, SourceFile> sourceFileCache);
}
