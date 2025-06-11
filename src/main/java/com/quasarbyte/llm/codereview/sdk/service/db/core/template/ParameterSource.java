package com.quasarbyte.llm.codereview.sdk.service.db.core.template;

import java.util.Map;

/**
 * Interface for providing named parameters to SQL queries.
 */
public interface ParameterSource {
    Object getValue(String paramName);
    boolean hasValue(String paramName);
    Map<String, Object> getValues();
}
