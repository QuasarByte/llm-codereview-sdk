package com.quasarbyte.llm.codereview.sdk.service;

import java.util.List;

public interface TargetResolverService {
    /**
     * Resolves the given target path string (can be a file, directory, symlink, or glob pattern)
     * and returns a list of normalized absolute file paths matching the target.
     */
    List<String> resolve(String pathStr);
}