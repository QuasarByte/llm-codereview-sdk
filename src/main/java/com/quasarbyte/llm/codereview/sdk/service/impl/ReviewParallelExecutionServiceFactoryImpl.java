package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewParallelExecutionServiceFactoryImpl implements ReviewParallelExecutionServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewParallelExecutionServiceFactoryImpl.class);

    @Override
    public ReviewParallelExecutionService create() {
        logger.info("Creating ReviewParallelExecutionService...");

        logger.debug("Instantiating LlmMessagesMapper");
        LlmMessagesMapper llmMessagesMapper = new RhinoLlmMessagesMapperImpl();

        logger.debug("Instantiating ChatCompletionCreateParamsFactory");
        ChatCompletionCreateParamsFactory chatCompletionCreateParamsFactory = new ChatCompletionCreateParamsFactoryImpl();

        logger.debug("Instantiating LlmReviewProcessor");
        LlmReviewProcessor llmReviewProcessor = new LlmReviewProcessorImpl(llmMessagesMapper, chatCompletionCreateParamsFactory);

        logger.debug("Instantiating SourceFileReader");
        SourceFileReader sourceFileReader = new SourceFileReaderImpl();

        logger.debug("Instantiating ResolvedFilePathToPromptMapper");
        ResolvedFilePathToPromptMapper resolvedFilePathToPromptMapper = new ResolvedFilePathToPromptMapperImpl(sourceFileReader);

        logger.debug("Instantiating SingleThreadTaskDispatcher");
        SingleThreadTaskDispatcher singleThreadTaskDispatcher = new SingleThreadTaskDispatcherImpl(llmReviewProcessor, resolvedFilePathToPromptMapper);

        logger.debug("Instantiating MultiThreadTaskDispatcher");
        MultiThreadTaskDispatcher multiThreadTaskDispatcher = new MultiThreadTaskDispatcherImpl(llmReviewProcessor, resolvedFilePathToPromptMapper);

        logger.debug("Instantiating ResolvedFilePathSplitter");
        ResolvedFilePathSplitter resolvedFilePathSplitter = new ResolvedFilePathSplitterImpl();

        logger.debug("Instantiating SymlinkResolverService");
        SymlinkResolverService symlinkResolverService = new SymlinkResolverServiceImpl();

        logger.debug("Instantiating GlobPatternMatcherService");
        GlobPatternMatcherService globPatternMatcherService = new GlobPatternMatcherServiceImpl(symlinkResolverService);

        logger.debug("Instantiating TargetResolverService");
        TargetResolverService targetResolverService = new TargetResolverServiceImpl(
                globPatternMatcherService,
                symlinkResolverService);

        logger.debug("Instantiating ReviewConfigurationResolver");
        ReviewConfigurationResolver reviewConfigurationResolver = new ReviewConfigurationResolverImpl(targetResolverService);

        logger.debug("Instantiating RulesToBatchesSplitter");
        RulesToBatchesSplitter rulesToBatchesSplitter = new RulesToBatchesSplitterImpl();

        logger.debug("Instantiating RulesMerger");
        RulesMerger rulesMerger = new RulesMergerImpl();

        logger.debug("Instantiating LlmRequestQuotaGuard");
        LlmRequestQuotaGuard llmRequestQuotaGuard = new LlmRequestQuotaGuardImpl();

        logger.debug("Instantiating ReviewDetailsService");
        ReviewDetailsService reviewDetailsService = new ReviewDetailsServiceImpl(
                llmRequestQuotaGuard,
                multiThreadTaskDispatcher,
                resolvedFilePathSplitter,
                reviewConfigurationResolver,
                rulesMerger,
                rulesToBatchesSplitter,
                singleThreadTaskDispatcher);

        logger.debug("Instantiating ReviewResultAggregator");
        ReviewResultAggregator reviewResultAggregator = new ReviewResultAggregatorImpl();

        logger.info("ReviewParallelExecutionService successfully created.");
        return new ReviewParallelExecutionServiceImpl(reviewDetailsService, reviewResultAggregator);
    }
}
