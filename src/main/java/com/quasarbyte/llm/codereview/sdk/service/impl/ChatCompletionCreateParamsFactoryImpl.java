package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.service.ChatCompletionCreateParamsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatCompletionCreateParamsFactoryImpl implements ChatCompletionCreateParamsFactory {

    private static final Logger logger = LoggerFactory.getLogger(ChatCompletionCreateParamsFactoryImpl.class);

    @Override
    public ChatCompletionCreateParams.Builder create(LlmChatCompletionConfiguration configuration) {
        logger.info("Creating ChatCompletionCreateParams.Builder from LlmChatCompletionConfiguration: {}", format(configuration));

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder();

        if (configuration.getModel() != null) {
            builder.model(configuration.getModel());
        }

        if (configuration.getFrequencyPenalty() != null) {
            builder.frequencyPenalty(configuration.getFrequencyPenalty());
        }

        if (configuration.getPresencePenalty() != null) {
            builder.presencePenalty(configuration.getPresencePenalty());
        }

        if (configuration.getLogprobs() != null) {
            builder.logprobs(configuration.getLogprobs());
        }

        if (configuration.getMaxCompletionTokens() != null) {
            builder.maxCompletionTokens(configuration.getMaxCompletionTokens());
        }

        if (configuration.getN() != null) {
            builder.n(configuration.getN().intValue());
        }

        if (configuration.getSeed() != null) {
            builder.seed(configuration.getSeed());
        }

        if (configuration.getStore() != null) {
            builder.store(configuration.getStore());
        }

        if (configuration.getTemperature() != null) {
            builder.temperature(configuration.getTemperature());
        }

        if (configuration.getTopLogprobs() != null) {
            builder.topLogprobs(configuration.getTopLogprobs().intValue());
        }

        if (configuration.getTopP() != null) {
            builder.topP(configuration.getTopP());
        }

        if (configuration.getUser() != null) {
            builder.user(configuration.getUser());
        }

        logger.info("ChatCompletionCreateParams.Builder successfully created.");
        return builder;
    }

    private static String format(LlmChatCompletionConfiguration configuration) {
        return "LlmChatCompletionConfiguration{" +
                "model='" + configuration.getModel() + '\'' +
                ", frequencyPenalty=" + configuration.getPresencePenalty() +
                ", logprobs=" + configuration.getLogprobs() +
                ", maxCompletionTokens=" + configuration.getMaxCompletionTokens() +
                ", n=" + configuration.getN() +
                ", presencePenalty=" + configuration.getPresencePenalty() +
                ", seed=" + configuration.getSeed() +
                ", store=" + configuration.getStore() +
                ", temperature=" + configuration.getTemperature() +
                ", topLogprobs=" + configuration.getTopLogprobs() +
                ", topP=" + configuration.getTopP() +
                ", user='" + configuration.getUser() + '\'' +
                '}';
    }
}
