package com.quasarbyte.llm.codereview.sdk.model.reviewed;

import com.quasarbyte.llm.codereview.sdk.model.llm.LlmReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessages;

import java.util.List;

public class ReviewedExecutionDetailsItem {
    private ReviewPrompt prompt;
    private LlmMessages llmMessages;
    List<LlmReviewResult> llmReviewResults;

    public ReviewPrompt getPrompt() {
        return prompt;
    }

    public ReviewedExecutionDetailsItem setPrompt(ReviewPrompt prompt) {
        this.prompt = prompt;
        return this;
    }

    public LlmMessages getLlmMessages() {
        return llmMessages;
    }

    public ReviewedExecutionDetailsItem setLlmMessages(LlmMessages llmMessages) {
        this.llmMessages = llmMessages;
        return this;
    }

    public List<LlmReviewResult> getLlmReviewResults() {
        return llmReviewResults;
    }

    public ReviewedExecutionDetailsItem setLlmReviewResults(List<LlmReviewResult> llmReviewResults) {
        this.llmReviewResults = llmReviewResults;
        return this;
    }
}
