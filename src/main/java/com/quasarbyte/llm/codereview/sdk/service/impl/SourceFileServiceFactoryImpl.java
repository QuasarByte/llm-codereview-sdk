package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.SourceFileReader;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileService;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceFileServiceFactoryImpl implements SourceFileServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(SourceFileServiceFactoryImpl.class);

    private final SourceFileReader sourceFileReader;

    public SourceFileServiceFactoryImpl(SourceFileReader sourceFileReader) {
        this.sourceFileReader = sourceFileReader;
    }

    public SourceFileService create() {
        logger.debug("Instantiating SourceFileService");
        return new SourceFileServiceImpl(sourceFileReader);
    }
}
