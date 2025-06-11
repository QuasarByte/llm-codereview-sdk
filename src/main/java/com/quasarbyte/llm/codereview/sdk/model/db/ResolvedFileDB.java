package com.quasarbyte.llm.codereview.sdk.model.db;

public class ResolvedFileDB {
    private Long id;
    private Long fileId;
    private Long groupId;
    private Long targetId;
    private Long reviewId;
    private String fileName;
    private String fileNameExtension;
    private String filePath;
    private String codePage;

    public Long getId() {
        return id;
    }

    public ResolvedFileDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getFileId() {
        return fileId;
    }

    public ResolvedFileDB setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public Long getGroupId() {
        return groupId;
    }

    public ResolvedFileDB setGroupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public Long getTargetId() {
        return targetId;
    }

    public ResolvedFileDB setTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public ResolvedFileDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ResolvedFileDB setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

    public ResolvedFileDB setFileNameExtension(String fileNameExtension) {
        this.fileNameExtension = fileNameExtension;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public ResolvedFileDB setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getCodePage() {
        return codePage;
    }

    public ResolvedFileDB setCodePage(String codePage) {
        this.codePage = codePage;
        return this;
    }
}
