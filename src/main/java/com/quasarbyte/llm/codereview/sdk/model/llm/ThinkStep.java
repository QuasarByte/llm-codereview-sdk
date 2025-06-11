package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("Reasoning step showing how the model analyzed a specific file against a specific rule. Helps explain why a rule was triggered or skipped.")
public class ThinkStep {
    @JsonPropertyDescription("Id of the source file this step refers to, as defined in the input prompt. Used to associate the reasoning step with the correct file")
    private Long fileId;
    @JsonPropertyDescription("Name of the file this step refers to.")
    private String fileName;
    @JsonPropertyDescription("Id of the rule being considered in this reasoning step. Must match a rule id from the input prompt.")
    private Long ruleId;
    @JsonPropertyDescription("Short code of the rule being considered. Must match a rule code from the input prompt.")
    private String ruleCode;
    @JsonPropertyDescription("Explanation of the model’s reasoning when checking this file against this rule. Should describe what was checked, what was found, and why a decision was made.")
    private String thinkText;

    public Long getFileId() {
        return fileId;
    }

    public ThinkStep setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ThinkStep setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public ThinkStep setRuleId(Long ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public ThinkStep setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
        return this;
    }

    public String getThinkText() {
        return thinkText;
    }

    public ThinkStep setThinkText(String thinkText) {
        this.thinkText = thinkText;
        return this;
    }
}
