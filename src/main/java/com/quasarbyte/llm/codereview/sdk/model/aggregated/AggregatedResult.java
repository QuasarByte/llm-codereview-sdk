package com.quasarbyte.llm.codereview.sdk.model.aggregated;

import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedFile;

import java.util.List;

public class AggregatedResult {
    private List<AggregatedFile> files;
    private List<ReviewedFile> unhandledFiles;
    private AggregatedCompletionUsage completionUsage;

    public List<AggregatedFile> getFiles() {
        return files;
    }

    public AggregatedResult setFiles(List<AggregatedFile> files) {
        this.files = files;
        return this;
    }

    public List<ReviewedFile> getUnhandledFiles() {
        return unhandledFiles;
    }

    public AggregatedResult setUnhandledFiles(List<ReviewedFile> unhandledFiles) {
        this.unhandledFiles = unhandledFiles;
        return this;
    }

    public AggregatedCompletionUsage getCompletionUsage() {
        return completionUsage;
    }

    public AggregatedResult setCompletionUsage(AggregatedCompletionUsage completionUsage) {
        this.completionUsage = completionUsage;
        return this;
    }
}
