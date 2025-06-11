package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.RunRepository;
import com.quasarbyte.llm.codereview.sdk.repository.RunRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Factory implementation for creating RunRepository instances.
 */
public class RunRepositoryFactoryImpl implements RunRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(RunRepositoryFactoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public RunRepositoryFactoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = Objects.requireNonNull(dbPojoJsonConvertor, "dbPojoJsonConvertor must not be null");
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public RunRepository create() {
        logger.debug("Creating RunRepository instance");
        return new RunRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
