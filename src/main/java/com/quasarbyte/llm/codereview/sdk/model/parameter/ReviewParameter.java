package com.quasarbyte.llm.codereview.sdk.model.parameter;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;

import java.time.Duration;
import java.util.List;

public class ReviewParameter {
    private String reviewName;
    private List<Rule> rules;
    private List<ReviewTarget> targets;
    private List<String> systemPrompts;
    private List<String> reviewPrompts;
    private LlmChatCompletionConfiguration llmChatCompletionConfiguration;
    private LlmMessagesMapperConfiguration llmMessagesMapperConfiguration;
    private Integer rulesBatchSize;
    private Duration timeoutDuration;
    private LlmQuota llmQuota;

    public String getReviewName() {
        return reviewName;
    }

    public ReviewParameter setReviewName(String reviewName) {
        this.reviewName = reviewName;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public ReviewParameter setRules(List<Rule> rules) {
        this.rules = rules;
        return this;
    }

    public List<ReviewTarget> getTargets() {
        return targets;
    }

    public ReviewParameter setTargets(List<ReviewTarget> targets) {
        this.targets = targets;
        return this;
    }

    public List<String> getSystemPrompts() {
        return systemPrompts;
    }

    public ReviewParameter setSystemPrompts(List<String> systemPrompts) {
        this.systemPrompts = systemPrompts;
        return this;
    }

    public List<String> getReviewPrompts() {
        return reviewPrompts;
    }

    public ReviewParameter setReviewPrompts(List<String> reviewPrompts) {
        this.reviewPrompts = reviewPrompts;
        return this;
    }

    public LlmChatCompletionConfiguration getLlmChatCompletionConfiguration() {
        return llmChatCompletionConfiguration;
    }

    public ReviewParameter setLlmChatCompletionConfiguration(LlmChatCompletionConfiguration llmChatCompletionConfiguration) {
        this.llmChatCompletionConfiguration = llmChatCompletionConfiguration;
        return this;
    }

    public LlmMessagesMapperConfiguration getLlmMessagesMapperConfiguration() {
        return llmMessagesMapperConfiguration;
    }

    public ReviewParameter setLlmMessagesMapperConfiguration(LlmMessagesMapperConfiguration llmMessagesMapperConfiguration) {
        this.llmMessagesMapperConfiguration = llmMessagesMapperConfiguration;
        return this;
    }

    public Integer getRulesBatchSize() {
        return rulesBatchSize;
    }

    public ReviewParameter setRulesBatchSize(Integer rulesBatchSize) {
        this.rulesBatchSize = rulesBatchSize;
        return this;
    }

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public ReviewParameter setTimeoutDuration(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public LlmQuota getLlmQuota() {
        return llmQuota;
    }

    public ReviewParameter setLlmQuota(LlmQuota llmQuota) {
        this.llmQuota = llmQuota;
        return this;
    }
}
