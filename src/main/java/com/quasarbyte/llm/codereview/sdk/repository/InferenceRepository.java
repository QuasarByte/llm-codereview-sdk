package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.InferenceDB;
import com.quasarbyte.llm.codereview.sdk.model.db.InferenceStatusEnum;

import java.util.List;

public interface InferenceRepository {
    List<InferenceDB> findByReviewId(Long reviewId);

    List<InferenceDB> findByRunId(Long runId);

    Long save(InferenceDB inferenceDB);

    void updateStatus(Long id, InferenceStatusEnum status);
}
