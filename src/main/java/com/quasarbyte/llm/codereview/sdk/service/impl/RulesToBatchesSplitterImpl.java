package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.service.RulesToBatchesSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RulesToBatchesSplitterImpl implements RulesToBatchesSplitter {

    private static final Logger logger = LoggerFactory.getLogger(RulesToBatchesSplitterImpl.class);

    @Override
    public List<List<Rule>> split(List<Rule> rules, Integer batchSize) {
        logger.info("Splitting rules into batches. Total rules: {}, Requested batch size: {}",
                rules == null ? 0 : rules.size(), batchSize);

        if (rules == null || rules.isEmpty()) {
            logger.warn("Rules list is null or empty, returning empty list.");
            return Collections.emptyList();
        }

        int actualBatchSize = (batchSize == null || batchSize <= 0) ? rules.size() : batchSize;
        if (batchSize == null || batchSize <= 0) {
            logger.warn("Provided batch size is null or non-positive ({}). Using rules.size() as batch size: {}", batchSize, actualBatchSize);
        }

        List<List<Rule>> batches = new ArrayList<>();
        int batchCount = 0;
        for (int i = 0; i < rules.size(); i += actualBatchSize) {
            List<Rule> batch = rules.subList(i, Math.min(i + actualBatchSize, rules.size()));
            batches.add(batch);
            logger.debug("Created batch {}: size {}", ++batchCount, batch.size());
        }

        logger.info("Total batches created: {}", batches.size());
        return batches;
    }
}
