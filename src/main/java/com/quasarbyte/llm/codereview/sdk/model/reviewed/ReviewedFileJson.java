package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFileJson;

import java.util.List;

public class ReviewedFileJson {
    private PromptFileJson promptFile;
    private List<ReviewedComment> comments;
    private List<ReviewedThinkStep> reviewedThinkSteps;

    public PromptFileJson getPromptFile() {
        return promptFile;
    }

    public ReviewedFileJson setPromptFile(PromptFileJson promptFile) {
        this.promptFile = promptFile;
        return this;
    }

    public List<ReviewedComment> getComments() {
        return comments;
    }

    public ReviewedFileJson setComments(List<ReviewedComment> comments) {
        this.comments = comments;
        return this;
    }

    public List<ReviewedThinkStep> getReviewedThinkSteps() {
        return reviewedThinkSteps;
    }

    public ReviewedFileJson setReviewedThinkSteps(List<ReviewedThinkStep> reviewedThinkSteps) {
        this.reviewedThinkSteps = reviewedThinkSteps;
        return this;
    }
}
