package com.quasarbyte.llm.codereview.sdk.service.html.impl;

import com.quasarbyte.llm.codereview.sdk.service.html.HtmlSanitizerService;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class HtmlSanitizerServiceImpl implements HtmlSanitizerService {

    private static final CssSchema CSS_SCHEMA = CssSchema.DEFAULT;

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowStyling(CSS_SCHEMA)
            .toFactory();

    @Override
    public String sanitize(String input) {
        final String result;

        if (input == null || input.trim().isEmpty()) {
            result = input;
        } else {
            result = POLICY.sanitize(input);
        }

        return result;
    }

}
