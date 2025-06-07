package com.quasarbyte.llm.codereview.sdk.model.resolved;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;

import java.util.List;

public class ResolvedFilesRules {
    private final List<ResolvedFilePath> resolvedFilePaths;
    private final List<Rule> rules;

    public ResolvedFilesRules(List<ResolvedFilePath> resolvedFilePaths, List<Rule> rules) {
        this.resolvedFilePaths = resolvedFilePaths;
        this.rules = rules;
    }

    public List<ResolvedFilePath> getResolvedFilePaths() {
        return resolvedFilePaths;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
