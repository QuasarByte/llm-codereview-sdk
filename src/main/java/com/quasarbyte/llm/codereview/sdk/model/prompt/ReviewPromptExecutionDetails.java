package com.quasarbyte.llm.codereview.sdk.model.prompt;

import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;

import java.util.List;

public class ReviewPromptExecutionDetails {
    private List<ResolvedFilePath> resolvedFilePaths;

    public List<ResolvedFilePath> getResolvedFilePaths() {
        return resolvedFilePaths;
    }

    public ReviewPromptExecutionDetails setResolvedFilePaths(List<ResolvedFilePath> resolvedFilePaths) {
        this.resolvedFilePaths = resolvedFilePaths;
        return this;
    }
}
