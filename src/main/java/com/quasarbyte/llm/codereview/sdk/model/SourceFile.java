package com.quasarbyte.llm.codereview.sdk.model;

import java.time.LocalDateTime;

public class SourceFile {
    private String fileName;
    private String fileNameExtension;
    private String filePath;
    private byte[] content;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime accessedAt;
    private String codePage;

    public String getFileName() {
        return fileName;
    }

    public SourceFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileNameExtension() {
        return fileNameExtension;
    }

    public SourceFile setFileNameExtension(String fileNameExtension) {
        this.fileNameExtension = fileNameExtension;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public SourceFile setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public SourceFile setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public SourceFile setSize(Long size) {
        this.size = size;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SourceFile setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public SourceFile setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    public SourceFile setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
        return this;
    }

    public String getCodePage() {
        return codePage;
    }

    public SourceFile setCodePage(String codePage) {
        this.codePage = codePage;
        return this;
    }
}
