package com.quasarbyte.llm.codereview.sdk.service.report.csv.impl;

import com.quasarbyte.llm.codereview.sdk.service.report.csv.CodeReviewReportCsvService;
import com.quasarbyte.llm.codereview.sdk.service.report.csv.CodeReviewReportCsvServiceFactory;

public class CodeReviewReportCsvServiceFactoryImpl implements CodeReviewReportCsvServiceFactory {
    @Override
    public CodeReviewReportCsvService create() {
        return new CodeReviewReportCsvServiceImpl();
    }
}
