package com.quasarbyte.llm.codereview.sdk.model.review;

public class ReviewThinkStep {
    private Long fileId;
    private String fileName;
    private Long ruleId;
    private String ruleCode;
    private String thinkText;

    public Long getFileId() {
        return fileId;
    }

    public ReviewThinkStep setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ReviewThinkStep setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public ReviewThinkStep setRuleId(Long ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public ReviewThinkStep setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
        return this;
    }

    public String getThinkText() {
        return thinkText;
    }

    public ReviewThinkStep setThinkText(String thinkText) {
        this.thinkText = thinkText;
        return this;
    }
}
