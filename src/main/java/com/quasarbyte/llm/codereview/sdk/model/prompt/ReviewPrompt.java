package com.quasarbyte.llm.codereview.sdk.model.prompt;

import java.util.List;

public class ReviewPrompt {
    private List<String> fileGroupPromptTexts;
    private List<String> reviewTargetPromptTexts;
    private List<String> reviewPromptTexts;
    private List<String> systemPromptTexts;
    private List<PromptRule> rules;
    private List<PromptFile> files;
    private ReviewPromptExecutionDetails executionDetails;

    public List<String> getFileGroupPromptTexts() {
        return fileGroupPromptTexts;
    }

    public ReviewPrompt setFileGroupPromptTexts(List<String> fileGroupPromptTexts) {
        this.fileGroupPromptTexts = fileGroupPromptTexts;
        return this;
    }

    public List<String> getReviewTargetPromptTexts() {
        return reviewTargetPromptTexts;
    }

    public ReviewPrompt setReviewTargetPromptTexts(List<String> reviewTargetPromptTexts) {
        this.reviewTargetPromptTexts = reviewTargetPromptTexts;
        return this;
    }

    public List<String> getReviewPromptTexts() {
        return reviewPromptTexts;
    }

    public ReviewPrompt setReviewPromptTexts(List<String> reviewPromptTexts) {
        this.reviewPromptTexts = reviewPromptTexts;
        return this;
    }

    public List<String> getSystemPromptTexts() {
        return systemPromptTexts;
    }

    public ReviewPrompt setSystemPromptTexts(List<String> systemPromptTexts) {
        this.systemPromptTexts = systemPromptTexts;
        return this;
    }

    public List<PromptRule> getRules() {
        return rules;
    }

    public ReviewPrompt setRules(List<PromptRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<PromptFile> getFiles() {
        return files;
    }

    public ReviewPrompt setFiles(List<PromptFile> files) {
        this.files = files;
        return this;
    }

    public ReviewPromptExecutionDetails getExecutionDetails() {
        return executionDetails;
    }

    public ReviewPrompt setExecutionDetails(ReviewPromptExecutionDetails executionDetails) {
        this.executionDetails = executionDetails;
        return this;
    }
}
