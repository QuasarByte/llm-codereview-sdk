package com.quasarbyte.llm.codereview.sdk.service.db.core;

import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;

public interface PersistenceConfigurationContext extends AutoCloseable {
    PersistenceConfigurationContext setPersistenceConfiguration(PersistenceConfiguration pc);
    PersistenceConfiguration getPersistenceConfiguration();
}
