package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.PromptDB;

import java.util.List;
import java.util.Optional;

public interface PromptRepository {
    Long save(PromptDB prompt);

    Optional<PromptDB> findById(Long promptId);

    List<PromptDB> findByReviewId(Long reviewId);

    List<Long> findPromptIdsByReviewId(Long reviewId);

    List<PromptDB> findNotFinishedPromptsByReviewId(Long reviewId);

    boolean existsById(Long promptId);

    int countPromptsByReviewId(Long reviewId);

    void updateReviewPromptById(Long promptId, com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson reviewPrompt);

    void deleteById(Long promptId);

    void deleteByReviewId(Long reviewId);
}
