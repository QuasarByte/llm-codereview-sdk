package com.quasarbyte.llm.codereview.sdk.model.llm;

public class ThinkStep {
    private Long fileId;
    private String fileName;
    private Long ruleId;
    private String ruleCode;
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
