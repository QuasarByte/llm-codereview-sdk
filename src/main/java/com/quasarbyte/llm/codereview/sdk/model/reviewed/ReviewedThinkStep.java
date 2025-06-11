package com.quasarbyte.llm.codereview.sdk.model.reviewed;

public class ReviewedThinkStep {
    private Long fileId;
    private String fileName;
    private Long ruleId;
    private String ruleCode;
    private String thinkText;

    public Long getFileId() {
        return fileId;
    }

    public ReviewedThinkStep setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ReviewedThinkStep setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public ReviewedThinkStep setRuleId(Long ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public ReviewedThinkStep setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
        return this;
    }

    public String getThinkText() {
        return thinkText;
    }

    public ReviewedThinkStep setThinkText(String thinkText) {
        this.thinkText = thinkText;
        return this;
    }
}
