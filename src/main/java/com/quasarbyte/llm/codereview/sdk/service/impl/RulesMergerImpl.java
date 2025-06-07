package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.service.RulesMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RulesMergerImpl implements RulesMerger {

    private static final Logger logger = LoggerFactory.getLogger(RulesMergerImpl.class);

    @Override
    public List<Rule> merge(List<List<Rule>> list) {
        logger.info("Merging rules from {} lists.", list == null ? 0 : list.size());
        if (list == null) {
            logger.warn("Input list of rule lists is null. Returning empty list.");
            return Collections.emptyList();
        }

        // Count total rules before filtering
        long totalRulesBefore = list.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .count();
        logger.debug("Total rules before filtering: {}", totalRulesBefore);

        Map<String, Rule> map = list.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(rule -> {
                    boolean valid = Objects.nonNull(rule.getCode()) && !rule.getCode().trim().isEmpty()
                            && Objects.nonNull(rule.getDescription()) && !rule.getDescription().trim().isEmpty();
                    if (!valid) {
                        logger.warn("Skipping invalid rule: {}", rule);
                    }
                    return valid;
                })
                .collect(Collectors.toMap(
                        Rule::getCode,
                        rule -> rule,
                        (r1, r2) -> {
                            logger.info("Duplicate rule code found: {}. Keeping the latter rule: {}", r1.getCode(), r2);
                            return r2; // if codes are the same, take the last one
                        }
                ));

        logger.info("Merged rules count: {} (after filtering and merging duplicates)", map.size());

        return new ArrayList<>(map.values());
    }
}
