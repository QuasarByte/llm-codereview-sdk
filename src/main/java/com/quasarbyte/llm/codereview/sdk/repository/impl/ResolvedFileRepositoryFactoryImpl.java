package com.quasarbyte.llm.codereview.sdk.repository.impl;

import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolvedFileRepositoryFactoryImpl implements ResolvedFileRepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFileRepositoryFactoryImpl.class);

    private final JDBCTemplate jdbcTemplate;

    public ResolvedFileRepositoryFactoryImpl(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResolvedFileRepository create() {
        logger.debug("Instantiating FileRepository");
        return new ResolvedFileRepositoryImpl(jdbcTemplate);
    }
}
