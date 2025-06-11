package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.ReviewTargetRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewTargetRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertorFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplateFactory;

import java.util.Objects;

public class ReviewTargetRepositoryFactoryImpl implements ReviewTargetRepositoryFactory {

    private final DbPojoJsonConvertorFactory dbPojoJsonConvertorFactory;
    private final JDBCTemplateFactory jdbcTemplateFactory;

    public ReviewTargetRepositoryFactoryImpl(DbPojoJsonConvertorFactory dbPojoJsonConvertorFactory, JDBCTemplateFactory jdbcTemplateFactory) {
        this.dbPojoJsonConvertorFactory = Objects.requireNonNull(dbPojoJsonConvertorFactory, "dbPojoJsonConvertorFactory must not be null");
        this.jdbcTemplateFactory = Objects.requireNonNull(jdbcTemplateFactory, "jdbcTemplateFactory must not be null");
    }

    @Override
    public ReviewTargetRepository create() {
        DbPojoJsonConvertor dbPojoJsonConvertor = dbPojoJsonConvertorFactory.create();
        JDBCTemplate jdbcTemplate = jdbcTemplateFactory.create();
        return new ReviewTargetRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
