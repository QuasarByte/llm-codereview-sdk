package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
