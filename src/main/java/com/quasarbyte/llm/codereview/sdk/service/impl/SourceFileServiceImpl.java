package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileReader;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceFileServiceImpl implements SourceFileService {

    private static final Logger logger = LoggerFactory.getLogger(SourceFileServiceImpl.class);

    private final SourceFileReader sourceFileReader;

    public SourceFileServiceImpl(SourceFileReader sourceFileReader) {
        this.sourceFileReader = sourceFileReader;
    }

    @Override
    public SourceFile findByPathAndCodePage(String path, String codePage) {
        logger.debug("Getting SourceFile for path: {} (codePage: {})", path, codePage);
        final SourceFile sourceFile = sourceFileReader.readFile(path, codePage);
        logger.debug("Read SourceFile for path: {}, codePage: {} successfully.", path, codePage);
        return sourceFile;
    }

}
