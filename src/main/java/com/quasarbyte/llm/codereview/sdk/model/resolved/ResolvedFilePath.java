package com.quasarbyte.llm.codereview.sdk.model.resolved;

public class ResolvedFilePath {
    private ResolvedFileGroupPath resolvedFileGroupPath;
    private String resolvedPath;

    public ResolvedFileGroupPath getResolvedFileGroupPath() {
        return resolvedFileGroupPath;
    }

    public ResolvedFilePath setResolvedFileGroupPath(ResolvedFileGroupPath resolvedFileGroupPath) {
        this.resolvedFileGroupPath = resolvedFileGroupPath;
        return this;
    }

    public String getResolvedPath() {
        return resolvedPath;
    }

    public ResolvedFilePath setResolvedPath(String resolvedPath) {
        this.resolvedPath = resolvedPath;
        return this;
    }
}
