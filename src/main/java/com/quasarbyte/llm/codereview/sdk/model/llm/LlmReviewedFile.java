package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

@JsonClassDescription("Result of analyzing a single source file during code review. Includes file name, file id, and a list of review comments related to this file.")
public class LlmReviewedFile {
    @JsonPropertyDescription("Unique identifier of the source file as defined in the input prompt. Used to associate this file with comments.")
    private Long fileId;
    @JsonPropertyDescription("Name of the reviewed source file.")
    private String fileName;
    @JsonPropertyDescription("List of review comments describing detected issues or improvement suggestions in this file. Each comment includes location, explanation, and suggested fix.")
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