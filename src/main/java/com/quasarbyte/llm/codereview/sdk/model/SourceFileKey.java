package com.quasarbyte.llm.codereview.sdk.model;

import java.util.Objects;

public class SourceFileKey {
    private String filePath;
    private String codePage;

    public String getFilePath() {
        return filePath;
    }

    public SourceFileKey setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getCodePage() {
        return codePage;
    }

    public SourceFileKey setCodePage(String codePage) {
        this.codePage = codePage;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SourceFileKey that = (SourceFileKey) o;
        return Objects.equals(filePath, that.filePath) && Objects.equals(codePage, that.codePage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, codePage);
    }
}
