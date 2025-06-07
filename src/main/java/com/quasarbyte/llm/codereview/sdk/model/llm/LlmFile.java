package com.quasarbyte.llm.codereview.sdk.model.llm;

public class LlmFile {
    private Long id;
    private LlmFileMetadata metadata;
    private String content;

    public Long getId() {
        return id;
    }

    public LlmFile setId(Long id) {
        this.id = id;
        return this;
    }

    public LlmFileMetadata getMetadata() {
        return metadata;
    }

    public LlmFile setMetadata(LlmFileMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getContent() {
        return content;
    }

    public LlmFile setContent(String content) {
        this.content = content;
        return this;
    }
}
