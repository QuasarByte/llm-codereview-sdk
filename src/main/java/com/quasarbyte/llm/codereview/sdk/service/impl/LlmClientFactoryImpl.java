package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LlmClientFactoryImpl implements LlmClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientFactoryImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LlmClient create(LlmClientConfiguration config) {
        // Log config as pretty JSON if debug is enabled
        if (logger.isDebugEnabled()) {
            try {
                String jsonConfig = objectMapper.writeValueAsString(maskedLlmClientConfigurationCopy(config));
                logger.debug("Creating LlmClient with configuration:\n{}", jsonConfig);
            } catch (JsonProcessingException e) {
                logger.debug("Failed to serialize LlmClientConfiguration to JSON", e);
            }
        }

        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder();
        logger.debug("Initialized OpenAIOkHttpClient.Builder");

        // Set base URL if specified
        if (config.getBaseUrl() != null) {
            builder.baseUrl(config.getBaseUrl());
        }

        // Set API key if specified
        if (config.getApiKey() != null) {
            builder.apiKey(config.getApiKey());
        }

        // Set organization if specified
        if (config.getOrganization() != null) {
            builder.organization(config.getOrganization());
        }

        // Set project if specified
        if (config.getProject() != null) {
            builder.project(config.getProject());
        }

        // Set Azure service version if specified
        if (config.getAzureServiceVersion() != null) {
            builder.azureServiceVersion(AzureOpenAIServiceVersion.fromString(config.getAzureServiceVersion()));
        }

        // Set checkJacksonVersionCompatibility if specified
        if (config.getCheckJacksonVersionCompatibility() != null) {
            builder.checkJacksonVersionCompatibility(config.getCheckJacksonVersionCompatibility());
        }

        // Set responseValidation if specified
        if (config.getResponseValidation() != null) {
            builder.responseValidation(config.getResponseValidation());
        }

        // Set maxRetries if specified
        if (config.getMaxRetries() != null) {
            builder.maxRetries(config.getMaxRetries());
        }

        // Set proxy if specified
        if (config.getProxy() != null) {
            builder.proxy(config.getProxy());
        }

        // Set headers if specified
        if (config.getHeadersMap() != null && !config.getHeadersMap().isEmpty()) {
            builder.headers(config.getHeadersMap());
        }

        // Set query params if specified
        if (config.getQueryParamsMap() != null && !config.getQueryParamsMap().isEmpty()) {
            builder.queryParams(config.getQueryParamsMap());
        }

        // Set JsonMapper if specified
        if (config.getJsonMapper() != null) {
            builder.jsonMapper(config.getJsonMapper());
        }

        // Set timeout duration is specified
        if (config.getTimeoutDuration() != null) {
            // If only timeout duration is specified, set as request timeout
            builder.timeout(config.getTimeoutDuration());
        }

        logger.debug("Building OpenAIClient...");
        OpenAIClient openAIClient = builder.build();
        logger.debug("OpenAIClient built successfully.");

        LlmClient llmClient = new LlmClient()
                .setOpenAIClient(openAIClient);

        logger.debug("Returning new LlmClient...");

        return llmClient;
    }

    @Override
    public List<LlmClient> create(List<LlmClientConfiguration> configs) {
        Objects.requireNonNull(configs, "configs cannot be null");
        return configs.stream().map(this::create).collect(Collectors.toList());
    }

    private static LlmClientConfiguration maskedLlmClientConfigurationCopy(LlmClientConfiguration configuration) {
        LlmClientConfiguration masked = new LlmClientConfiguration();
        masked.setCheckJacksonVersionCompatibility(configuration.getCheckJacksonVersionCompatibility())
                .setResponseValidation(configuration.getResponseValidation())
                .setTimeoutDuration(configuration.getTimeoutDuration())
                .setMaxRetries(configuration.getMaxRetries())
                .setJsonMapper(configuration.getJsonMapper())
                .setHeadersMap(configuration.getHeadersMap())
                .setQueryParamsMap(configuration.getQueryParamsMap())
                .setProxy(configuration.getProxy())
                .setApiKey(mask(configuration.getApiKey()))
                .setAzureServiceVersion(configuration.getAzureServiceVersion())
                .setBaseUrl(configuration.getBaseUrl())
                .setOrganization(configuration.getOrganization())
                .setProject(configuration.getProject());
        return masked;
    }

    // Optionally mask API key or sensitive info in logs
    private static String mask(String value) {
        return value != null ? "******" : null;
    }

}
