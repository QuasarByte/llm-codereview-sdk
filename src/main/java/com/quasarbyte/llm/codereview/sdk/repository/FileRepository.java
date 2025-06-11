package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.FileDB;

import java.util.List;
import java.util.Optional;

public interface FileRepository {
    Long save(FileDB fileDB);

    Optional<FileDB> findById(Long fileId);

    Optional<FileDB> findByFilePath(String filePath);

    List<FileDB> findByReviewId(Long reviewId);

    List<Long> findFileIdsByReviewId(Long reviewId);

    boolean existsById(Long fileId);

    boolean existsByFilePath(String filePath);

    int countFilesByReviewId(Long reviewId);

    void updateById(Long fileId, FileDB fileDB);

    void deleteById(Long fileId);

    void deleteByReviewId(Long reviewId);
}
