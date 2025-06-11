package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileGroupRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileGroupRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertorFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplateFactory;

import java.util.Objects;

public class ResolvedFileGroupRepositoryFactoryImpl implements ResolvedFileGroupRepositoryFactory {

    private final DbPojoJsonConvertorFactory dbPojoJsonConvertorFactory;
    private final JDBCTemplateFactory jdbcTemplateFactory;

    public ResolvedFileGroupRepositoryFactoryImpl(DbPojoJsonConvertorFactory dbPojoJsonConvertorFactory, JDBCTemplateFactory jdbcTemplateFactory) {
        this.dbPojoJsonConvertorFactory = Objects.requireNonNull(dbPojoJsonConvertorFactory, "dbPojoJsonConvertorFactory must not be null");
        this.jdbcTemplateFactory = Objects.requireNonNull(jdbcTemplateFactory, "jdbcTemplateFactory must not be null");
    }

    @Override
    public ResolvedFileGroupRepository create() {
        DbPojoJsonConvertor dbPojoJsonConvertor = dbPojoJsonConvertorFactory.create();
        JDBCTemplate jdbcTemplate = jdbcTemplateFactory.create();
        return new ResolvedFileGroupRepositoryImpl(dbPojoJsonConvertor, jdbcTemplate);
    }
}
