package com.quasarbyte.llm.codereview.sdk.model.aggregated;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedComment;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedThinkStep;

import java.util.List;

public class AggregatedFile {
    private SourceFile sourceFile;
    private List<ReviewedComment> comments;
    private List<ReviewedThinkStep> reviewedThinkSteps;

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public AggregatedFile setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
        return this;
    }

    public List<ReviewedComment> getComments() {
        return comments;
    }

    public AggregatedFile setComments(List<ReviewedComment> comments) {
        this.comments = comments;
        return this;
    }

    public List<ReviewedThinkStep> getReviewedThinkSteps() {
        return reviewedThinkSteps;
    }

    public AggregatedFile setReviewedThinkSteps(List<ReviewedThinkStep> reviewedThinkSteps) {
        this.reviewedThinkSteps = reviewedThinkSteps;
        return this;
    }
}
