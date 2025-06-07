package com.quasarbyte.llm.codereview.sdk.model.configuration;

public class LlmChatCompletionConfiguration {
    private String model;
    private Double frequencyPenalty;
    private Boolean logprobs;
    private Long maxCompletionTokens;
    private Long n;
    private Double presencePenalty;
    private Long seed;
    private Boolean store;
    private Double temperature;
    private Long topLogprobs;
    private Double topP;
    private String user;

    public String getModel() {
        return model;
    }

    public LlmChatCompletionConfiguration setModel(String model) {
        this.model = model;
        return this;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public LlmChatCompletionConfiguration setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public Boolean getLogprobs() {
        return logprobs;
    }

    public LlmChatCompletionConfiguration setLogprobs(Boolean logprobs) {
        this.logprobs = logprobs;
        return this;
    }

    public Long getMaxCompletionTokens() {
        return maxCompletionTokens;
    }

    public LlmChatCompletionConfiguration setMaxCompletionTokens(Long maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
        return this;
    }

    public Long getN() {
        return n;
    }

    public LlmChatCompletionConfiguration setN(Long n) {
        this.n = n;
        return this;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public LlmChatCompletionConfiguration setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    public Long getSeed() {
        return seed;
    }

    public LlmChatCompletionConfiguration setSeed(Long seed) {
        this.seed = seed;
        return this;
    }

    public Boolean getStore() {
        return store;
    }

    public LlmChatCompletionConfiguration setStore(Boolean store) {
        this.store = store;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public LlmChatCompletionConfiguration setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Long getTopLogprobs() {
        return topLogprobs;
    }

    public LlmChatCompletionConfiguration setTopLogprobs(Long topLogprobs) {
        this.topLogprobs = topLogprobs;
        return this;
    }

    public Double getTopP() {
        return topP;
    }

    public LlmChatCompletionConfiguration setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    public String getUser() {
        return user;
    }

    public LlmChatCompletionConfiguration setUser(String user) {
        this.user = user;
        return this;
    }
}
