package com.quasarbyte.llm.codereview.sdk.service.db.core.template;

/**
 * Factory for creating JDBCTemplate instances.
 */
public interface JDBCTemplateFactory {
    JDBCTemplate create();
}
