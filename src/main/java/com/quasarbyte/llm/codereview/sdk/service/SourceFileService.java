package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;

public interface SourceFileService {
    SourceFile findByPathAndCodePage(String path, String codePage);
}
