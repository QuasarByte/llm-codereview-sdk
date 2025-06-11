package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathToPromptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ReviewPromptCombiner {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPromptCombiner.class);

    private final ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper;

    public ReviewPromptCombiner(ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper) {
        this.resolvedFilePathToPromptMapper = resolvedFilePathToPromptMapper;
    }

    public List<ReviewPrompt> create(List<ResolvedFilesRules> resolvedFilesRulesList, boolean useReasoning) {
        final List<ReviewPrompt> reviewPrompts = new ArrayList<>();

        for (int i = 0; i < resolvedFilesRulesList.size(); i++) {
            ResolvedFilesRules resolvedFRBatch = resolvedFilesRulesList.get(i);
            try {
                logger.debug(
                        "Preparing review prompt #{}: {} files, {} rules",
                        i,
                        resolvedFRBatch.getResolvedFilePaths() != null ? resolvedFRBatch.getResolvedFilePaths().size() : 0,
                        resolvedFRBatch.getRules() != null ? resolvedFRBatch.getRules().size() : 0
                );
                ReviewPrompt reviewPrompt = resolvedFilePathToPromptMapper.map(resolvedFRBatch, useReasoning);

                logger.debug(
                        "Mapped Review prompt #{}, files count = {}, rules count = {}",
                        i,
                        reviewPrompt.getFiles() != null ? reviewPrompt.getFiles().size() : 0,
                        reviewPrompt.getRules() != null ? reviewPrompt.getRules().size() : 0
                );

                reviewPrompts.add(reviewPrompt);

                logger.info("Review prompt #{} prepared successfully.", i);

            } catch (Exception e) {
                logger.error("Review prompt #{} preparation failed with exception: '{}'", i, e.getMessage(), e);
                throw new LLMCodeReviewRuntimeException(String.format("Review prompt #%d failed: %s", i, e.getMessage()), e);
            }
        }

        return reviewPrompts;
    }

}
