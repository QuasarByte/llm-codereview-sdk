package com.quasarbyte.llm.codereview.sdk.service.targetresolver.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientFactory;
import com.quasarbyte.llm.codereview.sdk.service.ReviewParallelExecutionService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewParallelExecutionServiceFactory;
import com.quasarbyte.llm.codereview.sdk.service.impl.LlmClientFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.impl.ReviewParallelExecutionServiceFactoryImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class ReviewParallelExecutionServiceImplIT {

    public static final String LLM_MODEL_QWEN_3_0_6_B = "qwen3-0.6b";
    public static final String LLM_MODEL_QWEN_3_4_B = "qwen3-4b";
    public static final String LLM_MODEL_QWEN_3_8_B = "qwen3-8b";
    public static final String LLM_MODEL_QWEN_3_14_B = "qwen3-14b";
    public static final String FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES = "mapPromptToMessages";

    private static final String OLLAMA_BASE_URL = "http://localhost:11434/v1/";
    private static final String LM_STUDIO_BASE_URL = "http://127.0.0.1:1234/v1/";

    @Test
    void doTestOne() throws IOException {

        ReviewParallelExecutionServiceFactory reviewServiceFactory = new ReviewParallelExecutionServiceFactoryImpl();
        ReviewParallelExecutionService reviewService = reviewServiceFactory.create();

        ReviewParameter reviewParameter = new ReviewParameter()
                .setReviewName("test")
                .setSystemPrompts(Arrays.asList(
                                "You are code review assistant."
                        )
                )
                .setReviewPrompts(Arrays.asList(
                        "Please review all these Java files",
                        String.format("Comment style: %s", "Human like, friendly, natural, and professional tone; ideal for PRs, reports, and communication.")
                ))
                .setRules(Arrays.asList(
                                new Rule()
                                        .setCode("001")
                                        .setDescription("Find any possible unhandled exception.")
                                        .setSeverity(RuleSeverityEnum.CRITICAL),
                                new Rule()
                                        .setCode("002")
                                        .setDescription("Find possible ArrayIndexOutOfBoundsException.")
                                        .setSeverity(RuleSeverityEnum.CRITICAL)
                        )
                )
                .setTargets(Arrays.asList(new ReviewTarget()
                        .setFileGroups(Arrays.asList(new FileGroup()
                                .setPaths(Arrays.asList("src/test/resources/com/quasarbyte/llm/codereview/sdk/examples/**.{j}av[a]"))
                                .setFilesBatchSize(1)))))
                .setLlmChatCompletionConfiguration(new LlmChatCompletionConfiguration().setModel(LLM_MODEL_QWEN_3_0_6_B))
                .setLlmMessagesMapperConfiguration(new LlmMessagesMapperConfigurationRhino()
                        .setScriptBody(new String(Files.readAllBytes(Paths.get("src/main/resources/com/quasarbyte/llm/codereview/sdk/rhino/script/rhino-llm-messages-mapper.js"))))
                        .setFunctionName(FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES))
                .setTimeoutDuration(Duration.ofSeconds(60))
                .setRulesBatchSize(1);

        ParallelExecutionParameter parallelExecutionParameter = new ParallelExecutionParameter()
                .setBatchSize(1)
                .setExecutorService(Executors.newWorkStealingPool(100));

        LlmClientFactory llmClientFactory = new LlmClientFactoryImpl();

        LlmClient llmClient = llmClientFactory.create(new LlmClientConfiguration()
                .setBaseUrl(LM_STUDIO_BASE_URL)
                .setApiKey("dummy"));

        ReviewResult parallelResult = reviewService.review(reviewParameter, llmClient, parallelExecutionParameter);
    }

}
