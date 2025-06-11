package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.db.SupportedDatabaseDriver;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.LlmClientFactory;
import com.quasarbyte.llm.codereview.sdk.service.ReviewService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewServiceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReviewServiceImplIT {

    public static final String LLM_MODEL_QWEN_3_0_6_B = "qwen3-0.6b";
    public static final String LLM_MODEL_QWEN_3_4_B = "qwen3-4b";
    public static final String LLM_MODEL_QWEN_3_8_B = "qwen3-8b";
    public static final String LLM_MODEL_QWEN_3_14_B = "qwen3-14b";
    public static final String FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES = "mapPromptToMessages";

    private static final String OLLAMA_BASE_URL = "http://localhost:11434/v1/";
    private static final String LM_STUDIO_BASE_URL = "http://127.0.0.1:1234/v1/";

    private static MySQLContainer<?> mysql;
    private static PostgreSQLContainer<?> postgres;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void startContainer() {
        final String dbType = System.getProperty("test.db.type", "memory");

        if ("mysql".equals(dbType)) {
            mysql = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
            mysql.start();
        } else if ("postgresql".equals(dbType)) {
            postgres = new PostgreSQLContainer<>(
                    "postgres:16.2")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
            postgres.start();
        }

    }

    @AfterAll
    static void stopContainer() {
        if (mysql != null) {
            mysql.stop();
        }
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void doTestOne() throws IOException {

        final String dbType = System.getProperty("test.db.type", "memory");

        final String jdbcUrl;

        if ("mysql".equals(dbType)) {
            jdbcUrl = mysql.getJdbcUrl();
        } else if ("postgresql".equals(dbType)) {
            jdbcUrl = postgres.getJdbcUrl();
        } else {
            // Get JDBC URL based on profile configuration
            jdbcUrl = getJdbcUrl("test.db");
        }

        runTest(jdbcUrl);
        ReviewResult result = runTest(jdbcUrl);
    }

    private ReviewResult runTest(String jdbcUrl) throws IOException {
        ReviewServiceFactory reviewServiceFactory = new ReviewServiceFactoryImpl();
        ReviewService reviewService = reviewServiceFactory.create();

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
                                .setFilesBatchSize(1)
                                .setCodePage(StandardCharsets.UTF_8.name())))))
                .setLlmChatCompletionConfiguration(new LlmChatCompletionConfiguration().setModel(LLM_MODEL_QWEN_3_0_6_B))
                .setLlmMessagesMapperConfiguration(new LlmMessagesMapperConfigurationRhino()
                        .setScriptBody(new String(Files.readAllBytes(Paths.get("src/main/resources/com/quasarbyte/llm/codereview/sdk/rhino/script/rhino-llm-messages-mapper.js"))))
                        .setFunctionName(FUNCTION_NAME_MAP_PROMPT_TO_MESSAGES))
                .setTimeoutDuration(Duration.ofSeconds(60))
                .setRulesBatchSize(1)
                .setLlmQuota(
                        new LlmQuota()
                                .setRequestQuota(50L)
                                .setTokensQuota(new LlmTokensQuota()
                                        .setCompletionTokens(1000L)
                                        .setPromptTokens(2000L)
                                        .setTotalTokens(3000L))
                );

        LlmClientFactory llmClientFactory = new LlmClientFactoryImpl();

        LlmClient llmClient = llmClientFactory.create(new LlmClientConfiguration()
                .setBaseUrl(LM_STUDIO_BASE_URL)
                .setApiKey("dummy"));

        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration()
                .setDataSourceConfiguration(new DataSourceConfiguration()
                        .setDriverClassName(getDriverClassName())
                        .setJdbcUrl(jdbcUrl)
                        .setUsername(getUsername())
                        .setPassword(getPassword()));

        return reviewService.review(reviewParameter, llmClient, persistenceConfiguration);
    }

    private String getDriverClassName() {
        final String dbType = System.getProperty("test.db.type", "memory");
        if ("mysql".equals(dbType)) {
            return SupportedDatabaseDriver.MYSQL.getDriverClassName();
        } else if ("postgresql".equals(dbType)) {
            return SupportedDatabaseDriver.POSTGRESQL.getDriverClassName();
        } else {
            return SupportedDatabaseDriver.SQLITE.getDriverClassName();
        }
    }

    private String getJdbcUrl(String databaseName) {
        return new SQLightJdbcUrlValue().getJdbcUrl(tempDir, databaseName);
    }

    private String getUsername() {
        return "testuser";
    }

    private String getPassword() {
        return "testpass";
    }
}
