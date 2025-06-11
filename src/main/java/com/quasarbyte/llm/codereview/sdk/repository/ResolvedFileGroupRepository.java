package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.ResolvedFileGroupDB;

import java.util.List;
import java.util.Optional;

public interface ResolvedFileGroupRepository {
    Long save(ResolvedFileGroupDB resolvedFileGroupDB);

    Optional<ResolvedFileGroupDB> findById(Long resolvedFileGroupId);

    List<ResolvedFileGroupDB> findByReviewId(Long reviewId);

    List<ResolvedFileGroupDB> findByTargetId(Long targetId);

    List<ResolvedFileGroupDB> findByReviewIdAndTargetId(Long reviewId, Long targetId);

    List<Long> findGroupIdsByReviewId(Long reviewId);

    List<Long> findGroupIdsByTargetId(Long targetId);

    boolean existsById(Long resolvedFileGroupId);

    boolean existsByReviewId(Long reviewId);

    boolean existsByReviewIdAndTargetId(Long reviewId, Long targetId);

    int countGroupsByReviewId(Long reviewId);

    int countGroupsByTargetId(Long targetId);

    void updateById(Long resolvedFileGroupId, ResolvedFileGroupDB resolvedFileGroupDB);

    void deleteById(Long resolvedFileGroupId);

    void deleteByReviewId(Long reviewId);

    void deleteByTargetId(Long targetId);
}
