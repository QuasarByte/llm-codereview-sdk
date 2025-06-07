package com.quasarbyte.llm.codereview.sdk.model.llm;

import java.time.LocalDateTime;

public class LlmFileMetadata {
    private String fileName;
    private String fileNameExtension;
    private String filePath;
    private Long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime accessedAt;

    public String getFileName() {
        return fileName;
    }

    public LlmFileMetadata setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

    public LlmFileMetadata setFileNameExtension(String fileNameExtension) {
        this.fileNameExtension = fileNameExtension;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public LlmFileMetadata setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LlmFileMetadata setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LlmFileMetadata setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public LlmFileMetadata setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public LlmFileMetadata setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
        return this;
    }
}
