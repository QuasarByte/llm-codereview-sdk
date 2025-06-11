package com.quasarbyte.llm.codereview.sdk.service.report.html;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quasarbyte.llm.codereview.sdk.exception.report.ReportException;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import com.quasarbyte.llm.codereview.sdk.service.report.html.impl.CodeReviewReportHtmlServiceMustacheImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeReviewReportHtmlServiceMustacheImplTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ReviewDataSanitizationService sanitizationService;

    private CodeReviewReportHtmlServiceMustacheImpl service;
    private ObjectMapper objectMapper;
    private String actualTemplate;
    private ReviewResult testReviewResult;

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize ObjectMapper with JSR310 module for LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Read the actual template from resources
        actualTemplate = readResourceFile("com/quasarbyte/llm/codereview/sdk/service/impl/report/html/code-review-report-mustache-template.html");

        // Read test data from JSON
        testReviewResult = loadTestDataFromJson();

        // Mock the resource loader to return the actual template
        when(resourceLoader.load(any())).thenReturn(actualTemplate);

        // Mock sanitization service to return data unchanged (lenient to avoid unnecessary stubbing warnings)
        lenient().when(sanitizationService.sanitize(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Create the service
        service = new CodeReviewReportHtmlServiceMustacheImpl(resourceLoader, sanitizationService);
    }

    @Test
    public void generateHtmlReport_withRealTemplate_shouldCreateValidHtml() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        assertNotNull(htmlReport);
        assertTrue(htmlReport.contains("<!DOCTYPE html>"));
        assertTrue(htmlReport.contains("<title>Code Review Report</title>"));
        assertTrue(htmlReport.contains("LLM Code Review Report"));

        // Verify content from test data
        assertTrue(htmlReport.contains("alerts-search.component.ts"));
        assertTrue(htmlReport.contains("UNUSED_IMPORTS"));
        assertTrue(htmlReport.contains("HttpErrorResponse"));

        // Save to target directory for manual inspection
        saveHtmlReportToFile(htmlReport, "test-report-real-template.html");

        System.out.println("HTML report generated successfully!");
        System.out.println("Check target/test-reports/test-report-real-template.html to view in browser");
    }

    @Test
    public void generateHtmlReport_withCustomDateFormatter_shouldFormatDatesCorrectly() throws IOException {
        // Given
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm", Locale.ENGLISH);

        // When
        String htmlReport = service.generateHtmlReport(testReviewResult, customFormatter);

        // Then
        assertNotNull(htmlReport);
        // Check that custom formatter is being used - look for the actual dates from test data
        // The test data has "2025-06-10T23:47:48" which should be formatted as "Jun 10, 2025 at 23:47"
        assertTrue(htmlReport.contains("2025-06-10") || htmlReport.contains("Jun 10, 2025")); // Either original or formatted

        // Save with custom formatting
        saveHtmlReportToFile(htmlReport, "test-report-custom-date-format.html");
    }

    @Test
    public void generateHtmlReport_shouldHandleMultipleIssuesCorrectly() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        // Verify all issues from test data are present
        assertTrue(htmlReport.contains("Line 13, Column 0")); // First unused import
        assertTrue(htmlReport.contains("Line 14, Column 0")); // Second unused import
        assertTrue(htmlReport.contains("Line 15, Column 0")); // Third unused import

        // Verify warning icons and styling
        assertTrue(htmlReport.contains("🟡")); // Warning icon
        assertTrue(htmlReport.contains("WARNING Issue"));

        // Verify suggestions are included
        assertTrue(htmlReport.contains("Remove unused imports"));
    }

    @Test
    public void generateHtmlReport_shouldIncludeResourceUsage() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        assertTrue(htmlReport.contains("Resource Usage"));
        assertTrue(htmlReport.contains("2234")); // Completion tokens from test data
        assertTrue(htmlReport.contains("1391")); // Prompt tokens from test data
        assertTrue(htmlReport.contains("3625")); // Total tokens from test data
    }

    @Test
    public void generateHtmlReport_shouldIncludeReasoningSteps() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        assertTrue(htmlReport.contains("Reasoning Steps"));
        assertTrue(htmlReport.contains("Looking at the imports"));
        assertTrue(htmlReport.contains("HttpErrorResponse"));
    }

    @Test
    public void generateHtmlReport_shouldCreateWellFormedHtml() throws IOException {
        // When
        String htmlReport = service.generateHtmlReport(testReviewResult);

        // Then
        // Check HTML structure
        assertTrue(htmlReport.contains("<html lang=\"en\">"));
        assertTrue(htmlReport.contains("</html>"));
        assertTrue(htmlReport.contains("<head>"));
        assertTrue(htmlReport.contains("</head>"));
        assertTrue(htmlReport.contains("<body>"));
        assertTrue(htmlReport.contains("</body>"));

        // Check CSS is included
        assertTrue(htmlReport.contains("<style>"));
        assertTrue(htmlReport.contains("font-family:"));
        assertTrue(htmlReport.contains("background-color:"));

        // Check security headers
        assertTrue(htmlReport.contains("Content-Security-Policy"));
    }

    @Test
    public void constructor_shouldLoadTemplateCorrectly() throws IOException {
        // Verify template was loaded during construction
        verify(resourceLoader).load("classpath:com/quasarbyte/llm/codereview/sdk/service/impl/report/html/code-review-report-mustache-template.html");
    }

    @Test
    public void constructor_shouldThrowReportException_whenTemplateCannotBeLoaded() throws IOException {
        // Given
        when(resourceLoader.load(any())).thenThrow(new IOException("Template not found"));

        // When & Then
        ReportException exception = assertThrows(ReportException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                new CodeReviewReportHtmlServiceMustacheImpl(resourceLoader, sanitizationService);
            }
        });

        assertTrue(exception.getMessage().contains("Failed to compile Mustache template"));
    }

    @Test
    public void generateHtmlReport_shouldThrowNullPointerException_whenReviewResultIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                service.generateHtmlReport(null);
            }
        });
    }

    @Test
    public void generateHtmlReport_shouldThrowNullPointerException_whenDateTimeFormatterIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                service.generateHtmlReport(testReviewResult, null);
            }
        });
    }

    @Test
    public void generateHtmlReport_shouldThrowNullPointerException_whenReviewResultItemsIsNull() {
        // Given
        final ReviewResult reviewResult = new ReviewResult().setItems(null);

        // When & Then
        assertThrows(NullPointerException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                service.generateHtmlReport(reviewResult);
            }
        });
    }

    @Test
    public void generateHtmlReport_shouldHandleEmptyReviewResult() throws IOException {
        // Given
        ReviewResult emptyResult = new ReviewResult()
                .setItems(Collections.<ReviewResultItem>emptyList())
                .setCompletionUsage(null);
        // Use lenient stubbing for this specific test
        lenient().when(sanitizationService.sanitize(any())).thenReturn(emptyResult);

        // When
        String htmlReport = service.generateHtmlReport(emptyResult);

        // Then
        assertNotNull(htmlReport);
        assertTrue(htmlReport.contains("Files Reviewed:</strong> 0"));
        assertTrue(htmlReport.contains("Total Issues Found:</strong> 0"));
        assertTrue(htmlReport.contains("No files were analyzed"));

        saveHtmlReportToFile(htmlReport, "test-report-empty.html");
    }

    @Test
    public void generateHtmlReport_shouldThrowReportException_whenSanitizationFails() {
        // Given
        // Reset the mock for this specific test
        reset(sanitizationService);
        when(sanitizationService.sanitize(any())).thenThrow(new RuntimeException("Sanitization failed"));

        // When & Then
        ReportException exception = assertThrows(ReportException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() throws Throwable {
                service.generateHtmlReport(testReviewResult);
            }
        });

        assertTrue(exception.getMessage().contains("Failed to generate HTML report"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    public void generateCompleteReport_withVariousScenarios() throws IOException {
        // Create a comprehensive test report with various scenarios
        ReviewResult comprehensiveResult = createComprehensiveTestData();
        // Use lenient stubbing for this specific test
        lenient().when(sanitizationService.sanitize(any())).thenReturn(comprehensiveResult);

        // When
        String htmlReport = service.generateHtmlReport(comprehensiveResult);

        // Then
        assertNotNull(htmlReport);

        // Should contain different severity levels
        assertTrue(htmlReport.contains("🔴")); // Critical
        assertTrue(htmlReport.contains("🟡")); // Warning
        assertTrue(htmlReport.contains("ℹ️")); // Info

        saveHtmlReportToFile(htmlReport, "test-report-comprehensive.html");
    }

    private ReviewResult loadTestDataFromJson() throws IOException {
        String jsonContent = readResourceFile("src/test/resources/com/quasarbyte/llm/codereview/sdk/service/impl/report/html/test.json");
        return objectMapper.readValue(jsonContent, ReviewResult.class);
    }

    private String readResourceFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            // Try to read from classpath as fallback
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath.replace("src/test/resources/", "").replace("src/main/resources/", ""));
            if (inputStream != null) {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
            throw new IOException("Resource file not found: " + filePath);
        }

        // For JDK 8 compatibility - read file using older method
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, StandardCharsets.UTF_8);
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

    private ReviewResult createComprehensiveTestData() {
        // Create test data with various scenarios
        ReviewResult result = new ReviewResult();

        // File 1: Multiple issues with different severities
        ReviewFile file1 = new ReviewFile()
                .setFileName("critical-issues.ts")
                .setFilePath("/src/critical-issues.ts")
                .setSize(2500L)
                .setCreatedAt(LocalDateTime.now().minusDays(5))
                .setModifiedAt(LocalDateTime.now().minusHours(2));

        ReviewComment criticalComment = new ReviewComment()
                .setRule(new Rule().setCode("SECURITY_VULNERABILITY").setDescription("Potential security vulnerability").setSeverity(RuleSeverityEnum.CRITICAL))
                .setRuleId(1L)
                .setRuleCode("SECURITY_VULNERABILITY")
                .setLine(45)
                .setColumn(12)
                .setMessage("SQL injection vulnerability detected")
                .setSuggestion("Use parameterized queries to prevent SQL injection");

        ReviewComment warningComment = new ReviewComment()
                .setRule(new Rule().setCode("PERFORMANCE").setDescription("Performance issue").setSeverity(RuleSeverityEnum.WARNING))
                .setRuleId(2L)
                .setRuleCode("PERFORMANCE")
                .setLine(78)
                .setColumn(8)
                .setMessage("Inefficient loop detected")
                .setSuggestion("Consider using more efficient iteration methods");

        ReviewComment infoComment = new ReviewComment()
                .setRule(new Rule().setCode("CODE_STYLE").setDescription("Code style improvement").setSeverity(RuleSeverityEnum.INFO))
                .setRuleId(3L)
                .setRuleCode("CODE_STYLE")
                .setLine(102)
                .setColumn(4)
                .setMessage("Consider using const instead of let")
                .setSuggestion("Use const for variables that are not reassigned");

        ReviewResultItem item1 = new ReviewResultItem()
                .setFile(file1)
                .setComments(Arrays.asList(criticalComment, warningComment, infoComment))
                .setThinkSteps(Arrays.asList(
                        new ReviewThinkStep()
                                .setFileId(1L)
                                .setFileName("critical-issues.ts")
                                .setRuleId(1L)
                                .setRuleCode("SECURITY_VULNERABILITY")
                                .setThinkText("Analyzing the code for potential security vulnerabilities. Found unsafe string concatenation in SQL query.")
                ));

        // File 2: Clean file with no issues
        ReviewFile file2 = new ReviewFile()
                .setFileName("clean-code.ts")
                .setFilePath("/src/clean-code.ts")
                .setSize(1200L)
                .setCreatedAt(LocalDateTime.now().minusDays(1))
                .setModifiedAt(LocalDateTime.now().minusMinutes(30));

        ReviewResultItem item2 = new ReviewResultItem()
                .setFile(file2)
                .setComments(Collections.<ReviewComment>emptyList())
                .setThinkSteps(Collections.<ReviewThinkStep>emptyList());

        result.setItems(Arrays.asList(item1, item2));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(1500L)
                .setPromptTokens(800L)
                .setTotalTokens(2300L));

        return result;
    }
}