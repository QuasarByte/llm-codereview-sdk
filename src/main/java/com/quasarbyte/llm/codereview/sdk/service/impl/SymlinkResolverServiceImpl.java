package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.SymlinkResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SymlinkResolverServiceImpl implements SymlinkResolverService {

    private static final Logger logger = LoggerFactory.getLogger(SymlinkResolverServiceImpl.class);

    @Override
    public boolean isSymlinkToFile(Path symlink) {
        logger.debug("Checking if symlink '{}' points to a regular file.", symlink);
        try {
            Path realTarget = symlink.toRealPath();
            boolean isFile = Files.isRegularFile(realTarget);
            logger.debug("Symlink '{}' resolves to '{}'. isRegularFile={}", symlink, realTarget, isFile);
            return isFile;
        } catch (IOException | SecurityException e) {
            logger.warn("Failed to resolve symlink '{}' to file: {} - treating as not a file.", symlink, e.getMessage());
            logger.trace("Stack trace:", e);
            return false;
        }
    }

    @Override
    public boolean isSymlinkToDirectory(Path symlink) {
        logger.debug("Checking if symlink '{}' points to a directory.", symlink);
        try {
            Path realTarget = symlink.toRealPath();
            boolean isDirectory = Files.isDirectory(realTarget);
            logger.debug("Symlink '{}' resolves to '{}'. isDirectory={}", symlink, realTarget, isDirectory);
            return isDirectory;
        } catch (IOException | SecurityException e) {
            logger.warn("Failed to resolve symlink '{}' to directory: {} - treating as not a directory.", symlink, e.getMessage());
            logger.trace("Stack trace:", e);
            return false;
        }
    }
}