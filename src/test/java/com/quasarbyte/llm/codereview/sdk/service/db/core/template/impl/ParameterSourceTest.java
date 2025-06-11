package com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl;

import com.quasarbyte.llm.codereview.sdk.service.db.core.template.ParameterSource;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.util.ParameterSources;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterSourceTest {

    @Test
    void mapParameterSourceShouldWork() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Test");
        params.put("age", 25);
        params.put("active", true);

        ParameterSource source = ParameterSources.fromMap(params);

        assertEquals("Test", source.getValue("name"));
        assertEquals(25, source.getValue("age"));
        assertEquals(true, source.getValue("active"));
        assertNull(source.getValue("missing"));

        assertTrue(source.hasValue("name"));
        assertFalse(source.hasValue("missing"));

        Map<String, Object> values = source.getValues();
        assertEquals(3, values.size());
        assertEquals("Test", values.get("name"));
    }

    @Test
    void mapParameterSourceFluentAPIShouldWork() {

        Map<String, Object> params = new HashMap<>();
        params.put("active", true);
        params.put("score", 95.5);

        MapParameterSource source = ParameterSources.empty()
            .addValue("name", "Test")
            .addValue("age", 25)
            .addValues(params);

        assertEquals("Test", source.getValue("name"));
        assertEquals(25, source.getValue("age"));
        assertEquals(true, source.getValue("active"));
        assertEquals(95.5, source.getValue("score"));
    }

    @Test
    void beanParameterSourceShouldWork() {
        TestBean bean = new TestBean();
        bean.setName("Test Bean");
        bean.setAge(30);
        bean.setActive(false);

        ParameterSource source = ParameterSources.fromBean(bean);

        assertEquals("Test Bean", source.getValue("name"));
        assertEquals(30, source.getValue("age"));
        assertEquals(false, source.getValue("active"));
        assertNull(source.getValue("missing"));

        assertTrue(source.hasValue("name"));
        assertTrue(source.hasValue("age"));
        assertTrue(source.hasValue("active"));
        assertFalse(source.hasValue("missing"));
    }

    @Test
    void beanParameterSourceShouldThrowExceptionForNullBean() {
        assertThrows(NullPointerException.class, () -> ParameterSources.fromBean(null));
    }

    @Test
    void parameterSourceUtilsShouldCreateCorrectInstances() {
        ParameterSource single = ParameterSources.of("key", "value");
        assertEquals("value", single.getValue("key"));

        ParameterSource dual = ParameterSources.of("key1", "value1", "key2", "value2");
        assertEquals("value1", dual.getValue("key1"));
        assertEquals("value2", dual.getValue("key2"));

        ParameterSource triple = ParameterSources.of("key1", "value1", "key2", "value2", "key3", "value3");
        assertEquals("value1", triple.getValue("key1"));
        assertEquals("value2", triple.getValue("key2"));
        assertEquals("value3", triple.getValue("key3"));
    }

    // Test bean classes
    static class TestBean {
        private String name;
        private Integer age;
        private Boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}
