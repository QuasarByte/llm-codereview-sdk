package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Factory implementation for creating ReviewRepository instances.
 */
public class ReviewRepositoryFactoryImpl implements ReviewRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewRepositoryFactoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public ReviewRepositoryFactoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public ReviewRepository create() {
        logger.debug("Creating ReviewRepository instance");
        return new ReviewRepositoryImpl(jdbcTemplate);
    }
}
