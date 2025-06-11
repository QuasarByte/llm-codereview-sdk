package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.model.db.RuleDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import com.quasarbyte.llm.codereview.sdk.repository.RuleRepository;
import com.quasarbyte.llm.codereview.sdk.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple implementation of RuleService with key-based locking for scalable upsert operations.
 */
public class RuleServiceImpl implements RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);
    private static final long LOCK_TIMEOUT_SECONDS = 30;

    private final RuleRepository ruleRepository;
    
    // Key-based locking for better scalability
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public RuleServiceImpl(RuleRepository ruleRepository) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
    }

    @Override
    public Long findOrInsertRule(Long reviewId, Rule rule) {
        validateUpsertRuleInput(reviewId, rule);

        RuleDB ruleDB = new RuleDB()
                .setReviewId(reviewId)
                .setCode(rule.getCode())
                .setDescription(rule.getDescription())
                .setSeverity(rule.getSeverity());

        return findOrInsertRuleDB(ruleDB);
    }

    @Override
    public Long findOrInsertRuleDB(RuleDB ruleDB) {
        validateUpsertRuleDBInput(ruleDB);

        String lockKey = createLockKey(ruleDB.getReviewId(), ruleDB.getCode());
        ReentrantLock lock = acquireLock(lockKey);
        
        try {
            Optional<RuleDB> existingRule = ruleRepository.findByReviewIdAndCode(
                    ruleDB.getReviewId(), ruleDB.getCode());

            final Long id;

            if (existingRule.isPresent()) {
                RuleDB existing = existingRule.get();
                id = existing.getId();
                logger.debug("Found existing rule with id: {}", id);
                return id;
            } else {
                id = ruleRepository.save(ruleDB);
                logger.debug("Created new rule with id: {}", id);
                return id;
            }
        } finally {
            releaseLock(lockKey, lock);
        }
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id);
        return ruleRepository.existById(id);
    }

    @Override
    public boolean existsByReviewIdAndCode(Long reviewId, String code) {
        validateReviewIdAndCode(reviewId, code);
        return ruleRepository.existByReviewIdAndCode(reviewId, code);
    }

    @Override
    public Optional<RuleDB> findById(Long id) {
        validateId(id);
        return ruleRepository.findById(id);
    }

    @Override
    public Optional<RuleDB> findByReviewIdAndCode(Long reviewId, String code) {
        validateReviewIdAndCode(reviewId, code);
        return ruleRepository.findByReviewIdAndCode(reviewId, code);
    }

    @Override
    public List<RuleDB> findAll() {
        return ruleRepository.findAll();
    }

    @Override
    public List<RuleDB> findAllByReviewId(Long reviewId) {
        validateReviewId(reviewId);
        return ruleRepository.findAllByReviewId(reviewId);
    }

    @Override
    public List<RuleDB> searchByReviewIdAndDescription(Long reviewId) {
        validateReviewId(reviewId);
        return ruleRepository.searchByReviewIdAndDescription(reviewId);
    }

    @Override
    public List<RuleDB> searchByDescription(Long reviewId) {
        validateReviewId(reviewId);
        return ruleRepository.searchByDescription(reviewId);
    }

    @Override
    public List<RuleDB> searchByReviewIdAndDescriptionCI(Long reviewId) {
        validateReviewId(reviewId);
        return ruleRepository.searchByReviewIdAndDescriptionCI(reviewId);
    }

    @Override
    public List<RuleDB> searchByDescriptionCI(Long reviewId) {
        validateReviewId(reviewId);
        return ruleRepository.searchByDescriptionCI(reviewId);
    }

    @Override
    public Long save(RuleDB ruleDB) {
        validateRuleDB(ruleDB);
        return ruleRepository.save(ruleDB);
    }

    @Override
    public void update(RuleDB ruleDB) {
        validateUpdateInput(ruleDB);
        ruleRepository.update(ruleDB);
    }

    @Override
    public void updateReviewId(Long id, Long reviewId) {
        validateId(id);
        validateReviewId(reviewId);
        ruleRepository.updateReviewId(id, reviewId);
    }

    @Override
    public void updateCode(Long id, String code) {
        validateId(id);
        validateCode(code);
        ruleRepository.updateCode(id, code);
    }

    @Override
    public void updateDescription(Long id, String description) {
        validateId(id);
        validateDescription(description);
        ruleRepository.updateDescription(id, description);
    }

    @Override
    public void updateSeverity(Long id, RuleSeverityEnum severity) {
        validateId(id);
        validateSeverity(severity);
        ruleRepository.updateSeverity(id, severity);
    }

    @Override
    public void deleteAll() {
        ruleRepository.deleteAll();
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);
        ruleRepository.deleteById(id);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        validateReviewId(reviewId);
        ruleRepository.deleteByReviewId(reviewId);
    }

    @Override
    public void deleteByReviewIdAndCode(Long reviewId, String code) {
        validateReviewIdAndCode(reviewId, code);
        ruleRepository.deleteByReviewIdAndCode(reviewId, code);
    }

    // Validation methods

    private void validateUpsertRuleInput(Long reviewId, Rule rule) {
        if (reviewId == null) {
            throw new ValidationException("reviewId must not be null");
        }

        if (reviewId <= 0) {
            throw new ValidationException("reviewId must be positive");
        }

        Objects.requireNonNull(rule, "rule must not be null");

        if (rule.getCode() == null) {
            throw new ValidationException("rule code must not be null");
        }

        if (rule.getCode().trim().isEmpty()) {
            throw new ValidationException("rule code must not be blank");
        }

        if (rule.getSeverity() == null) {
            throw new ValidationException("rule severity must not be null");
        }
    }

    private void validateUpsertRuleDBInput(RuleDB ruleDB) {
        Objects.requireNonNull(ruleDB, "ruleDB must not be null");

        if (ruleDB.getReviewId() == null) {
            throw new ValidationException("ruleDB reviewId must not be null");
        }

        if (ruleDB.getReviewId() <= 0) {
            throw new ValidationException("ruleDB reviewId must be positive");
        }

        if (ruleDB.getCode() == null) {
            throw new ValidationException("ruleDB code must not be null");
        }

        if (ruleDB.getCode().trim().isEmpty()) {
            throw new ValidationException("ruleDB code must not be blank");
        }

        if (ruleDB.getSeverity() == null) {
            throw new ValidationException("ruleDB severity must not be null");
        }
    }

    private void validateUpdateInput(RuleDB ruleDB) {
        Objects.requireNonNull(ruleDB, "ruleDB must not be null");

        if (ruleDB.getId() == null) {
            throw new ValidationException("ruleDB id must not be null");
        }

        if (ruleDB.getId() <= 0) {
            throw new ValidationException("ruleDB id must be positive");
        }

        if (ruleDB.getReviewId() == null) {
            throw new ValidationException("ruleDB reviewId must not be null");
        }

        if (ruleDB.getReviewId() <= 0) {
            throw new ValidationException("ruleDB reviewId must be positive");
        }

        if (ruleDB.getCode() == null) {
            throw new ValidationException("ruleDB code must not be null");
        }

        if (ruleDB.getCode().trim().isEmpty()) {
            throw new ValidationException("ruleDB code must not be blank");
        }

        if (ruleDB.getSeverity() == null) {
            throw new ValidationException("ruleDB severity must not be null");
        }
    }

    private void validateRuleDB(RuleDB ruleDB) {
        validateUpsertRuleDBInput(ruleDB);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new ValidationException("Id must not be null");
        }

        if (id <= 0) {
            throw new ValidationException("id must be positive");
        }
    }

    private void validateReviewId(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("reviewId must not be null");
        }

        if (reviewId <= 0) {
            throw new ValidationException("reviewId must be positive");
        }
    }

    private void validateCode(String code) {
        if (code == null) {
            throw new ValidationException("code must not be null");
        }

        if (code.trim().isEmpty()) {
            throw new ValidationException("code must not be empty");
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new ValidationException("description must not be null");
        }
    }

    private void validateSeverity(RuleSeverityEnum severity) {
        if (severity == null) {
            throw new ValidationException("severity must not be null");
        }
    }

    private void validateReviewIdAndCode(Long reviewId, String code) {
        validateReviewId(reviewId);
        validateCode(code);
    }

    // Key-based locking methods

    private String createLockKey(Long reviewId, String code) {
        return reviewId + ":" + code;
    }

    private ReentrantLock acquireLock(String lockKey) {
        ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to acquire lock for key: " + lockKey + " within " + LOCK_TIMEOUT_SECONDS + " seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for lock: " + lockKey, e);
        }
        
        return lock;
    }

    private void releaseLock(String lockKey, ReentrantLock lock) {
        try {
            lock.unlock();
        } finally {
            // Thread-safe cleanup to prevent memory leaks
            // Use synchronized block to avoid race conditions
            synchronized (lockMap) {
                if (lock.getQueueLength() == 0 && !lock.isLocked()) {
                    lockMap.remove(lockKey, lock);
                }
            }
        }
    }
}
