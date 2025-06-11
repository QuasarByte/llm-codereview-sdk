package com.quasarbyte.llm.codereview.sdk.model.db;

import java.time.LocalDateTime;

public class FileDB {
    private Long id;
    private Long reviewId;
    private String fileName;
    private String fileNameExtension;
    private String filePath;
    private byte[] content;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime accessedAt;

    public Long getId() {
        return id;
    }

    public FileDB setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public FileDB setReviewId(Long reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public FileDB setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

    public FileDB setFileNameExtension(String fileNameExtension) {
        this.fileNameExtension = fileNameExtension;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public FileDB setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public FileDB setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public FileDB setSize(Long size) {
        this.size = size;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public FileDB setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public FileDB setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public FileDB setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
        return this;
    }
}
