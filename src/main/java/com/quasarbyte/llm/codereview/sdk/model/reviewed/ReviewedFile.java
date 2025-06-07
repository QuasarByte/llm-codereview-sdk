package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;

import java.util.List;

public class ReviewedFile {
    private PromptFile promptFile;
    private List<ReviewedComment> comments;

    public PromptFile getPromptFile() {
        return promptFile;
    }

    public ReviewedFile setPromptFile(PromptFile promptFile) {
        this.promptFile = promptFile;
        return this;
    }

    public List<ReviewedComment> getComments() {
        return comments;
    }

    public ReviewedFile setComments(List<ReviewedComment> comments) {
        this.comments = comments;
        return this;
    }
}
