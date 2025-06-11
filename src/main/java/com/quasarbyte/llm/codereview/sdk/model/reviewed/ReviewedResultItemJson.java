package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.llm.ThinkStep;

import java.util.List;

public class ReviewedResultItemJson {
    private List<ReviewedFileJson> files;
    private List<ThinkStep> thinkSteps;
    private ReviewedCompletionUsage completionUsage;

    public List<ReviewedFileJson> getFiles() {
        return files;
    }

    public ReviewedResultItemJson setFiles(List<ReviewedFileJson> files) {
        this.files = files;
        return this;
    }

    public List<ThinkStep> getThinkSteps() {
        return thinkSteps;
    }

    public ReviewedResultItemJson setThinkSteps(List<ThinkStep> thinkSteps) {
        this.thinkSteps = thinkSteps;
        return this;
    }

    public ReviewedCompletionUsage getCompletionUsage() {
        return completionUsage;
    }

    public ReviewedResultItemJson setCompletionUsage(ReviewedCompletionUsage completionUsage) {
        this.completionUsage = completionUsage;
        return this;
    }
}
