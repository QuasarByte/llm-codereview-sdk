package com.quasarbyte.llm.codereview.sdk.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface GlobPatternMatcherService {
    /**
     * Returns true if the given path string contains a glob pattern.
     */
    boolean containsGlob(String path);

    /**
     * Determines the root directory to start searching for a given glob pattern.
     */
    Path detectRootDir(String globPattern);

    /**
     * Returns all files (and symlinks to files) matching the given glob pattern.
     */
    List<Path> match(String globPattern) throws IOException;
}