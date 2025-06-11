package com.quasarbyte.llm.codereview.sdk.model.context;

import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;

public class ReviewRunDetails {
    private final Boolean reviewIsNew;
    private final Long reviewId;
    private final Long runId;
    private final PersistenceConfiguration persistenceConfiguration;

    public ReviewRunDetails(Boolean reviewIsNew, Long reviewId, Long runId, PersistenceConfiguration persistenceConfiguration) {
        this.reviewIsNew = reviewIsNew;
        this.reviewId = reviewId;
        this.runId = runId;
        this.persistenceConfiguration = persistenceConfiguration;
    }

    public Boolean getReviewIsNew() {
        return reviewIsNew;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public Long getRunId() {
        return runId;
    }

    public PersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
    }
}
