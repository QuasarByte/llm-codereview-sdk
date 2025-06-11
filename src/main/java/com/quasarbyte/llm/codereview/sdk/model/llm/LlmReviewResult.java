package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

@JsonClassDescription("Final code review result containing the list of all reviewed source files with detected issues and suggestions.")
public class LlmReviewResult {
    @JsonPropertyDescription("List of reviewed source files with identified problems and recommendations. Each item includes file name and related comments.")
    private List<LlmReviewedFile> files;

    public List<LlmReviewedFile> getFiles() {
        return files;
    }

    public LlmReviewResult setFiles(List<LlmReviewedFile> files) {
        this.files = files;
        return this;
    }
}