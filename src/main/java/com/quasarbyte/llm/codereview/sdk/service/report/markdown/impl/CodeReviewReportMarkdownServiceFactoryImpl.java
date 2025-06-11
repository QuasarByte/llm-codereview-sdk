package com.quasarbyte.llm.codereview.sdk.service.report.markdown.impl;

import com.quasarbyte.llm.codereview.sdk.service.report.markdown.CodeReviewReportMarkdownService;
import com.quasarbyte.llm.codereview.sdk.service.report.markdown.CodeReviewReportMarkdownServiceFactory;

public class CodeReviewReportMarkdownServiceFactoryImpl implements CodeReviewReportMarkdownServiceFactory {
    @Override
    public CodeReviewReportMarkdownService create() {
        return new CodeReviewReportMarkdownServiceImpl();
    }
}
