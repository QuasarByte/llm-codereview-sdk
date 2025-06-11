package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepository;
import com.quasarbyte.llm.codereview.sdk.repository.InferenceRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewTargetRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewTargetRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.repository.RunRepository;
import com.quasarbyte.llm.codereview.sdk.repository.RunRepositoryFactory;
import com.quasarbyte.llm.codereview.sdk.repository.impl.InferenceRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.PromptRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.ReviewRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.ReviewTargetRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.repository.impl.RunRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.*;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl.DBConnectionContextFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.impl.DBConnectionManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl.DataSourceFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.impl.DataSourceManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.impl.PersistenceConfigurationContextFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.DbPojoJsonConvertor;
import com.quasarbyte.llm.codereview.sdk.service.db.core.pojo.impl.DbPojoJsonConvertorFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplate;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.JDBCTemplateFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.template.impl.JDBCTemplateFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunner;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunnerFactory;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.impl.TransactionRunnerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.DBTransactionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.impl.DBTransactionManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManagerFactory;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.impl.LiquibaseMigrationManagerFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapperFactory;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapper;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapperFactory;
import com.quasarbyte.llm.codereview.sdk.service.mapper.impl.ReviewPromptJsonMapperFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.mapper.impl.ReviewedResultItemJsonMapperFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewParallelExecutionServiceFactoryImpl implements ReviewParallelExecutionServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReviewParallelExecutionServiceFactoryImpl.class);

    public ReviewParallelExecutionServiceFactoryImpl() {
    }

    @Override
    public ReviewParallelExecutionService create() {
        logger.info("Creating ReviewParallelExecutionService...");

        logger.debug("Instantiating LlmMessagesMapper");
        LlmMessagesMapper llmMessagesMapper = new RhinoLlmMessagesMapperImpl();

        logger.debug("Instantiating ChatCompletionCreateParamsFactory");
        ChatCompletionCreateParamsFactory chatCompletionCreateParamsFactory = new ChatCompletionCreateParamsFactoryImpl();

        logger.debug("Instantiating LlmReviewProcessor");
        LlmReviewProcessor llmReviewProcessor = new LlmReviewProcessorImpl(llmMessagesMapper, chatCompletionCreateParamsFactory);

        logger.debug("Instantiating ObjectMapper");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        logger.debug("Instantiating DBConnectionContext");
        DBConnectionContext dbConnectionContext = new DBConnectionContextFactoryImpl().create();

        logger.debug("Instantiating PersistenceConfigurationContext");
        PersistenceConfigurationContext persistenceConfigurationContext = new PersistenceConfigurationContextFactoryImpl().create();

        logger.debug("Instantiating DataSourceFactory");
        DataSourceFactory dataSourceFactory = new DataSourceFactoryImpl(persistenceConfigurationContext);

        logger.debug("Instantiating DataSourceManager");
        DataSourceManager dataSourceManager = new DataSourceManagerFactoryImpl(dataSourceFactory).create();

        logger.debug("Instantiating DBConnectionManager");
        DBConnectionManager dbConnectionManager = new DBConnectionManagerFactoryImpl(dbConnectionContext, dataSourceManager).create();

        logger.debug("Instantiating DbPojoJsonConvertor");
        DbPojoJsonConvertor dbPojoJsonConvertor = new DbPojoJsonConvertorFactoryImpl(objectMapper).create();

        logger.debug("Instantiating JDBCTemplateFactory");
        JDBCTemplateFactory jdbcTemplateFactory = new JDBCTemplateFactoryImpl(dbConnectionManager);

        logger.debug("Instantiating JDBCTemplate");
        JDBCTemplate jdbcTemplate = jdbcTemplateFactory.create();

        logger.debug("Instantiating ReviewRunContextFactory");
        ReviewRunContextFactory reviewRunContextFactory = new ReviewRunContextFactoryImpl();

        logger.debug("Instantiating ReviewRunContext");
        ReviewRunContext reviewRunContext = reviewRunContextFactory.create();

        logger.debug("Instantiating ReviewPromptCombinerFactory");
        ReviewPromptCombinerFactory reviewPromptCombinerFactory = new ReviewPromptCombinerFactoryImpl(dbPojoJsonConvertor, jdbcTemplate, objectMapper, reviewRunContext);

        logger.debug("Instantiating ReviewPromptCombiner");
        ReviewPromptCombiner reviewPromptCombiner = reviewPromptCombinerFactory.create();

        logger.debug("Instantiating PromptRepositoryFactory");
        PromptRepositoryFactory promptRepositoryFactory = new PromptRepositoryFactoryImpl(dbPojoJsonConvertor, jdbcTemplate);

        logger.debug("Instantiating PromptRepositoryFactory");
        PromptRepository promptRepository = promptRepositoryFactory.create();

        logger.debug("Instantiating ReviewPromptJsonMapperFactory");
        ReviewPromptJsonMapperFactory reviewPromptJsonMapperFactory = new ReviewPromptJsonMapperFactoryImpl();

        logger.debug("Instantiating ReviewPromptJsonMapper");
        ReviewPromptJsonMapper reviewPromptJsonMapper = reviewPromptJsonMapperFactory.create();

        logger.debug("Instantiating ReviewPromptCreatorFactory");
        ReviewPromptCreatorFactory reviewPromptCreatorFactory = new ReviewPromptCreatorFactoryImpl(promptRepository, reviewPromptCombiner, reviewPromptJsonMapper, reviewRunContext);

        logger.debug("Instantiating ReviewPromptCreator");
        ReviewPromptCreator reviewPromptCreator = reviewPromptCreatorFactory.create();

        logger.debug("Instantiating InferenceRepositoryFactory");
        InferenceRepositoryFactory inferenceRepositoryFactory = new InferenceRepositoryFactoryImpl(dbPojoJsonConvertor, jdbcTemplate);

        logger.debug("Instantiating InferenceRepository");
        InferenceRepository inferenceRepository = inferenceRepositoryFactory.create();

        logger.debug("Instantiating ReviewedResultItemJsonMapperFactory");
        ReviewedResultItemJsonMapperFactory reviewedResultItemJsonMapperFactory = new ReviewedResultItemJsonMapperFactoryImpl();

        logger.debug("Instantiating ReviewedResultItemJsonMapper");
        ReviewedResultItemJsonMapper reviewedResultItemJsonMapper = reviewedResultItemJsonMapperFactory.create();

        logger.debug("Instantiating LlmReviewProcessorStatefulFactoryImpl");
        LlmReviewProcessorStatefulFactoryImpl llmReviewProcessorStatefulFactory = new LlmReviewProcessorStatefulFactoryImpl(inferenceRepository, llmReviewProcessor, reviewRunContext, reviewedResultItemJsonMapper);

        logger.debug("Instantiating LlmReviewProcessor");
        LlmReviewProcessor llmReviewProcessorStatefulProcessor = llmReviewProcessorStatefulFactory.create();

        logger.debug("Instantiating LlmClientLoadBalancerRoundRobin");
        LlmClientLoadBalancerRoundRobin roundRobinLoadBalancer = new LlmClientLoadBalancerRoundRobinImpl();

        logger.debug("Instantiating LlmClientLoadBalancerRandom");
        LlmClientLoadBalancerRandom randomLoadBalancer = new LlmClientLoadBalancerRandomImpl();

        logger.debug("Instantiating SingleThreadTaskDispatcher");
        SingleThreadTaskDispatcher singleThreadTaskDispatcher = new SingleThreadTaskDispatcherImpl(llmReviewProcessorStatefulProcessor, reviewPromptCreator, roundRobinLoadBalancer, randomLoadBalancer);

        logger.debug("Instantiating MultiThreadTaskDispatcher");
        MultiThreadTaskDispatcher multiThreadTaskDispatcher = new MultiThreadTaskDispatcherImpl(llmReviewProcessorStatefulProcessor, reviewPromptCreator, roundRobinLoadBalancer, randomLoadBalancer);

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

        logger.debug("Instantiating ReviewTargetRepositoryFactory");
        ReviewTargetRepositoryFactory reviewTargetRepositoryFactory = new ReviewTargetRepositoryFactoryImpl(new DbPojoJsonConvertorFactoryImpl(objectMapper), jdbcTemplateFactory);

        logger.debug("Instantiating ReviewTargetRepository");
        ReviewTargetRepository reviewTargetRepository = reviewTargetRepositoryFactory.create();

        logger.debug("Instantiating ReviewDetailsService");
        ReviewDetailsService reviewDetailsService = new ReviewDetailsServiceImpl(
                llmRequestQuotaGuard,
                multiThreadTaskDispatcher,
                resolvedFilePathSplitter,
                reviewTargetRepository,
                reviewConfigurationResolver,
                reviewRunContext,
                rulesMerger,
                rulesToBatchesSplitter,
                singleThreadTaskDispatcher);

        logger.debug("Instantiating ReviewResultAggregator");
        ReviewResultAggregator reviewResultAggregator = new ReviewResultAggregatorImpl();

        // Add missing dependencies for persistence
        logger.debug("Instantiating ReviewRepositoryFactory");
        ReviewRepository reviewRepository = new ReviewRepositoryFactoryImpl(jdbcTemplate).create();

        logger.debug("Instantiating RunRepositoryFactory");
        RunRepository runRepository = new RunRepositoryFactoryImpl(dbPojoJsonConvertor, jdbcTemplate).create();

        logger.debug("Instantiating DBTransactionManager");
        DBTransactionManager dbTransactionManager = new DBTransactionManagerFactoryImpl(dbConnectionContext).create();

        logger.debug("Instantiating TransactionRunner");
        TransactionRunner transactionRunner = new TransactionRunnerFactoryImpl(dbTransactionManager).create();

        logger.debug("Instantiating LiquibaseMigrationManager");
        LiquibaseMigrationManager liquibaseMigrationManager = new LiquibaseMigrationManagerFactoryImpl(dbConnectionManager).create();

        logger.info("ReviewParallelExecutionService successfully created.");
        return new ReviewParallelExecutionServiceImpl(
                dbConnectionManager,
                dataSourceManager,
                liquibaseMigrationManager,
                persistenceConfigurationContext,
                reviewDetailsService,
                reviewRepository,
                reviewResultAggregator,
                reviewRunContext,
                runRepository,
                transactionRunner);
    }
}
