package com.quasarbyte.llm.codereview.sdk.model.run;

/**
 * Configuration for controlling run failure conditions based on review results.
 * <p>
 * This class defines the threshold values for warnings and critical issues.
 * If the number of detected warnings or critical issues meets or exceeds the specified thresholds,
 * the run will be marked as failed.
 * </p>
 * <p>
 * Both thresholds are optional. If a threshold is not set or is less than or equal to zero,
 * that severity will not cause the run to fail.
 * </p>
 */
public class RunFailureConfiguration {
    /**
     * The maximum allowed number of warnings before the run is considered failed.
     * <p>
     * If the number of warnings is greater than or equal to this threshold,
     * the run will fail. If this value is {@code null} or less than or equal to zero,
     * warnings will not cause the run to fail.
     * </p>
     */
    private Integer warningThreshold;

    /**
     * The maximum allowed number of critical issues before the run is considered failed.
     * <p>
     * If the number of critical issues is greater than or equal to this threshold,
     * the run will fail. If this value is {@code null} or less than or equal to zero,
     * critical issues will not cause the run to fail.
     * </p>
     */
    private Integer criticalThreshold;

    public Integer getWarningThreshold() {
        return warningThreshold;
    }

    public RunFailureConfiguration setWarningThreshold(Integer warningThreshold) {
        this.warningThreshold = warningThreshold;
        return this;
    }

    public Integer getCriticalThreshold() {
        return criticalThreshold;
    }

    public RunFailureConfiguration setCriticalThreshold(Integer criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
        return this;
    }
}
