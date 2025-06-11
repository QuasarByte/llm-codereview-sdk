package com.quasarbyte.llm.codereview.sdk.service;

import com.quasarbyte.llm.codereview.sdk.model.run.RunFailureConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.statistics.SeverityStatistics;

public interface RunFailureChecker {
    boolean check(RunFailureConfiguration configuration, SeverityStatistics severityStatistics);
}
