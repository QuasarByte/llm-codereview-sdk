package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.TargetResolutionException;
import com.quasarbyte.llm.codereview.sdk.service.GlobPatternMatcherService;
import com.quasarbyte.llm.codereview.sdk.service.SymlinkResolverService;
import com.quasarbyte.llm.codereview.sdk.service.TargetResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TargetResolverServiceImpl implements TargetResolverService {

    private static final Logger logger = LoggerFactory.getLogger(TargetResolverServiceImpl.class);

    private final GlobPatternMatcherService globPatternMatcherService;
    private final SymlinkResolverService symlinkResolverService;

    public TargetResolverServiceImpl(GlobPatternMatcherService globPatternMatcherService,
                                     SymlinkResolverService symlinkResolverService) {
        this.globPatternMatcherService = globPatternMatcherService;
        this.symlinkResolverService = symlinkResolverService;
        logger.debug("TargetResolverServiceImpl initialized with GlobPatternMatcherService: {}, SymlinkResolverService: {}",
                globPatternMatcherService, symlinkResolverService);
    }

    @Override
    public List<String> resolve(String pathStr) {
        logger.info("Resolving target(s) for path pattern: '{}'", pathStr);
        List<String> resolved = new ArrayList<>();
        try {
            List<Path> paths = globPatternMatcherService.match(pathStr);
            logger.debug("Glob pattern '{}' matched {} path(s).", pathStr, paths.size());

            for (Path path : paths) {
                boolean isRegular = Files.isRegularFile(path);
                boolean isSymlinkToFile = Files.isSymbolicLink(path) && symlinkResolverService.isSymlinkToFile(path);

                logger.trace("Path '{}' isRegularFile: {}, isSymlinkToFile: {}", path, isRegular, isSymlinkToFile);

                if (isRegular || isSymlinkToFile) {
                    String absPath = path.toAbsolutePath().normalize().toString();
                    resolved.add(absPath);
                    logger.debug("Resolved file path: '{}'", absPath);
                } else {
                    logger.debug("Skipping non-file and non-symlinked-file path: '{}'", path);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to resolve glob pattern '{}': {}", pathStr, e.getMessage(), e);
            throw new TargetResolutionException("Failed to resolve glob pattern: " + pathStr, e);
        }

        logger.info("Resolved {} file(s) for pattern '{}'.", resolved.size(), pathStr);
        return resolved;
    }
}
