package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromptRepositoryFactoryImpl implements PromptRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(PromptRepositoryFactoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public PromptRepositoryFactoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = dbPojoJsonConvertor;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PromptRepository create() {
        logger.debug("Instantiating PromptRepository");
        return new PromptRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
