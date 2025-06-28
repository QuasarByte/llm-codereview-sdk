package com.quasarbyte.llm.codereview.sdk.service.report.html;

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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeReviewReportHtmlServiceExcludeCleanFilesTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ReviewDataSanitizationService sanitizationService;

    private CodeReviewReportHtmlServiceMustacheImpl service;
    private String actualTemplate;

    @BeforeEach
    public void setUp() throws IOException {
        // Read the actual template from resources
        actualTemplate = readResourceFile("com/quasarbyte/llm/codereview/sdk/service/impl/report/html/code-review-report-mustache-template.html");

        // Mock the resource loader to return the actual template
        when(resourceLoader.load(any())).thenReturn(actualTemplate);

        // Mock sanitization service to return data unchanged
        when(sanitizationService.sanitize(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Create the service
        service = new CodeReviewReportHtmlServiceMustacheImpl(resourceLoader, sanitizationService);
    }

    @Test
    public void generateHtmlReport_shouldExcludeFilesWithoutIssues() throws IOException {
        // Given: A review result with mixed files - some with issues, some without
        ReviewResult reviewResult = createMixedReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then
        assertNotNull(htmlReport);
        
        // Should contain files with issues
        assertTrue(htmlReport.contains("file-with-issues.ts"), "Should include files with issues");
        assertTrue(htmlReport.contains("SECURITY_VULNERABILITY"), "Should include issues from files with issues");
        
        // Should NOT contain files without issues  
        assertFalse(htmlReport.contains("clean-file.ts"), "Should exclude files without issues");
        assertFalse(htmlReport.contains("another-clean-file.ts"), "Should exclude files without issues");
        
        // Should report correct counts - all files analyzed vs only files with issues in detailed view
        assertTrue(htmlReport.contains("Files Reviewed:</strong> 3"), "Should count all files that were analyzed");
        assertTrue(htmlReport.contains("Total Issues Found:</strong> 2"), "Should count only issues from files with issues");
        
        System.out.println("=== EXCLUDE CLEAN FILES TEST ===");
        System.out.println("✓ Files with issues are included in the report");
        System.out.println("✓ Files without issues are excluded from the report");
        System.out.println("✓ File count shows all analyzed files, detailed view shows only files with issues");
        System.out.println("✓ Issue counts are correct");
    }

    private ReviewResult createMixedReviewResult() {
        ReviewResult result = new ReviewResult();

        // File 1: Has issues (should be included)
        ReviewFile fileWithIssues = new ReviewFile()
                .setFileName("file-with-issues.ts")
                .setFilePath("/src/file-with-issues.ts")
                .setSize(1500L)
                .setCreatedAt(LocalDateTime.now().minusDays(2))
                .setModifiedAt(LocalDateTime.now().minusHours(1));

        ReviewComment criticalComment = new ReviewComment()
                .setRule(new Rule().setCode("SECURITY_VULNERABILITY").setDescription("Security issue").setSeverity(RuleSeverityEnum.CRITICAL))
                .setRuleId(1L)
                .setRuleCode("SECURITY_VULNERABILITY")
                .setLine(25)
                .setColumn(10)
                .setMessage("SQL injection vulnerability detected")
                .setSuggestion("Use parameterized queries");

        ReviewComment warningComment = new ReviewComment()
                .setRule(new Rule().setCode("CODE_STYLE").setDescription("Style issue").setSeverity(RuleSeverityEnum.WARNING))
                .setRuleId(2L)
                .setRuleCode("CODE_STYLE")
                .setLine(45)
                .setColumn(5)
                .setMessage("Missing semicolon")
                .setSuggestion("Add semicolon at end of statement");

        ReviewResultItem itemWithIssues = new ReviewResultItem()
                .setFile(fileWithIssues)
                .setComments(Arrays.asList(criticalComment, warningComment))
                .setThinkSteps(Collections.emptyList());

        // File 2: Clean file without issues (should be excluded)
        ReviewFile cleanFile1 = new ReviewFile()
                .setFileName("clean-file.ts")
                .setFilePath("/src/clean-file.ts")
                .setSize(800L)
                .setCreatedAt(LocalDateTime.now().minusDays(1))
                .setModifiedAt(LocalDateTime.now().minusMinutes(30));

        ReviewResultItem cleanItem1 = new ReviewResultItem()
                .setFile(cleanFile1)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        // File 3: Another clean file without issues (should be excluded)
        ReviewFile cleanFile2 = new ReviewFile()
                .setFileName("another-clean-file.ts")
                .setFilePath("/src/another-clean-file.ts")
                .setSize(1200L)
                .setCreatedAt(LocalDateTime.now().minusHours(5))
                .setModifiedAt(LocalDateTime.now().minusMinutes(15));

        ReviewResultItem cleanItem2 = new ReviewResultItem()
                .setFile(cleanFile2)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        result.setItems(Arrays.asList(itemWithIssues, cleanItem1, cleanItem2));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(1200L)
                .setPromptTokens(600L)
                .setTotalTokens(1800L));

        return result;
    }

    private String readResourceFile(String filePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException("Resource file not found: " + filePath);
        }

        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
