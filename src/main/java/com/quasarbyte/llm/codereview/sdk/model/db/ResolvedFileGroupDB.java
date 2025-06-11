package com.quasarbyte.llm.codereview.sdk.model.db;

import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;

public class ResolvedFileGroupDB {
    private Long id;
    private Long targetId;
    private Long reviewId;
    private FileGroup fileGroup;

    public Long getId() {
        return id;
    }

    public ResolvedFileGroupDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTargetId() {
        return targetId;
    }

    public ResolvedFileGroupDB setTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ResolvedFileGroupDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public FileGroup getFileGroup() {
        return fileGroup;
    }

    public ResolvedFileGroupDB setFileGroup(FileGroup fileGroup) {
        this.fileGroup = fileGroup;
        return this;
    }
}
