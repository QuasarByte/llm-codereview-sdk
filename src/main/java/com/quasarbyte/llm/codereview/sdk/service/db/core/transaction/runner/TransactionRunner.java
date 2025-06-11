package com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner;

import java.util.function.Supplier;

/**
 * TransactionRunner provides methods to execute code in a transactional context.
 * Supports standard and checked-exception-compatible functional interfaces.
 */
public interface TransactionRunner {

    /**
     * Runs the given action within a transaction (no checked exceptions, no return value).
     *
     * @param action Runnable to execute in a transaction.
     */
    void runRunnable(Runnable action);

    /**
     * Runs the given supplier within a transaction and returns its result (no checked exceptions).
     *
     * @param supplier Supplier to execute in a transaction.
     * @param <T>      Result type.
     * @return Result returned by supplier.
     */
    <T> T runSupplier(Supplier<T> supplier);

    /**
     * Runs the given action within a transaction (allows checked exceptions, no return value).
     *
     * @param action ThrowingRunnable to execute in a transaction.
     * @throws Exception if action throws any exception.
     */
    void runThrowingRunnable(ThrowingRunnable action) throws Exception;

    /**
     * Runs the given supplier within a transaction and returns its result (allows checked exceptions).
     *
     * @param supplier ThrowingSupplier to execute in a transaction.
     * @param <T>      Result type.
     * @return Result returned by supplier.
     * @throws Exception if supplier throws any exception.
     */
    <T> T runThrowingSupplier(ThrowingSupplier<T> supplier) throws Exception;
}
