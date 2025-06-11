package com.quasarbyte.llm.codereview.sdk.model.review;

import java.util.List;

public class ReviewResultItem {
    private ReviewFile file;
    private List<ReviewComment> comments;
    private List<ReviewThinkStep> thinkSteps;

    public ReviewFile getFile() {
        return file;
    }

    public ReviewResultItem setFile(ReviewFile file) {
        this.file = file;
        return this;
    }

    public List<ReviewComment> getComments() {
        return comments;
    }

    public ReviewResultItem setComments(List<ReviewComment> comments) {
        this.comments = comments;
        return this;
    }

    public List<ReviewThinkStep> getThinkSteps() {
        return thinkSteps;
    }

    public ReviewResultItem setThinkSteps(List<ReviewThinkStep> thinkSteps) {
        this.thinkSteps = thinkSteps;
        return this;
    }
}
