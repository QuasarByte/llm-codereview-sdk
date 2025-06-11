package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.FileRepository;
import com.quasarbyte.llm.codereview.sdk.repository.FileRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRepositoryFactoryImpl implements FileRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryFactoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public FileRepositoryFactoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FileRepository create() {
        logger.debug("Instantiating FileRepository");
        return new FileRepositoryImpl(jdbcTemplate);
    }
}
