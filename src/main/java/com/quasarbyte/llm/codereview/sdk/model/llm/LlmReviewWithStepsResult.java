package com.quasarbyte.llm.codereview.sdk.model.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

@JsonClassDescription("Extended code review result that includes both file-level comments and the model's reasoning steps.")
public class LlmReviewWithStepsResult extends LlmReviewResult {
    @JsonPropertyDescription("List of reasoning steps showing how the model evaluated each rule for each file.")
    private List<ThinkStep> thinkSteps;

    public List<ThinkStep> getThinkSteps() {
        return thinkSteps;
    }

    public LlmReviewWithStepsResult setThinkSteps(List<ThinkStep> thinkSteps) {
        this.thinkSteps = thinkSteps;
        return this;
    }
}