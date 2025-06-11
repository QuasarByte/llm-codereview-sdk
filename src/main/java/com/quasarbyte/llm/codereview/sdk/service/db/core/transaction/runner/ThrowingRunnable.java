package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
