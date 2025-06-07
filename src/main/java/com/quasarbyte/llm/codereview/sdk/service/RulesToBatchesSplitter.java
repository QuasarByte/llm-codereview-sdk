package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;

import java.util.List;

public interface RulesToBatchesSplitter {
    List<List<Rule>> split(List<Rule> rules, Integer batchSize);
}
