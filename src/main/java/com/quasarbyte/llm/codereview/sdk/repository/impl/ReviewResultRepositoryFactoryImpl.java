package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.ReviewResultRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewResultRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewResultRepositoryFactoryImpl implements ReviewResultRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewResultRepositoryFactoryImpl.class);

    private final DbPojoJsonConvertor dbPojoJsonConvertor;
    private final JDBCTemplate jdbcTemplate;

    public ReviewResultRepositoryFactoryImpl(DbPojoJsonConvertor dbPojoJsonConvertor, JDBCTemplate jdbcTemplate) {
        this.dbPojoJsonConvertor = dbPojoJsonConvertor;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReviewResultRepository create() {
        logger.debug("Instantiating ReviewResultRepository");
        return new ReviewResultRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
