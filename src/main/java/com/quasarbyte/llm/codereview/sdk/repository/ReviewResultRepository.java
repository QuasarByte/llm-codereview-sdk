package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewResultDB;

import java.util.List;

public interface ReviewResultRepository {
    List<ReviewResultDB> findByReviewId(Long reviewId);

    List<ReviewResultDB> findByRunId(Long runId);

    Long save(ReviewResultDB reviewResultDB);

    void deleteByReviewId(Long reviewId);

    void deleteByRunId(Long runId);
}
