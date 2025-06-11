package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepository;
import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InferenceRepositoryFactoryImpl implements InferenceRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(InferenceRepositoryFactoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public InferenceRepositoryFactoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = dbPojoJsonConvertor;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public InferenceRepository create() {
        logger.debug("Instantiating InferenceRepository");
        return new InferenceRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
