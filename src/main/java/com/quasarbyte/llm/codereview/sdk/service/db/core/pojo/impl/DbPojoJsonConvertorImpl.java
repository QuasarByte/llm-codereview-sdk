package com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.sdk.exception.JsonSerialisationException;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DbPojoJsonConvertorImpl implements DbPojoJsonConvertor {

    private static final Logger logger = LoggerFactory.getLogger(DbPojoJsonConvertorImpl.class);
    private final ObjectMapper objectMapper;

    public DbPojoJsonConvertorImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> String convertToString(T pojo) {
        if (pojo == null) {
            logger.debug("convertToString called with null pojo, returning null");
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(pojo);
            logger.trace("Successfully converted instance of {} to JSON string (length: {})",
                    pojo.getClass().getSimpleName(), json.length());
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize POJO of type {}: {}",
                    pojo.getClass().getSimpleName(), e.getMessage(), e);
            throw new JsonSerialisationException(
                    String.format("Failed to convert %s to JSON string", pojo.getClass().getSimpleName()), e);
        }
    }

    @Override
    public <T> T convertToPojo(String json, Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");

        if (json == null) {
            logger.debug("convertToPojo called with null JSON string, returning null");
            return null;
        }

        try {
            T result = objectMapper.readValue(json, clazz);
            logger.trace("Successfully deserialized JSON string (length: {}) to {}",
                    json.length(), clazz.getSimpleName());
            return result;
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to type {}: {}. Source: '{}'",
                    clazz.getSimpleName(), e.getMessage(),
                    json.length() > 100 ? json.substring(0, 100) + "..." : json, e);
            throw new JsonSerialisationException(
                    String.format("Failed to convert JSON string to %s", clazz.getSimpleName()), e);
        }
    }

    @Override
    public <T> T convertToPojo(String json, TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "typeReference cannot be null");

        if (json == null) {
            logger.debug("convertToPojo called with null JSON string, returning null");
            return null;
        }

        try {
            T result = objectMapper.readValue(json, typeReference);
            logger.trace("Successfully deserialized JSON string (length: {}) to {}",
                    json.length(), typeReference.getType().getTypeName());
            return result;
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to type {}: {}. Source: '{}'",
                    typeReference.getType().getTypeName(), e.getMessage(),
                    json.length() > 100 ? json.substring(0, 100) + "..." : json, e);
            throw new JsonSerialisationException(
                    String.format("Failed to convert JSON string to %s", typeReference.getType().getTypeName()), e);
        }
    }
}
