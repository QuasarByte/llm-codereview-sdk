package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ParameterSource implementation backed by a Map.
 */
public class MapParameterSource implements ParameterSource {
    
    private final Map<String, Object> parameters;
    
    public MapParameterSource() {
        this.parameters = new HashMap<>();
    }
    
    public MapParameterSource(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(Objects.requireNonNull(parameters, "parameters must not be null"));
    }
    
    public MapParameterSource addValue(String paramName, Object value) {
        Objects.requireNonNull(paramName, "paramName must not be null");
        this.parameters.put(paramName, value);
        return this;
    }
    
    public MapParameterSource addValues(Map<String, Object> values) {
        if (values != null) {
            this.parameters.putAll(values);
        }
        return this;
    }
    
    @Override
    public Object getValue(String paramName) {
        return parameters.get(paramName);
    }
    
    @Override
    public boolean hasValue(String paramName) {
        return parameters.containsKey(paramName);
    }
    
    @Override
    public Map<String, Object> getValues() {
        return new HashMap<>(parameters);
    }
}
