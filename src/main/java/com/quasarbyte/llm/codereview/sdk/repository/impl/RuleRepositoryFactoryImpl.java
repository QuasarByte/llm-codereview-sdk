package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.RuleRepository;
import com.quasarbyte.llm.codereview.sdk.repository.RuleRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleRepositoryFactoryImpl implements RuleRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(RuleRepositoryFactoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public RuleRepositoryFactoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RuleRepository create() {
        logger.debug("Instantiating RuleRepository");
        return new RuleRepositoryImpl(jdbcTemplate);
    }
}
