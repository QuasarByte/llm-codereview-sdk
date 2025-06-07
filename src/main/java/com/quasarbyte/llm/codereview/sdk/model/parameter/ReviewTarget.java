package com.quasarbyte.llm.codereview.sdk.model.parameter;

import java.util.List;

public class ReviewTarget {
    private String reviewTargetName;
    private List<FileGroup> fileGroups;
    private List<Rule> rules;
    private List<String> reviewTargetPrompts;

    public String getReviewTargetName() {
        return reviewTargetName;
    }

    public ReviewTarget setReviewTargetName(String reviewTargetName) {
        this.reviewTargetName = reviewTargetName;
        return this;
    }

    public List<FileGroup> getFileGroups() {
        return fileGroups;
    }

    public ReviewTarget setFileGroups(List<FileGroup> fileGroups) {
        this.fileGroups = fileGroups;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public ReviewTarget setRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public List<String> getReviewTargetPrompts() {
        return reviewTargetPrompts;
    }

    public ReviewTarget setReviewTargetPrompts(List<String> reviewTargetPrompts) {
        this.reviewTargetPrompts = reviewTargetPrompts;
        return this;
    }
}
