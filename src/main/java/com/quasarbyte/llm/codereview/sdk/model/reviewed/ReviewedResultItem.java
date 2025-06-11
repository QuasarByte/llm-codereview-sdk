package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.llm.ThinkStep;

import java.util.List;

public class ReviewedResultItem {
    private List<ReviewedFile> files;
    private List<ThinkStep> thinkSteps;
    private ReviewedCompletionUsage completionUsage;

    public List<ReviewedFile> getFiles() {
        return files;
    }

    public ReviewedResultItem setFiles(List<ReviewedFile> files) {
        this.files = files;
        return this;
    }

    public List<ThinkStep> getThinkSteps() {
        return thinkSteps;
    }

    public ReviewedResultItem setThinkSteps(List<ThinkStep> thinkSteps) {
        this.thinkSteps = thinkSteps;
        return this;
    }

    public ReviewedCompletionUsage getCompletionUsage() {
        return completionUsage;
    }

    public ReviewedResultItem setCompletionUsage(ReviewedCompletionUsage completionUsage) {
        this.completionUsage = completionUsage;
        return this;
    }
}
