package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;

import java.util.List;

public class ReviewedExecutionDetails {
    private List<List<ResolvedFilePath>> resolvedFilePaths;
    private List<ReviewedResultItem> reviewedResultItems;

    public List<List<ResolvedFilePath>> getResolvedFilePaths() {
        return resolvedFilePaths;
    }

    public ReviewedExecutionDetails setResolvedFilePaths(List<List<ResolvedFilePath>> resolvedFilePaths) {
        this.resolvedFilePaths = resolvedFilePaths;
        return this;
    }

    public List<ReviewedResultItem> getReviewResultItems() {
        return reviewedResultItems;
    }

    public ReviewedExecutionDetails setReviewResultItems(List<ReviewedResultItem> reviewedResultItems) {
        this.reviewedResultItems = reviewedResultItems;
        return this;
    }
}
