package com.quasarbyte.llm.codereview.sdk.model.configuration;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.net.Proxy;
import java.time.Duration;
import java.util.Map;

public class LlmClientConfiguration {
    private Boolean checkJacksonVersionCompatibility;
    private Boolean responseValidation;
    private Duration timeoutDuration;
    private Integer maxRetries;
    private JsonMapper jsonMapper;
    private Map<String, Iterable<String>> headersMap;
    private Map<String, Iterable<String>> queryParamsMap;
    private Proxy proxy;
    private String apiKey;
    private String azureServiceVersion;
    private String baseUrl;
    private String organization;
    private String project;

    public Boolean getCheckJacksonVersionCompatibility() {
        return checkJacksonVersionCompatibility;
    }

    public LlmClientConfiguration setCheckJacksonVersionCompatibility(Boolean checkJacksonVersionCompatibility) {
        this.checkJacksonVersionCompatibility = checkJacksonVersionCompatibility;
        return this;
    }

    public Boolean getResponseValidation() {
        return responseValidation;
    }

    public LlmClientConfiguration setResponseValidation(Boolean responseValidation) {
        this.responseValidation = responseValidation;
        return this;
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public LlmClientConfiguration setTimeoutDuration(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public LlmClientConfiguration setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public LlmClientConfiguration setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public Map<String, Iterable<String>> getHeadersMap() {
        return headersMap;
    }

    public LlmClientConfiguration setHeadersMap(Map<String, Iterable<String>> headersMap) {
        this.headersMap = headersMap;
        return this;
    }

    public Map<String, Iterable<String>> getQueryParamsMap() {
        return queryParamsMap;
    }

    public LlmClientConfiguration setQueryParamsMap(Map<String, Iterable<String>> queryParamsMap) {
        this.queryParamsMap = queryParamsMap;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public LlmClientConfiguration setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public LlmClientConfiguration setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getAzureServiceVersion() {
        return azureServiceVersion;
    }

    public LlmClientConfiguration setAzureServiceVersion(String azureServiceVersion) {
        this.azureServiceVersion = azureServiceVersion;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public LlmClientConfiguration setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public LlmClientConfiguration setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getProject() {
        return project;
    }

    public LlmClientConfiguration setProject(String project) {
        this.project = project;
        return this;
    }
}
