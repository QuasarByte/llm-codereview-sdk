package com.quasarbyte.llm.codereview.sdk.model.resolved;

import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;

import java.util.List;

public class ResolvedFileGroup {
    private FileGroup fileGroup;
    private ResolvedReviewTarget resolvedReviewTarget;
    private List<ResolvedFileGroupPath> resolvedFileGroupPaths;

    public FileGroup getFileGroup() {
        return fileGroup;
    }

    public ResolvedFileGroup setFileGroup(FileGroup fileGroup) {
        this.fileGroup = fileGroup;
        return this;
    }

    public ResolvedReviewTarget getResolvedReviewTarget() {
        return resolvedReviewTarget;
    }

    public ResolvedFileGroup setResolvedReviewTarget(ResolvedReviewTarget resolvedReviewTarget) {
        this.resolvedReviewTarget = resolvedReviewTarget;
        return this;
    }

    public List<ResolvedFileGroupPath> getResolvedFileGroupPaths() {
        return resolvedFileGroupPaths;
    }

    public ResolvedFileGroup setResolvedFileGroupPaths(List<ResolvedFileGroupPath> resolvedFileGroupPaths) {
        this.resolvedFileGroupPaths = resolvedFileGroupPaths;
        return this;
    }
}
