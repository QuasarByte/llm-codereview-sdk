package com.quasarbyte.llm.codereview.sdk.service.db.core.template.util;

import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl.BeanParameterSource;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl.MapParameterSource;

import java.util.Map;

/**
 * Utility class for creating ParameterSource instances.
 */
public final class ParameterSources {
    
    private ParameterSources() {
        // Utility class
    }
    
    /**
     * Create a MapParameterSource from a Map.
     */
    public static ParameterSource fromMap(Map<String, Object> parameters) {
        return new MapParameterSource(parameters);
    }
    
    /**
     * Create an empty MapParameterSource.
     */
    public static MapParameterSource empty() {
        return new MapParameterSource();
    }
    
    /**
     * Create a BeanParameterSource from a JavaBean.
     */
    public static ParameterSource fromBean(Object bean) {
        return new BeanParameterSource(bean);
    }
    
    /**
     * Create a MapParameterSource with a single parameter.
     */
    public static MapParameterSource of(String name, Object value) {
        return new MapParameterSource().addValue(name, value);
    }
    
    /**
     * Create a MapParameterSource with two parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2);
    }
    
    /**
     * Create a MapParameterSource with three parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3);
    }
    
    /**
     * Create a MapParameterSource with four parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4);
    }
    
    /**
     * Create a MapParameterSource with five parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5);
    }
    
    /**
     * Create a MapParameterSource with six parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6);
    }
    
    /**
     * Create a MapParameterSource with seven parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7);
    }
    
    /**
     * Create a MapParameterSource with eight parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8);
    }
    
    /**
     * Create a MapParameterSource with nine parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9);
    }
    
    /**
     * Create a MapParameterSource with ten parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10);
    }
    
    /**
     * Create a MapParameterSource with eleven parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11);
    }
    
    /**
     * Create a MapParameterSource with twelve parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11, String name12, Object value12) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11)
                .addValue(name12, value12);
    }
    
    /**
     * Create a MapParameterSource with thirteen parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11, String name12, Object value12, String name13, Object value13) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11)
                .addValue(name12, value12)
                .addValue(name13, value13);
    }
    
    /**
     * Create a MapParameterSource with fourteen parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11, String name12, Object value12, String name13, Object value13, String name14, Object value14) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11)
                .addValue(name12, value12)
                .addValue(name13, value13)
                .addValue(name14, value14);
    }
    
    /**
     * Create a MapParameterSource with fifteen parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11, String name12, Object value12, String name13, Object value13, String name14, Object value14, String name15, Object value15) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11)
                .addValue(name12, value12)
                .addValue(name13, value13)
                .addValue(name14, value14)
                .addValue(name15, value15);
    }
    
    /**
     * Create a MapParameterSource with sixteen parameters.
     */
    public static MapParameterSource of(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4, String name5, Object value5, String name6, Object value6, String name7, Object value7, String name8, Object value8, String name9, Object value9, String name10, Object value10, String name11, Object value11, String name12, Object value12, String name13, Object value13, String name14, Object value14, String name15, Object value15, String name16, Object value16) {
        return new MapParameterSource()
                .addValue(name1, value1)
                .addValue(name2, value2)
                .addValue(name3, value3)
                .addValue(name4, value4)
                .addValue(name5, value5)
                .addValue(name6, value6)
                .addValue(name7, value7)
                .addValue(name8, value8)
                .addValue(name9, value9)
                .addValue(name10, value10)
                .addValue(name11, value11)
                .addValue(name12, value12)
                .addValue(name13, value13)
                .addValue(name14, value14)
                .addValue(name15, value15)
                .addValue(name16, value16);
    }
    
    /**
     * Create a MapParameterSource with variable number of parameters.
     * Use this for more than 12 parameters.
     * 
     * @param namesAndValues alternating parameter names and values
     */
    public static MapParameterSource ofVarArgs(Object... namesAndValues) {
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must be provided in name-value pairs");
        }
        
        MapParameterSource source = new MapParameterSource();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (!(namesAndValues[i] instanceof String)) {
                throw new IllegalArgumentException("Parameter name at index " + i + " must be a String");
            }
            source.addValue((String) namesAndValues[i], namesAndValues[i + 1]);
        }
        return source;
    }
}
