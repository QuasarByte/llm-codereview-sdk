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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<String> resolve(String pathStr, List<String> excludePaths) {
        Objects.requireNonNull(pathStr, "pathStr cannot be null");
        Objects.requireNonNull(excludePaths, "excludePaths cannot be null");

        logger.info("Resolving target(s) for path pattern: '{}'", pathStr);

        Set<String> excludeAbsPaths = resolveExcludeAbsPaths(excludePaths);

        List<String> resolved = resolveIncludedPaths(pathStr, excludeAbsPaths);

        String excludePathsJoined = formatExcludePathsForLogging(excludeAbsPaths);

        logger.info("Resolved {} file(s) for pattern '{}', excluded patterns: {}.",
                resolved.size(), pathStr, excludePathsJoined);

        return resolved;
    }

    /**
     * Resolves all exclude patterns to a set of absolute, normalized paths.
     */
    private Set<String> resolveExcludeAbsPaths(List<String> excludePaths) {
        return excludePaths.stream()
                .filter(Objects::nonNull)
                .flatMap(excludePattern -> {
                    try {
                        List<Path> exPaths = globPatternMatcherService.match(excludePattern);
                        logger.debug("Exclude glob pattern '{}' matched {} path(s).", excludePattern, exPaths.size());
                        return exPaths.stream()
                                .map(exPath -> {
                                    String absEx = exPath.toAbsolutePath().normalize().toString();
                                    logger.trace("Adding to exclude set: '{}'", absEx);
                                    return absEx;
                                });
                    } catch (IOException e) {
                        logger.error("Failed to resolve glob pattern '{}': {}", excludePattern, e.getMessage(), e);
                        throw new TargetResolutionException("Failed to resolve glob pattern: " + excludePattern, e);
                    }
                })
                .collect(Collectors.toSet());
    }

    /**
     * Resolves main pattern, filters, excludes unwanted paths, sorts, and returns.
     */
    private List<String> resolveIncludedPaths(String pathStr, Set<String> excludeAbsPaths) {
        try {
            List<Path> paths = globPatternMatcherService.match(pathStr);
            logger.debug("Glob pattern '{}' matched {} path(s).", pathStr, paths.size());

            return paths.stream()
                    .filter(path -> {
                        boolean isRegular = Files.isRegularFile(path);
                        boolean isSymlinkToFile = Files.isSymbolicLink(path) && symlinkResolverService.isSymlinkToFile(path);
                        logger.trace("Path '{}' isRegularFile: {}, isSymlinkToFile: {}", path, isRegular, isSymlinkToFile);
                        return isRegular || isSymlinkToFile;
                    })
                    .map(path -> path.toAbsolutePath().normalize().toString())
                    .peek(absPath -> {
                        if (excludeAbsPaths.contains(absPath)) {
                            logger.debug("Excluded file path: '{}'", absPath);
                        } else {
                            logger.debug("Resolved file path: '{}'", absPath);
                        }
                    })
                    .filter(absPath -> !excludeAbsPaths.contains(absPath))
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Failed to resolve glob pattern '{}': {}", pathStr, e.getMessage(), e);
            throw new TargetResolutionException("Failed to resolve glob pattern: " + pathStr, e);
        }
    }

    /**
     * Formats exclude paths for use in summary logging.
     */
    private String formatExcludePathsForLogging(Set<String> excludeAbsPaths) {
        return excludeAbsPaths.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", ", "'", "'"));
    }
}
