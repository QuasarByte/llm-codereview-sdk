package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedReviewTarget;

import java.util.List;

public interface ResolvedFilePathSplitter {
    List<List<ResolvedFilePath>> split(List<ResolvedReviewTarget> targets);
}
