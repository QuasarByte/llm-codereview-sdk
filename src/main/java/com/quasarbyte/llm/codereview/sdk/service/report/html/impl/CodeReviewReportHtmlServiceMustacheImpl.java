package com.quasarbyte.llm.codereview.sdk.service.report.html.impl;

import com.quasarbyte.llm.codereview.sdk.exception.CannotReadResourceException;
import com.quasarbyte.llm.codereview.sdk.exception.report.ReportException;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import com.quasarbyte.llm.codereview.sdk.service.report.html.ReviewDataSanitizationService;
import com.quasarbyte.llm.codereview.sdk.service.report.html.dto.ReportTemplateData;
import com.quasarbyte.llm.codereview.sdk.service.report.html.CodeReviewReportHtmlService;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clean HTML report service that works with pre-sanitized data
 * <p>
 * Architecture:
 * 1. ReviewDataSanitizationService sanitizes LLM content
 * 2. This service handles only presentation logic
 * 3. Clear separation of security and presentation concerns
 */
public class CodeReviewReportHtmlServiceMustacheImpl implements CodeReviewReportHtmlService {

    private static final DateTimeFormatter ISO_FULL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final String TEMPLATE_LOCATION = "classpath:com/quasarbyte/llm/codereview/sdk/service/impl/report/html/code-review-report-mustache-template.html";

    private static final Logger logger = LoggerFactory.getLogger(CodeReviewReportHtmlServiceMustacheImpl.class);

    private final Template template;
    private final ResourceLoader resourceLoader;
    private final ReviewDataSanitizationService sanitizationService;

    public CodeReviewReportHtmlServiceMustacheImpl(ResourceLoader resourceLoader,
                                                   ReviewDataSanitizationService sanitizationService) {
        this.resourceLoader = resourceLoader;
        this.sanitizationService = sanitizationService;
        this.template = compileTemplate();
    }

    @Override
    public String generateHtmlReport(ReviewResult reviewResult) {
        return generateHtmlReport(reviewResult, ISO_FULL_FORMAT);
    }

    @Override
    public String generateHtmlReport(ReviewResult reviewResult, DateTimeFormatter dateTimeFormatter) {
        Objects.requireNonNull(reviewResult, "reviewResult must not be null");
        Objects.requireNonNull(dateTimeFormatter, "dateTimeFormatter must not be null");
        Objects.requireNonNull(reviewResult.getItems(), "reviewResult.getItems() must not be null");

        try {
            logger.debug("Generating HTML report for {} files",
                    reviewResult.getItems() != null ? reviewResult.getItems().size() : 0);

            // STEP 1: Sanitize all LLM-generated content
            ReviewResult sanitizedResult = sanitizationService.sanitize(reviewResult);

            // STEP 2: Convert to template data (pure presentation logic)
            ReportTemplateData templateData = new ReportTemplateData(sanitizedResult, dateTimeFormatter);
            setFileIndices(templateData);

            // STEP 3: Generate an HTML report
            String html = template.execute(templateData);

            logger.debug("Successfully generated HTML report");
            return html;

        } catch (Exception e) {
            logger.error("Failed to generate HTML report", e);
            throw new ReportException("Failed to generate HTML report: " + e.getMessage(), e);
        }
    }

    private void setFileIndices(ReportTemplateData templateData) {
        if (templateData.getFiles() != null) {
            for (int i = 0; i < templateData.getFiles().size(); i++) {
                templateData.getFiles().get(i).setIndex(i + 1);
            }
        }
    }

    private Template compileTemplate() {
        try {
            String templateSource = loadTemplate();
            return Mustache.compiler()
                    .escapeHTML(true)       // Additional safety for template variables
                    .defaultValue("")       // Empty string for missing variables
                    .nullValue("")          // Empty string for null values
                    .compile(templateSource);
        } catch (Exception e) {
            logger.error("Failed to compile Mustache template", e);
            throw new ReportException("Failed to compile Mustache template: " + e.getMessage(), e);
        }
    }

    private String loadTemplate() {
        try {
            return resourceLoader.load(TEMPLATE_LOCATION);
        } catch (IOException e) {
            logger.error("Cannot read report template '{}', error: '{}'", TEMPLATE_LOCATION, e.getMessage(), e);
            throw new CannotReadResourceException(
                    String.format("Cannot read report template '%s', error: '%s'", TEMPLATE_LOCATION, e.getMessage()), e);
        }
    }
}