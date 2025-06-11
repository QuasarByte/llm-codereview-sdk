package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.repository.RuleRepository;
import com.quasarbyte.llm.codereview.sdk.service.RuleService;
import com.quasarbyte.llm.codereview.sdk.service.RuleServiceFactory;

public class RuleServiceFactoryImpl implements RuleServiceFactory {

    private final RuleRepository ruleRepository;

    public RuleServiceFactoryImpl(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public RuleService create() {
        return new RuleServiceImpl(ruleRepository);
    }
}
