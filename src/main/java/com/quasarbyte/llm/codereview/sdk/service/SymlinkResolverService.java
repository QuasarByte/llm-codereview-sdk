package com.quasarbyte.llm.codereview.sdk.service;

import java.nio.file.Path;

public interface SymlinkResolverService {
    /**
     * Determines if a symbolic link points to a regular file.
     */
    boolean isSymlinkToFile(Path symlink);

    /**
     * Determines if a symbolic link points to a directory.
     */
    boolean isSymlinkToDirectory(Path symlink);
}
