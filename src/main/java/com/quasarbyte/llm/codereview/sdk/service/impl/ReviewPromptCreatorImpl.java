package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilesRules;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreator;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReviewPromptCreatorImpl implements ReviewPromptCreator {

    private final PromptRepository promptRepository;
    private final ReviewPromptCombiner reviewPromptCombiner;
    private final ReviewPromptJsonMapper reviewPromptJsonMapper;
    private final ReviewRunContext reviewRunContext;

    public ReviewPromptCreatorImpl(PromptRepository promptRepository, ReviewPromptCombiner reviewPromptCombiner, ReviewPromptJsonMapper reviewPromptJsonMapper, ReviewRunContext reviewRunContext) {
        this.promptRepository = promptRepository;
        this.reviewPromptCombiner = reviewPromptCombiner;
        this.reviewPromptJsonMapper = reviewPromptJsonMapper;
        this.reviewRunContext = reviewRunContext;
    }

    @Override
    public List<ReviewPrompt> create(List<ResolvedFilesRules> resolvedFilesRulesList, boolean useReasoning) {

        final List<ReviewPrompt> result;

        ReviewRunDetails reviewRunDetails = Objects.requireNonNull(reviewRunContext.getRunDetails(), "reviewRunDetails cannot be null");
        Long reviewId = reviewRunDetails.getReviewId();
        boolean reviewIsNew = Objects.requireNonNull(reviewRunDetails.getReviewIsNew(), "reviewIsNew cannot be null");

        if (reviewIsNew) {
            result = reviewPromptCombiner.create(resolvedFilesRulesList, useReasoning);
        } else {
            result = promptRepository.findNotFinishedPromptsByReviewId(reviewId)
                    .stream()
                    .map(promptDB -> {
                        ReviewPrompt reviewPrompt = reviewPromptJsonMapper.fromJson(promptDB.getReviewPrompt());
                        reviewPrompt.setId(promptDB.getId());
                        return reviewPrompt;
                    })
                    .collect(Collectors.toList());
        }

        return result;
    }

}
