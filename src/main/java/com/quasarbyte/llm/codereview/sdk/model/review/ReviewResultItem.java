package com.quasarbyte.llm.codereview.sdk.model.review;

import java.util.List;

public class ReviewResultItem {
    ReviewFile file;
    List<ReviewComment> comments;

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
}
