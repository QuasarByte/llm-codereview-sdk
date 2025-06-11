package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.ReviewTargetDB;

import java.util.List;
import java.util.Optional;

public interface ReviewTargetRepository {
    Long save(ReviewTargetDB reviewTargetDB);

    Optional<ReviewTargetDB> findById(Long resolvedReviewTargetId);

    List<ReviewTargetDB> findByReviewId(Long reviewId);

    Optional<ReviewTargetDB> findFirstByReviewId(Long reviewId);

    List<Long> findTargetIdsByReviewId(Long reviewId);

    boolean existsById(Long resolvedReviewTargetId);

    boolean existsByReviewId(Long reviewId);

    int countTargetsByReviewId(Long reviewId);

    void updateById(Long resolvedReviewTargetId, ReviewTargetDB reviewTargetDB);

    void updateResolvedReviewTargetById(Long resolvedReviewTargetId, com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget reviewTarget);

    void deleteById(Long resolvedReviewTargetId);

    void deleteByReviewId(Long reviewId);
}
