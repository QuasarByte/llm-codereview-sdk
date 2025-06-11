package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientFactory;
import com.quasarbyte.llm.codereview.sdk.service.ReviewParallelExecutionService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewParallelExecutionServiceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ReviewParallelExecutionServicePersistenceTest {

    public static final String LLM_MODEL_TEST = "test-model";
    public static final String FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES = "mapPromptToMessages";

    @TempDir
    Path tempDir;

    @Test
    void testServiceCreationAndPersistenceSetup() throws IOException {
        // Test that the service can be created with all persistence dependencies
        ReviewParallelExecutionServiceFactory reviewServiceFactory = new ReviewParallelExecutionServiceFactoryImpl();
        ReviewParallelExecutionService reviewService = reviewServiceFactory.create();

        assertNotNull(reviewService, "ReviewParallelExecutionService should be created successfully");

        // Create a minimal review parameter for testing
        ReviewParameter reviewParameter = new ReviewParameter()
                .setReviewName("persistence-test")
                .setSystemPrompts(Arrays.asList("You are a test assistant."))
                .setReviewPrompts(Arrays.asList("Test prompt"))
                .setRules(Arrays.asList(
                        new Rule()
                                .setCode("TEST001")
                                .setDescription("Test rule")
                                .setSeverity(RuleSeverityEnum.INFO)
                ))
                .setTargets(Arrays.asList(new ReviewTarget()
                        .setFileGroups(Arrays.asList(new FileGroup()
                                .setPaths(Arrays.asList("src/test/resources/com/quasarbyte/llm/codereview/sdk/examples/ExampleOne.java"))
                                .setFilesBatchSize(1)))))
                .setLlmChatCompletionConfiguration(new LlmChatCompletionConfiguration().setModel(LLM_MODEL_TEST))
                .setLlmMessagesMapperConfiguration(new LlmMessagesMapperConfigurationRhino()
                        .setScriptBody(new String(Files.readAllBytes(Paths.get("src/main/resources/com/quasarbyte/llm/codereview/sdk/rhino/script/rhino-llm-messages-mapper.js"))))
                        .setFunctionName(FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES))
                .setTimeoutDuration(Duration.ofSeconds(1)) // Very short timeout to avoid LLM calls
                .setRulesBatchSize(1);

        ParallelExecutionParameter parallelExecutionParameter = new ParallelExecutionParameter()
                .setBatchSize(1)
                .setExecutorService(Executors.newWorkStealingPool(1));

        LlmClientFactory llmClientFactory = new LlmClientFactoryImpl();
        LlmClient llmClient = llmClientFactory.create(new LlmClientConfiguration()
                .setBaseUrl("http://localhost:1")  // Non-existent URL to ensure quick failure
                .setApiKey("test"));

        // Create persistence configuration
        File dbFile = tempDir.resolve("persistence-test.db").toFile();
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration()
                .setDataSourceConfiguration(new DataSourceConfiguration()
                        .setDriverClassName("org.sqlite.JDBC")
                        .setJdbcUrl(jdbcUrl));

        // This should set up the database, create review and run records, but fail at LLM call
        // We expect an exception due to the invalid LLM URL, but it should be a connection exception, not a NullPointerException
        Exception exception = assertThrows(Exception.class, () -> {
            reviewService.review(reviewParameter, llmClient, persistenceConfiguration, parallelExecutionParameter);
        });

        // Verify it's not a NullPointerException related to missing persistence setup
        assertFalse(exception.getMessage().contains("reviewRunDetails cannot be null"), 
                "Should not fail due to missing reviewRunDetails");
        assertFalse(exception.getMessage().contains("ReviewRunDetails cannot be null"), 
                "Should not fail due to missing ReviewRunDetails");

        // Verify the database was created
        assertTrue(dbFile.exists(), "Database file should be created");
        assertTrue(dbFile.length() > 0, "Database file should not be empty");

        System.out.println("Test passed: Persistence setup works correctly, failed at LLM call as expected");
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }
}
