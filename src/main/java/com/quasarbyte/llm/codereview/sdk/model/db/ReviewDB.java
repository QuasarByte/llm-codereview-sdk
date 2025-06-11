package com.quasarbyte.llm.codereview.sdk.model.db;

import java.time.LocalDateTime;

public class ReviewDB {
    private Long id;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public ReviewDB setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReviewDB setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
