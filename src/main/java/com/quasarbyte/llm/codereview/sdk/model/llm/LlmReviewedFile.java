package com.quasarbyte.llm.codereview.sdk.model.llm;

import java.util.List;

public class LlmReviewedFile {
    private Long fileId;
    private String fileName;
    private List<LlmReviewComment> comments;

    public Long getFileId() {
        return fileId;
    }

    public LlmReviewedFile setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public LlmReviewedFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public List<LlmReviewComment> getComments() {
        return comments;
    }

    public LlmReviewedFile setComments(List<LlmReviewComment> comments) {
        this.comments = comments;
        return this;
    }
}
