package com.quasarbyte.llm.codereview.sdk.model.prompt;

import com.quasarbyte.llm.codereview.sdk.model.SourceFileKey;

public class PromptFileJson {
    private Long id;
    private SourceFileKey sourceFileKey;

    public Long getId() {
        return id;
    }

    public PromptFileJson setId(Long id) {
        this.id = id;
        return this;
    }

    public SourceFileKey getSourceFileKey() {
        return sourceFileKey;
    }

    public PromptFileJson setSourceFileKey(SourceFileKey sourceFileKey) {
        this.sourceFileKey = sourceFileKey;
        return this;
    }
}
