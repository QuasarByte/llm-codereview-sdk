package com.quasarbyte.llm.codereview.sdk.model.llm;

import java.util.List;

public class LlmReviewResult {
    private List<LlmReviewedFile> files;
    private List<ThinkStep> thinkSteps;

    public List<LlmReviewedFile> getFiles() {
        return files;
    }

    public LlmReviewResult setFiles(List<LlmReviewedFile> files) {
        this.files = files;
        return this;
    }

    public List<ThinkStep> getThinkSteps() {
        return thinkSteps;
    }

    public LlmReviewResult setThinkSteps(List<ThinkStep> thinkSteps) {
        this.thinkSteps = thinkSteps;
        return this;
    }
}
