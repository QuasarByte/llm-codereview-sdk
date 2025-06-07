package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import java.util.List;

public class ReviewedResultItem {
    private ReviewedExecutionDetailsItem executionDetailsItem;
    private List<ReviewedFile> files;
    private ReviewedCompletionUsage completionUsage;

    public ReviewedExecutionDetailsItem getExecutionDetailsItem() {
        return executionDetailsItem;
    }

    public ReviewedResultItem setExecutionDetailsItem(ReviewedExecutionDetailsItem executionDetailsItem) {
        this.executionDetailsItem = executionDetailsItem;
        return this;
    }

    public List<ReviewedFile> getFiles() {
        return files;
    }

    public ReviewedResultItem setFiles(List<ReviewedFile> files) {
        this.files = files;
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
