package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.GlobPatternMatcherService;
import com.quasarbyte.llm.codereview.sdk.service.SymlinkResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * GlobPatternMatcherService implementation.
 * Supports matching files for any input: absolute, relative, file, directory, or glob pattern.
 */
public class GlobPatternMatcherServiceImpl implements GlobPatternMatcherService {

    private static final Logger logger = LoggerFactory.getLogger(GlobPatternMatcherServiceImpl.class);

    private final SymlinkResolverService symlinkResolverService;

    public GlobPatternMatcherServiceImpl(SymlinkResolverService symlinkResolverService) {
        this.symlinkResolverService = symlinkResolverService;
    }

    @Override
    public List<Path> match(String userInputPattern) throws IOException {
        logger.info("Starting match with pattern: {}", userInputPattern);
        List<Path> result = new ArrayList<>();
        String unixStylePattern = userInputPattern.replace("\\", "/");

        if (!containsGlob(unixStylePattern)) {
            logger.debug("Pattern does not contain glob. Treating as file or directory path.");
            Path path = Paths.get(userInputPattern);
            if (!path.isAbsolute()) {
                path = Paths.get("").toAbsolutePath().resolve(path).normalize();
                logger.debug("Resolved to absolute path: {}", path);
            }
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    logger.info("Path is a directory. Listing files (non-recursive): {}", path);
                    // Optionally, add all files in directory (non-recursive)
                    try (Stream<Path> stream = Files.list(path)) {
                        stream.forEach(p -> {
                            logger.debug("Adding file from directory: {}", p);
                            result.add(p.toAbsolutePath().normalize());
                        });
                    } catch (IOException e) {
                        logger.error("Error listing directory: {}", path, e);
                        throw e;
                    }
                } else {
                    logger.debug("Path is a file. Adding: {}", path);
                    result.add(path);
                }
            } else {
                logger.warn("Path does not exist: {}", path);
            }
            logger.info("Finished match for non-glob pattern. Found {} entries.", result.size());
            return result;
        }

        // Use detectRootDir as required by your interface!
        Path root = detectRootDir(unixStylePattern);
        logger.debug("Detected root dir: {}", root);
        Path absoluteRoot = root.isAbsolute()
                ? root
                : Paths.get("").toAbsolutePath().resolve(root).normalize();

        String rootStr = root.toString().replace("\\", "/");
        String relativeGlob = unixStylePattern.substring(Math.min(rootStr.length(), unixStylePattern.length()));
        if (relativeGlob.startsWith("/")) relativeGlob = relativeGlob.substring(1);

        String nativeGlob = relativeGlob.replace("/", File.separator);
        logger.debug("Using glob for matching: {}", nativeGlob);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + nativeGlob);

        try (Stream<Path> stream = Files.walk(absoluteRoot)) {
            stream
                    .filter(path -> Files.isRegularFile(path)
                            || (Files.isSymbolicLink(path) && symlinkResolverService.isSymlinkToFile(path)))
                    .forEach(absPath -> {
                        Path relPath;
                        try {
                            relPath = absoluteRoot.relativize(absPath);
                        } catch (IllegalArgumentException e) {
                            logger.warn("Failed to relativize path {} to root {}: {}", absPath, absoluteRoot, e.getMessage());
                            relPath = absPath;
                        }
                        if (matcher.matches(relPath)) {
                            logger.debug("Glob matched: {}", absPath);
                            result.add(absPath.toAbsolutePath().normalize());
                        }
                    });
        } catch (IOException e) {
            logger.error("Error walking file tree starting at: {}", absoluteRoot, e);
            throw e;
        }
        logger.info("Finished match for glob pattern. Found {} entries.", result.size());
        return result;
    }

    @Override
    public Path detectRootDir(String unixStyleGlob) {
        int idx = findFirstGlobCharIndex(unixStyleGlob);
        String rootStr = (idx >= 0) ? unixStyleGlob.substring(0, idx) : unixStyleGlob;
        // Remove trailing '/' if present
        if (rootStr.endsWith("/")) rootStr = rootStr.substring(0, rootStr.length() - 1);
        if (rootStr.isEmpty()) {
            logger.debug("Root dir detected as current directory (empty rootStr)");
            return Paths.get(".");
        }
        logger.debug("detectRootDir: rootStr='{}' from glob '{}'", rootStr, unixStyleGlob);
        return Paths.get(rootStr);
    }

    // Checks if the pattern contains glob symbols
    public boolean containsGlob(String path) {
        boolean result = path.contains("*") || path.contains("?") || path.contains("[") || path.contains("{");
        logger.debug("containsGlob('{}') = {}", path, result);
        return result;
    }

    // Returns the index of the first glob symbol in the pattern string
    private int findFirstGlobCharIndex(String s) {
        int[] indexes = {
                s.indexOf('*'),
                s.indexOf('?'),
                s.indexOf('['),
                s.indexOf('{')
        };
        int min = Integer.MAX_VALUE;
        for (int idx : indexes) {
            if (idx >= 0 && idx < min) min = idx;
        }
        logger.debug("findFirstGlobCharIndex('{}') = {}", s, (min == Integer.MAX_VALUE ? -1 : min));
        return min == Integer.MAX_VALUE ? -1 : min;
    }
}
