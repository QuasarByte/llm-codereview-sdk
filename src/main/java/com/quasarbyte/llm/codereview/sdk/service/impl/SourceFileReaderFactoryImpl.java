package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.SourceFileReader;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceFileReaderFactoryImpl implements SourceFileReaderFactory {

    private static final Logger logger = LoggerFactory.getLogger(SourceFileReaderFactoryImpl.class);

    @Override
    public SourceFileReader create() {
        logger.debug("Instantiating SourceFileReader");
        return new SourceFileReaderImpl();
    }

}
