package com.quasarbyte.llm.codereview.sdk.service.db.core.pojo;

import com.fasterxml.jackson.core.type.TypeReference;

public interface DbPojoJsonConvertor {
    <T> String convertToString(T pojo);
    <T> T convertToPojo(String pojo, Class<T> clazz);
    <T> T convertToPojo(String pojo, TypeReference<T> typeReference);
}
