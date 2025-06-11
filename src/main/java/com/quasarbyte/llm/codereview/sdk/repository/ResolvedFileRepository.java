package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.ResolvedFileDB;

import java.util.List;
import java.util.Optional;

public interface ResolvedFileRepository {
    Long save(ResolvedFileDB resolvedFileDB);

    Optional<ResolvedFileDB> findById(Long resolvedFileId);

    List<ResolvedFileDB> findByReviewId(Long reviewId);

    List<ResolvedFileDB> findByGroupId(Long groupId);

    List<ResolvedFileDB> findByTargetId(Long targetId);

    List<ResolvedFileDB> findByFileId(Long fileId);

    Optional<ResolvedFileDB> findByFileIdAndReviewId(Long fileId, Long reviewId);

    List<Long> findFileIdsByReviewId(Long reviewId);

    List<Long> findFileIdsByGroupId(Long groupId);

    boolean existsById(Long resolvedFileId);

    boolean existsByFilePath(String resolvedFilePath);

    boolean existsByFileId(Long fileId);

    boolean existsByFileIdAndReviewId(Long fileId, Long reviewId);

    int countFilesByReviewId(Long reviewId);

    int countFilesByGroupId(Long groupId);

    void updateById(Long resolvedFileId, ResolvedFileDB resolvedFileDB);

    void deleteById(Long resolvedFileId);

    void deleteByReviewId(Long reviewId);

    void deleteByGroupId(Long groupId);

    void deleteByFileId(Long fileId);
}
