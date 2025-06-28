package com.quasarbyte.llm.codereview.sdk.service.report.html;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import com.quasarbyte.llm.codereview.sdk.service.report.html.impl.CodeReviewReportHtmlServiceMustacheImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that uses real implementations instead of mocks
 * to avoid unnecessary stubbing issues.
 */
class CodeReviewReportHtmlServiceIntegrationTest {

    private CodeReviewReportHtmlServiceMustacheImpl service;
    private ObjectMapper objectMapper;
    private ReviewResult testReviewResult;

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize ObjectMapper with JSR310 module for LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Read test data from JSON
        testReviewResult = loadTestDataFromJson();

        // Create real implementations
        ResourceLoader resourceLoader = new TestResourceLoader();
        ReviewDataSanitizationService sanitizationService = new TestReviewDataSanitizationServiceImpl();

        // Create the service with real implementations
        service = new CodeReviewReportHtmlServiceMustacheImpl(resourceLoader, sanitizationService);
    }

    @Test
    public void generateHtmlReport_withRealImplementations_shouldCreateValidHtml() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        assertNotNull(htmlReport);
        assertTrue(htmlReport.contains("<!DOCTYPE html>"));
        assertTrue(htmlReport.contains("<title>Code Review Report</title>"));
        assertTrue(htmlReport.contains("LLM Code Review Report"));

        // Verify content from test data (using actual test file name)
        assertTrue(htmlReport.contains("document-search.component.ts"));
        assertTrue(htmlReport.contains("UNUSED_IMPORTS"));
        assertTrue(htmlReport.contains("HttpErrorResponse"));

        // Save to target directory for manual inspection
        saveHtmlReportToFile(htmlReport, "integration-test-report.html");

        System.out.println("Integration test HTML report generated successfully!");
        System.out.println("Check target/test-reports/integration-test-report.html to view in browser");
    }

    @Test
    public void generateHtmlReport_withCustomDateFormatter_shouldWork() throws IOException {
        // Given
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH);

        // When
        String htmlReport = service.generateHtmlReport(testReviewResult, customFormatter);

        // Then
        assertNotNull(htmlReport);
        assertTrue(htmlReport.contains("10/06/2025")); // Should contain custom formatted date

        saveHtmlReportToFile(htmlReport, "integration-test-custom-date.html");
    }

    @Test
    public void generateHtmlReport_shouldIncludeAllSections() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        // Check all main sections are present
        assertTrue(htmlReport.contains("Summary"));
        assertTrue(htmlReport.contains("Files with Issues"));
        assertTrue(htmlReport.contains("Resource Usage"));
        assertTrue(htmlReport.contains("Reasoning Steps"));

        // Check specific data from test JSON
        assertTrue(htmlReport.contains("2234")); // Completion tokens
        assertTrue(htmlReport.contains("1391")); // Prompt tokens
        assertTrue(htmlReport.contains("3625")); // Total tokens

        // Check warning styling - fix these assertions
        assertTrue(htmlReport.contains("WARNING")); // Check for WARNING severity
        assertTrue(htmlReport.contains("UNUSED_IMPORTS")); // Check for rule code
    }

    private ReviewResult loadTestDataFromJson() throws IOException {
        String jsonContent = readResourceFile("test.json");
        return objectMapper.readValue(jsonContent, ReviewResult.class);
    }

    private String readResourceFile(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "com/quasarbyte/llm/codereview/sdk/service/impl/report/html/" + fileName);

        if (inputStream == null) {
            throw new IOException("Resource file not found: " + fileName);
        }

        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void saveHtmlReportToFile(String htmlContent, String fileName) throws IOException {
        Path targetDir = Paths.get("target", "test-reports");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path reportFile = targetDir.resolve(fileName);
        Files.write(reportFile, htmlContent.getBytes(StandardCharsets.UTF_8));

        System.out.println("HTML report saved to: " + reportFile.toAbsolutePath());
    }

    /**
     * Test implementation of ResourceLoader that reads from classpath
     */
    private static class TestResourceLoader implements ResourceLoader {
        @Override
        public String load(String location) throws IOException {
            // Remove classpath: prefix if present
            String resourcePath = location.replace("classpath:", "");

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + location);
            }

            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        @Override
        public String load(String location, String codePage) throws IOException {
            // For this test, we'll ignore the codePage parameter and use UTF-8
            return load(location);
        }
    }
}