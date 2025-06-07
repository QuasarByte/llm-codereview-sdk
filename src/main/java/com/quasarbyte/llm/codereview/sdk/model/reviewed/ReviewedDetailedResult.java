package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import java.util.List;

public class ReviewedDetailedResult {
    private ReviewedExecutionDetails executionDetails;
    private List<ReviewedFile> files;

    public ReviewedExecutionDetails getExecutionDetails() {
        return executionDetails;
    }

    public ReviewedDetailedResult setExecutionDetails(ReviewedExecutionDetails executionDetails) {
        this.executionDetails = executionDetails;
        return this;
    }

    public List<ReviewedFile> getFiles() {
        return files;
    }

    public ReviewedDetailedResult setFiles(List<ReviewedFile> files) {
        this.files = files;
        return this;
    }
}
