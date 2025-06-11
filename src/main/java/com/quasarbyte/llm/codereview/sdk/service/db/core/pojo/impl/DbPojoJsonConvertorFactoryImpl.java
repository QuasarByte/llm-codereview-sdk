package com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbPojoJsonConvertorFactoryImpl implements DbPojoJsonConvertorFactory {

    private static final Logger logger = LoggerFactory.getLogger(DbPojoJsonConvertorFactoryImpl.class);

    private final ObjectMapper objectMapper;

    public DbPojoJsonConvertorFactoryImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public DbPojoJsonConvertor create() {
        logger.debug("Instantiating DbPojoJsonConvertor");
        return new DbPojoJsonConvertorImpl(objectMapper);
    }
}
