package com.quasarbyte.llm.codereview.sdk.model.parameter;

import java.util.List;

public class FileGroup {
    private String fileGroupName;
    private List<String> paths;
    private Integer filesBatchSize;
    private List<Rule> rules;
    private List<String> fileGroupPrompts;
    private String codePage;

    public String getFileGroupName() {
        return fileGroupName;
    }

    public FileGroup setFileGroupName(String fileGroupName) {
        this.fileGroupName = fileGroupName;
        return this;
    }

    public List<String> getPaths() {
        return paths;
    }

    public FileGroup setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    public Integer getFilesBatchSize() {
        return filesBatchSize;
    }

    public FileGroup setFilesBatchSize(Integer filesBatchSize) {
        this.filesBatchSize = filesBatchSize;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public FileGroup setRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public List<String> getFileGroupPrompts() {
        return fileGroupPrompts;
    }

    public FileGroup setFileGroupPrompts(List<String> fileGroupPrompts) {
        this.fileGroupPrompts = fileGroupPrompts;
        return this;
    }

    public String getCodePage() {
        return codePage;
    }

    public FileGroup setCodePage(String codePage) {
        this.codePage = codePage;
        return this;
    }
}
