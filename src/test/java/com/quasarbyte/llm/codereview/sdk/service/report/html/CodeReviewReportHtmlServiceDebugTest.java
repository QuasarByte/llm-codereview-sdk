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
class CodeReviewReportHtmlServiceDebugTest {

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
    public void debugFileCountIssue_singleFile() throws IOException {
        // Given: A review result with exactly 1 file
        ReviewResult reviewResult = createSingleFileReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then - Let's see what we get
        System.out.println("=== DEBUG: SINGLE FILE TEST ===");
        System.out.println("Input items count: " + reviewResult.getItems().size());
        
        boolean contains0Files = htmlReport.contains("Files with Issues:</strong> 0");
        System.out.println("Report shows 'Files with Issues: 0': " + contains0Files);
        
        if (contains0Files) {
            System.out.println("✓ CORRECT: Single clean file shows correctly as 0 issues");
        } else {
            // Let's find what it actually says
            String[] lines = htmlReport.split("\n");
            for (String line : lines) {
                if (line.contains("Files with Issues:")) {
                    System.out.println("❌ INCORRECT: Found line: " + line.trim());
                }
            }
        }
    }

    @Test
    public void debugFileCountIssue_multipleFiles() throws IOException {
        // Given: A review result with 3 files
        ReviewResult reviewResult = createMultipleFilesReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then - Let's see what we get
        System.out.println("=== DEBUG: MULTIPLE FILES TEST ===");
        System.out.println("Input items count: " + reviewResult.getItems().size());
        
        boolean contains0Files = htmlReport.contains("Files with Issues:</strong> 0");
        System.out.println("Report shows 'Files with Issues: 0': " + contains0Files);
        
        if (contains0Files) {
            System.out.println("✓ CORRECT: Multiple clean files show correctly as 0 issues");
        } else {
            // Let's find what it actually says
            String[] lines = htmlReport.split("\n");
            for (String line : lines) {
                if (line.contains("Files with Issues:")) {
                    System.out.println("❌ INCORRECT: Found line: " + line.trim());
                }
            }
        }
    }

    @Test 
    public void debugFileCountIssue_mixedWithIssues() throws IOException {
        // Given: A review result with 3 files, 1 with issues, 2 clean
        ReviewResult reviewResult = createMixedFilesReviewResult();

        // When
        String htmlReport = service.generateHtmlReport(reviewResult);

        // Then - Let's see what we get
        System.out.println("=== DEBUG: MIXED FILES TEST ===");
        System.out.println("Input items count: " + reviewResult.getItems().size());
        
        boolean contains3Files = htmlReport.contains("Files Analyzed:</strong> 3");
        System.out.println("Report shows 'Files Analyzed: 3': " + contains3Files);
        
        if (contains3Files) {
            System.out.println("✓ CORRECT: Mixed files show correctly as 3");
        } else {
            // Let's find what it actually says  
            String[] lines = htmlReport.split("\n");
            for (String line : lines) {
                if (line.contains("Files Analyzed:")) {
                    System.out.println("❌ INCORRECT: Found line: " + line.trim());
                }
            }
        }
        
        // Also check if detailed section shows only files with issues
        boolean showsFileWithIssues = htmlReport.contains("file-with-issues.ts");
        boolean showsCleanFile = htmlReport.contains("clean-file.ts");
        
        System.out.println("Shows file with issues: " + showsFileWithIssues);
        System.out.println("Shows clean file: " + showsCleanFile);
        
        if (showsFileWithIssues && !showsCleanFile) {
            System.out.println("✓ CORRECT: Only files with issues shown in detailed view");
        } else {
            System.out.println("❌ INCORRECT: File filtering not working correctly");
        }
    }

    private ReviewResult createSingleFileReviewResult() {
        ReviewResult result = new ReviewResult();

        ReviewFile file = new ReviewFile()
                .setFileName("single-file.ts")
                .setFilePath("/src/single-file.ts")
                .setSize(800L);

        ReviewResultItem item = new ReviewResultItem()
                .setFile(file)
                .setComments(Collections.emptyList()) // No issues
                .setThinkSteps(Collections.emptyList());

        result.setItems(Arrays.asList(item));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(400L)
                .setPromptTokens(200L) 
                .setTotalTokens(600L));

        return result;
    }

    private ReviewResult createMultipleFilesReviewResult() {
        ReviewResult result = new ReviewResult();

        ReviewFile file1 = new ReviewFile()
                .setFileName("file-1.ts")
                .setFilePath("/src/file-1.ts")
                .setSize(800L);

        ReviewFile file2 = new ReviewFile()
                .setFileName("file-2.ts") 
                .setFilePath("/src/file-2.ts")
                .setSize(900L);

        ReviewFile file3 = new ReviewFile()
                .setFileName("file-3.ts")
                .setFilePath("/src/file-3.ts")
                .setSize(1000L);

        ReviewResultItem item1 = new ReviewResultItem()
                .setFile(file1)
                .setComments(Collections.emptyList())
                .setThinkSteps(Collections.emptyList());

        ReviewResultItem item2 = new ReviewResultItem()
                .setFile(file2)
                .setComments(Collections.emptyList())
                .setThinkSteps(Collections.emptyList());

        ReviewResultItem item3 = new ReviewResultItem()
                .setFile(file3)
                .setComments(Collections.emptyList())
                .setThinkSteps(Collections.emptyList());

        result.setItems(Arrays.asList(item1, item2, item3));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(800L)
                .setPromptTokens(400L)
                .setTotalTokens(1200L));

        return result;
    }

    private ReviewResult createMixedFilesReviewResult() {
        ReviewResult result = new ReviewResult();

        // File with issues
        ReviewFile fileWithIssues = new ReviewFile()
                .setFileName("file-with-issues.ts")
                .setFilePath("/src/file-with-issues.ts")
                .setSize(1000L);

        ReviewComment issue = new ReviewComment()
                .setLine(10)
                .setColumn(5)
                .setMessage("Test issue")
                .setSuggestion("Fix this");

        ReviewResultItem itemWithIssues = new ReviewResultItem()
                .setFile(fileWithIssues)
                .setComments(Arrays.asList(issue))
                .setThinkSteps(Collections.emptyList());

        // Clean files
        ReviewFile cleanFile1 = new ReviewFile()
                .setFileName("clean-file.ts")
                .setFilePath("/src/clean-file.ts")
                .setSize(800L);

        ReviewFile cleanFile2 = new ReviewFile()
                .setFileName("another-clean-file.ts")
                .setFilePath("/src/another-clean-file.ts")
                .setSize(900L);

        ReviewResultItem cleanItem1 = new ReviewResultItem()
                .setFile(cleanFile1)
                .setComments(Collections.emptyList())
                .setThinkSteps(Collections.emptyList());

        ReviewResultItem cleanItem2 = new ReviewResultItem()
                .setFile(cleanFile2)
                .setComments(Collections.emptyList())
                .setThinkSteps(Collections.emptyList());

        result.setItems(Arrays.asList(itemWithIssues, cleanItem1, cleanItem2));
        result.setCompletionUsage(new ReviewCompletionUsage()
                .setCompletionTokens(1000L)
                .setPromptTokens(500L)
                .setTotalTokens(1500L));

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
