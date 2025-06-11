package com.quasarbyte.llm.codereview.sdk.service;

import java.util.List;

/**
 * Service for resolving target path strings into a list of matching absolute file paths.
 * <p>
 * Excluded paths can be specified to filter out specific files or patterns from the result.
 */
public interface TargetResolverService {
    /**
     * Resolves the given target path string and returns a list of normalized, absolute file paths
     * that match the specified target, excluding any paths matching the provided exclusion list.
     *
     * @param pathStr the target path string to resolve; may be a file path or glob pattern
     * @param excludePaths list of file paths or glob patterns to be excluded from the result
     * @return list of normalized, absolute file paths matching the target and not present in the exclude list
     */
    List<String> resolve(String pathStr, List<String> excludePaths);
}