package com.quasarbyte.llm.codereview.sdk.model.statistics;

public class SeverityStatistics {
    private Long infoCount;
    private Long warningCount;
    private Long criticalCount;

    public Long getInfoCount() {
        return infoCount;
    }

    public SeverityStatistics setInfoCount(Long infoCount) {
        this.infoCount = infoCount;
        return this;
    }

    public Long getWarningCount() {
        return warningCount;
    }

    public SeverityStatistics setWarningCount(Long warningCount) {
        this.warningCount = warningCount;
        return this;
    }

    public Long getCriticalCount() {
        return criticalCount;
    }

    public SeverityStatistics setCriticalCount(Long criticalCount) {
        this.criticalCount = criticalCount;
        return this;
    }
}
