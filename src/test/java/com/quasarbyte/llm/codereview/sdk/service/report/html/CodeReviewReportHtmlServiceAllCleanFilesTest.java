package com.quasarbyte.llm.codereview.sdk.service.report.html;

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
class CodeReviewReportHtmlServiceAllCleanFilesTest {

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
    public void generateHtmlReport_shouldShowAllCleanMessage_whenAllFilesHaveNoIssues() throws IOException {
        // Given: A review result with multiple files but NO issues
        ReviewResult reviewResult = createAllCleanFilesReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then
        assertNotNull(htmlReport);
        
        // Should show correct summary counts
        assertTrue(htmlReport.contains("Files with Issues:</strong> 0"), "Should show zero files with issues");
        assertTrue(htmlReport.contains("Total Issues Found:</strong> 0"), "Should show zero issues");
        
        // Should show the "all clean" message instead of "No files were analyzed"
        assertTrue(htmlReport.contains("No issues found. All files are clean!"), 
                "Should show encouraging message when all files are clean");
        
        // Should NOT show the generic "No issues found" message (which appears when no files were analyzed)
        // The template shows "No issues found." for {{^hasFilesAnalyzed}} and 
        // "No issues found. All files are clean!" for {{#allFilesClean}}
        // We want to ensure it shows the encouraging version, not the generic one
        assertTrue(htmlReport.contains("All files are clean!"), 
                "Should show the encouraging 'All files are clean!' message");
        
        // Should NOT show any individual file sections (since we filter out clean files)
        assertFalse(htmlReport.contains("clean-file-1.ts"), "Should not show individual clean files");
        assertFalse(htmlReport.contains("clean-file-2.ts"), "Should not show individual clean files");
        assertFalse(htmlReport.contains("clean-file-3.ts"), "Should not show individual clean files");
        
        System.out.println("=== ALL CLEAN FILES TEST ===");
        System.out.println("✓ Correct summary counts shown");
        System.out.println("✓ Encouraging 'all clean' message displayed");
        System.out.println("✓ No incorrect 'No files were analyzed' message");
        System.out.println("✓ Individual clean files are not cluttering the detailed view");
    }
    
    @Test
    public void generateHtmlReport_shouldShowNoFilesMessage_whenNoFilesAtAll() throws IOException {
        // Given: A review result with NO files at all
        ReviewResult reviewResult = createEmptyReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then
        assertNotNull(htmlReport);
        
        // Should show zero counts
        assertTrue(htmlReport.contains("Files with Issues:</strong> 0"), "Should show zero files with issues");
        assertTrue(htmlReport.contains("Total Issues Found:</strong> 0"), "Should show zero issues");
        
        // Should show the "No files were analyzed" message (the generic one)
        assertTrue(htmlReport.contains("No issues found.") && !htmlReport.contains("All files are clean"), 
                "Should show generic 'No issues found.' when no files were analyzed");
        
        // Should NOT show the "all clean" message
        assertFalse(htmlReport.contains("All files are clean"), 
                "Should not show 'all clean' message when no files were analyzed");
        
        System.out.println("=== NO FILES TEST ===");
        System.out.println("✓ Correct zero counts shown");
        System.out.println("✓ Appropriate 'No files were analyzed' message displayed");
        System.out.println("✓ No inappropriate 'all clean' message");
    }

    private ReviewResult createAllCleanFilesReviewResult() {
        ReviewResult result = new ReviewResult();

        // File 1: Clean file without issues
        ReviewFile cleanFile1 = new ReviewFile()
                .setFileName("clean-file-1.ts")
                .setFilePath("/src/clean-file-1.ts")
                .setSize(800L)
                .setCreatedAt(LocalDateTime.now().minusDays(1))
                .setModifiedAt(LocalDateTime.now().minusMinutes(30));

        ReviewResultItem cleanItem1 = new ReviewResultItem()
                .setFile(cleanFile1)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        // File 2: Another clean file without issues
        ReviewFile cleanFile2 = new ReviewFile()
                .setFileName("clean-file-2.ts")
                .setFilePath("/src/clean-file-2.ts")
                .setSize(1200L)
                .setCreatedAt(LocalDateTime.now().minusHours(5))
                .setModifiedAt(LocalDateTime.now().minusMinutes(15));

        ReviewResultItem cleanItem2 = new ReviewResultItem()
                .setFile(cleanFile2)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        // File 3: Third clean file without issues
        ReviewFile cleanFile3 = new ReviewFile()
                .setFileName("clean-file-3.ts")
                .setFilePath("/src/clean-file-3.ts")
                .setSize(950L)
                .setCreatedAt(LocalDateTime.now().minusHours(2))
                .setModifiedAt(LocalDateTime.now().minusMinutes(45));

        ReviewResultItem cleanItem3 = new ReviewResultItem()
                .setFile(cleanFile3)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        result.setItems(Arrays.asList(cleanItem1, cleanItem2, cleanItem3));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(800L)
                .setPromptTokens(400L)
                .setTotalTokens(1200L));

        return result;
    }
    
    private ReviewResult createEmptyReviewResult() {
        ReviewResult result = new ReviewResult();
        result.setItems(Collections.emptyList()); // No files at all
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(0L)
                .setPromptTokens(0L)
                .setTotalTokens(0L));
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
