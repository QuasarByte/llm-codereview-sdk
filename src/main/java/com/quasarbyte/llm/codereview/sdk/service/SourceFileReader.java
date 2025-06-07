package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;

public interface SourceFileReader {
    SourceFile readFile(String filePath, String codePage);
}
