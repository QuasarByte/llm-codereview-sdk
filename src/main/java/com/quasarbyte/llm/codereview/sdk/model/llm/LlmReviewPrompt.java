package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;

import java.util.List;

public class LlmReviewPrompt {
    private List<String> reviewPromptTexts;
    private List<String> reviewTargetPromptTexts;
    private List<String> fileGroupPromptTexts;
    private List<String> systemPromptTexts;
    private List<PromptRule> rules;
    private List<LlmFile> files;

    public List<String> getReviewPromptTexts() {
        return reviewPromptTexts;
    }

    public LlmReviewPrompt setReviewPromptTexts(List<String> reviewPromptTexts) {
        this.reviewPromptTexts = reviewPromptTexts;
        return this;
    }

    public List<String> getReviewTargetPromptTexts() {
        return reviewTargetPromptTexts;
    }

    public LlmReviewPrompt setReviewTargetPromptTexts(List<String> reviewTargetPromptTexts) {
        this.reviewTargetPromptTexts = reviewTargetPromptTexts;
        return this;
    }

    public List<String> getFileGroupPromptTexts() {
        return fileGroupPromptTexts;
    }

    public LlmReviewPrompt setFileGroupPromptTexts(List<String> fileGroupPromptTexts) {
        this.fileGroupPromptTexts = fileGroupPromptTexts;
        return this;
    }

    public List<String> getSystemPromptTexts() {
        return systemPromptTexts;
    }

    public LlmReviewPrompt setSystemPromptTexts(List<String> systemPromptTexts) {
        this.systemPromptTexts = systemPromptTexts;
        return this;
    }

    public List<PromptRule> getRules() {
        return rules;
    }

    public LlmReviewPrompt setRules(List<PromptRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<LlmFile> getFiles() {
        return files;
    }

    public LlmReviewPrompt setFiles(List<LlmFile> files) {
        this.files = files;
        return this;
    }
}
