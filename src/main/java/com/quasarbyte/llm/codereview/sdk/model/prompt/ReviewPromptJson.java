package com.quasarbyte.llm.codereview.sdk.model.prompt;

import java.util.List;

public class ReviewPromptJson {
    private List<String> fileGroupPromptTexts;
    private List<String> reviewTargetPromptTexts;
    private List<String> reviewPromptTexts;
    private List<String> systemPromptTexts;
    private List<PromptRule> rules;
    private List<PromptFileJson> files;
    private Boolean useReasoning;

    public List<String> getFileGroupPromptTexts() {
        return fileGroupPromptTexts;
    }

    public ReviewPromptJson setFileGroupPromptTexts(List<String> fileGroupPromptTexts) {
        this.fileGroupPromptTexts = fileGroupPromptTexts;
        return this;
    }

    public List<String> getReviewTargetPromptTexts() {
        return reviewTargetPromptTexts;
    }

    public ReviewPromptJson setReviewTargetPromptTexts(List<String> reviewTargetPromptTexts) {
        this.reviewTargetPromptTexts = reviewTargetPromptTexts;
        return this;
    }

    public List<String> getReviewPromptTexts() {
        return reviewPromptTexts;
    }

    public ReviewPromptJson setReviewPromptTexts(List<String> reviewPromptTexts) {
        this.reviewPromptTexts = reviewPromptTexts;
        return this;
    }

    public List<String> getSystemPromptTexts() {
        return systemPromptTexts;
    }

    public ReviewPromptJson setSystemPromptTexts(List<String> systemPromptTexts) {
        this.systemPromptTexts = systemPromptTexts;
        return this;
    }

    public List<PromptRule> getRules() {
        return rules;
    }

    public ReviewPromptJson setRules(List<PromptRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<PromptFileJson> getFiles() {
        return files;
    }

    public ReviewPromptJson setFiles(List<PromptFileJson> files) {
        this.files = files;
        return this;
    }

    public Boolean getUseReasoning() {
        return useReasoning;
    }

    public ReviewPromptJson setUseReasoning(Boolean useReasoning) {
        this.useReasoning = useReasoning;
        return this;
    }
}
