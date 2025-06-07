package com.quasarbyte.llm.codereview.sdk.model.review;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ReviewFile {
    private String fileName;
    private String filePath;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime accessedAt;

    public String getFileName() {
        return fileName;
    }

    public ReviewFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public ReviewFile setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public ReviewFile setSize(Long size) {
        this.size = size;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReviewFile setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public ReviewFile setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public ReviewFile setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
        return this;
    }
}
