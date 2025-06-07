package com.quasarbyte.llm.codereview.sdk.model.prompt;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;

public class PromptFile {
    private Long id;
    private ResolvedFilePath resolvedFilePath;
    private SourceFile sourceFile;

    public Long getId() {
        return id;
    }

    public PromptFile setId(Long id) {
        this.id = id;
        return this;
    }

    public ResolvedFilePath getResolvedFilePath() {
        return resolvedFilePath;
    }

    public PromptFile setResolvedFilePath(ResolvedFilePath resolvedFilePath) {
        this.resolvedFilePath = resolvedFilePath;
        return this;
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public PromptFile setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
        return this;
    }
}
