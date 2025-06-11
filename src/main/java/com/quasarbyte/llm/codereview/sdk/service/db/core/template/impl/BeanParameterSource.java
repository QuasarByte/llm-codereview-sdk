package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ParameterSource implementation that uses JavaBean property accessors.
 */
public class BeanParameterSource implements ParameterSource {
    
    private final Object bean;
    private final Map<String, PropertyDescriptor> propertyDescriptors;
    
    public BeanParameterSource(Object bean) {
        this.bean = Objects.requireNonNull(bean, "bean must not be null");
        this.propertyDescriptors = new HashMap<>();
        
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                    propertyDescriptors.put(pd.getName(), pd);
                }
            }
        } catch (IntrospectionException e) {
            throw new PersistenceRuntimeException("Failed to introspect bean class: " + bean.getClass().getName(), e);
        }
    }
    
    @Override
    public Object getValue(String paramName) {
        PropertyDescriptor pd = propertyDescriptors.get(paramName);
        if (pd == null) {
            return null;
        }
        
        try {
            Method readMethod = pd.getReadMethod();
            if (readMethod != null) {
                return readMethod.invoke(bean);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PersistenceRuntimeException("Failed to read property '" + paramName + "' from bean", e);
        }
        
        return null;
    }
    
    @Override
    public boolean hasValue(String paramName) {
        return propertyDescriptors.containsKey(paramName);
    }
    
    @Override
    public Map<String, Object> getValues() {
        Map<String, Object> values = new HashMap<>();
        for (String paramName : propertyDescriptors.keySet()) {
            values.put(paramName, getValue(paramName));
        }
        return values;
    }
}
