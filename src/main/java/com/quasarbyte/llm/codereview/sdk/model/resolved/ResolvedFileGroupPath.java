package com.quasarbyte.llm.codereview.sdk.model.resolved;

import java.util.List;

public class ResolvedFileGroupPath {
    private String path;
    private List<ResolvedFilePath> resolvedPaths;
    private ResolvedFileGroup resolvedFileGroup;

    public String getPath() {
        return path;
    }

    public ResolvedFileGroupPath setPath(String path) {
        this.path = path;
        return this;
    }

    public List<ResolvedFilePath> getResolvedPaths() {
        return resolvedPaths;
    }

    public ResolvedFileGroupPath setResolvedPaths(List<ResolvedFilePath> resolvedPaths) {
        this.resolvedPaths = resolvedPaths;
        return this;
    }

    public ResolvedFileGroup getResolvedFileGroup() {
        return resolvedFileGroup;
    }

    public ResolvedFileGroupPath setResolvedFileGroup(ResolvedFileGroup resolvedFileGroup) {
        this.resolvedFileGroup = resolvedFileGroup;
        return this;
    }
}
