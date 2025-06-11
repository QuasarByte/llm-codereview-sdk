package com.quasarbyte.llm.codereview.sdk.model.prompt;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;

public class PromptFile {
    private Long id;
    private SourceFile sourceFile;

    public Long getId() {
        return id;
    }

    public PromptFile setId(Long id) {
        this.id = id;
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
