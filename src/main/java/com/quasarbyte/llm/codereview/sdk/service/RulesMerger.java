package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;

import java.util.List;

public interface RulesMerger {
    List<Rule> merge(List<List<Rule>> list);
}
