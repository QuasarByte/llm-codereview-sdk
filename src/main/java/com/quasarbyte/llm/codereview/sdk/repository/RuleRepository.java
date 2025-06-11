package com.quasarbyte.llm.codereview.sdk.repository;

import com.quasarbyte.llm.codereview.sdk.model.db.RuleDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;

import java.util.List;
import java.util.Optional;

public interface RuleRepository {

    boolean existById(Long id);

    boolean existByReviewIdAndCode(Long reviewId, String code);

    Optional<RuleDB> findById(Long id);

    Optional<RuleDB> findByReviewIdAndCode(Long reviewId, String code);

    List<RuleDB> findAll();

    List<RuleDB> findAllByReviewId(Long reviewId);

    List<RuleDB> searchByReviewIdAndDescription(Long reviewId);

    List<RuleDB> searchByDescription(Long reviewId);

    List<RuleDB> searchByReviewIdAndDescriptionCI(Long reviewId);

    List<RuleDB> searchByDescriptionCI(Long reviewId);

    Long save(RuleDB ruleDB);

    void update(RuleDB ruleDB);

    void updateReviewId(Long id, Long reviewId);

    void updateCode(Long id, String code);

    void updateDescription(Long id, String description);

    void updateSeverity(Long id, RuleSeverityEnum severity);

    void deleteAll();

    void deleteById(Long id);

    void deleteByReviewId(Long reviewId);

    void deleteByReviewIdAndCode(Long reviewId, String code);
}
