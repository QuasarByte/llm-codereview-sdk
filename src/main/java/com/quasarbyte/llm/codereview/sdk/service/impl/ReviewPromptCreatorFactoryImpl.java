package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreator;
import com.quasarbyte.llm.codereview.sdk.service.ReviewPromptCreatorFactory;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;

public class ReviewPromptCreatorFactoryImpl implements ReviewPromptCreatorFactory {

    private final PromptRepository promptRepository;
    private final ReviewPromptCombiner reviewPromptCombiner;
    private final ReviewPromptJsonMapper reviewPromptJsonMapper;
    private final ReviewRunContext reviewRunContext;

    public ReviewPromptCreatorFactoryImpl(PromptRepository promptRepository, ReviewPromptCombiner reviewPromptCombiner, ReviewPromptJsonMapper reviewPromptJsonMapper, ReviewRunContext reviewRunContext) {
        this.promptRepository = promptRepository;
        this.reviewPromptCombiner = reviewPromptCombiner;
        this.reviewPromptJsonMapper = reviewPromptJsonMapper;
        this.reviewRunContext = reviewRunContext;
    }

    @Override
    public ReviewPromptCreator create() {
        return new ReviewPromptCreatorImpl(promptRepository, reviewPromptCombiner, reviewPromptJsonMapper, reviewRunContext);
    }

}
