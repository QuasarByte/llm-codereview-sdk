package com.quasarbyte.llm.codereview.sdk.service.report.html.impl;

import com.quasarbyte.llm.codereview.sdk.service.ResourceLoader;
import com.quasarbyte.llm.codereview.sdk.service.html.HtmlSanitizerService;
import com.quasarbyte.llm.codereview.sdk.service.html.impl.HtmlSanitizerServiceImpl;
import com.quasarbyte.llm.codereview.sdk.service.impl.ResourceLoaderImpl;
import com.quasarbyte.llm.codereview.sdk.service.report.html.CodeReviewReportHtmlService;
import com.quasarbyte.llm.codereview.sdk.service.report.html.CodeReviewReportHtmlServiceFactory;
import com.quasarbyte.llm.codereview.sdk.service.report.html.ReviewDataSanitizationService;

public class CodeReviewReportHtmlServiceFactoryImpl implements CodeReviewReportHtmlServiceFactory {
    @Override
    public CodeReviewReportHtmlService create() {
        ResourceLoader resourceLoader = new ResourceLoaderImpl();
        HtmlSanitizerService htmlSanitizer = new HtmlSanitizerServiceImpl();
        ReviewDataSanitizationService sanitizationService = new ReviewDataSanitizationServiceImpl(htmlSanitizer);
        return new CodeReviewReportHtmlServiceMustacheImpl(resourceLoader, sanitizationService);
    }
}
