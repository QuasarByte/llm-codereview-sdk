package com.quasarbyte.llm.codereview.sdk.model.parameter;

import java.util.concurrent.ExecutorService;

public class ParallelExecutionParameter {
    private Integer batchSize;
    private ExecutorService executorService;

    public Integer getBatchSize() {
        return batchSize;
    }

    public ParallelExecutionParameter setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ParallelExecutionParameter setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }
}
