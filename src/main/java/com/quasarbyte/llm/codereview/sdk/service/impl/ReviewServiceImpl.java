package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRuntimeException;
import com.quasarbyte.llm.codereview.sdk.exception.ValidationException;
import com.quasarbyte.llm.codereview.sdk.exception.db.NotFoundException;
import com.quasarbyte.llm.codereview.sdk.exception.db.PersistenceRuntimeException;
import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedResult;
import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.db.ReviewResultDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;
import com.quasarbyte.llm.codereview.sdk.model.review.*;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedComment;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedDetailedResult;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedThinkStep;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ReviewResultRepository;
import com.quasarbyte.llm.codereview.sdk.repository.RunRepository;
import com.quasarbyte.llm.codereview.sdk.service.ReviewDetailsService;
import com.quasarbyte.llm.codereview.sdk.service.ReviewResultAggregator;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.ReviewService;
import com.quasarbyte.llm.codereview.sdk.service.db.core.PersistenceConfigurationContext;
import com.quasarbyte.llm.codereview.sdk.service.db.core.connection.DBConnectionManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.datasource.DataSourceManager;
import com.quasarbyte.llm.codereview.sdk.service.db.core.transaction.runner.TransactionRunner;
import com.quasarbyte.llm.codereview.sdk.service.liquibase.manager.LiquibaseMigrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final DBConnectionManager dbConnectionManager;
    private final DataSourceManager dataSourceManager;
    private final LiquibaseMigrationManager liquibaseMigrationManager;
    private final PersistenceConfigurationContext persistenceConfigurationContext;
    private final ReviewDetailsService reviewDetailsService;
    private final ReviewRepository reviewRepository;
    private final ReviewResultAggregator reviewResultAggregator;
    private final ReviewResultRepository reviewResultRepository;
    private final ReviewRunContext reviewRunContext;
    private final RunRepository runRepository;
    private final TransactionRunner transactionRunner;

    public ReviewServiceImpl(DBConnectionManager dbConnectionManager,
                             DataSourceManager dataSourceManager,
                             LiquibaseMigrationManager liquibaseMigrationManager,
                             PersistenceConfigurationContext persistenceConfigurationContext,
                             ReviewDetailsService reviewDetailsService,
                             ReviewRepository reviewRepository,
                             ReviewResultAggregator reviewResultAggregator,
                             ReviewResultRepository reviewResultRepository,
                             ReviewRunContext reviewRunContext,
                             RunRepository runRepository,
                             TransactionRunner transactionRunner) {
        this.dbConnectionManager = dbConnectionManager;
        this.dataSourceManager = dataSourceManager;
        this.liquibaseMigrationManager = liquibaseMigrationManager;
        this.persistenceConfigurationContext = persistenceConfigurationContext;
        this.reviewDetailsService = reviewDetailsService;
        this.reviewRepository = reviewRepository;
        this.reviewResultAggregator = reviewResultAggregator;
        this.reviewResultRepository = reviewResultRepository;
        this.reviewRunContext = reviewRunContext;
        this.runRepository = runRepository;
        this.transactionRunner = transactionRunner;
    }

    @Override
    public ReviewResult review(ReviewParameter reviewParameter,
                               LlmClient llmClient,
                               PersistenceConfiguration persistenceConfiguration) {

        logger.info("Starting review process.");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        Objects.requireNonNull(llmClient, "llmClient must not be null");

        final PersistenceConfiguration resolvedPersistenceConfiguration = resolvePersistenceConfiguration(persistenceConfiguration);

        try (AutoCloseable pcAutoCloseable = persistenceConfigurationContext.setPersistenceConfiguration(resolvedPersistenceConfiguration)) {

            try (AutoCloseable dsmAutoCloseable = dataSourceManager) {

                liquibaseMigrationManager.runMigrations();

                final Long[] reviewIdArr = new Long[1];
                final Boolean[] reviewIsNewArr = new Boolean[1];
                final Long[] runIdArr = new Long[1];

                try (Connection ignored = dbConnectionManager.openConnection()) {

                    final Long reviewId = reviewParameter.getReviewId();

                    if (reviewId == null) {
                        transactionRunner.runRunnable(() -> {
                            reviewIdArr[0] = reviewRepository.save();
                            logger.info("reviewId: {}", reviewIdArr[0]);
                            runIdArr[0] = runRepository.save(reviewIdArr[0], reviewParameter).getId();
                            logger.info("runId: {}", runIdArr[0]);
                        });

                        reviewIsNewArr[0] = true;

                    } else {

                        transactionRunner.runRunnable(() -> {

                            if (reviewRepository.existsById(reviewId)) {
                                reviewIdArr[0] = reviewId;
                                reviewIsNewArr[0] = false;
                            } else {
                                throw new NotFoundException(String.format("Review with ID %d not found", reviewId));
                            }

                            logger.info("reviewId: {}", reviewIdArr[0]);
                            runIdArr[0] = runRepository.save(reviewIdArr[0], reviewParameter).getId();
                            logger.info("runId: {}", runIdArr[0]);
                        });

                    }

                } catch (Exception e) {
                    logger.error("Error, error message: {}", e.getMessage(), e);
                    throw new PersistenceRuntimeException(e);
                }

                logger.info("reviewIsNew: {}", reviewIsNewArr[0]);

                reviewRunContext.setReviewRunDetails(new ReviewRunDetails(
                        reviewIsNewArr[0],
                        reviewIdArr[0],
                        runIdArr[0],
                        resolvedPersistenceConfiguration));

                logger.debug("Invoking reviewDetailsService.review(...)");
                ReviewedDetailedResult reviewedDetailedResult = reviewDetailsService.review(reviewParameter, llmClient);

                logger.debug("Aggregating reviewed result.");
                AggregatedResult aggregatedResult = reviewResultAggregator.aggregate(reviewedDetailedResult);

                logger.debug("Mapping aggregated files to ReviewResultItem list.");
                List<ReviewResultItem> reviewResultItems = aggregatedResult.getFiles()
                        .stream()
                        .sorted(Comparator.comparing(aggregatedFile -> aggregatedFile.getSourceFile().getFilePath()))
                        .map(ReviewServiceImpl::mapAggregatedFileToReviewResultItem)
                        .collect(Collectors.toList());

                logger.info("Review completed: {} files processed.", reviewResultItems.size());

                ReviewResult finalReviewResult = new ReviewResult()
                        .setItems(reviewResultItems)
                        .setCompletionUsage(new ReviewCompletionUsage()
                                .setCompletionTokens(aggregatedResult.getCompletionUsage().getCompletionTokens())
                                .setPromptTokens(aggregatedResult.getCompletionUsage().getPromptTokens())
                                .setTotalTokens(aggregatedResult.getCompletionUsage().getTotalTokens())
                        );

                // Save the review result to the database
                try {
                    logger.debug("Saving review result to database for review ID: {}, run ID: {}", reviewIdArr[0], runIdArr[0]);

                    ReviewResultDB reviewResultDB = new ReviewResultDB()
                            .setReviewId(reviewIdArr[0])
                            .setRunId(runIdArr[0])
                            .setReviewParameter(reviewParameter)
                            .setReviewResult(finalReviewResult);

                    Long reviewResultId = reviewResultRepository.save(reviewResultDB);
                    logger.info("Successfully saved review result with ID: {} for review ID: {}, run ID: {}",
                            reviewResultId, reviewIdArr[0], runIdArr[0]);
                } catch (Exception e) {
                    logger.error("Failed to save review result for review ID: {}, run ID: {}. Error: {}",
                            reviewIdArr[0], runIdArr[0], e.getMessage(), e);
                    // Don't fail the entire review process if saving fails
                }

                return finalReviewResult;
            }

        } catch (Exception e) {
            throw new LLMCodeReviewRuntimeException(e);
        }
    }

    @Override
    public ReviewResult review(ReviewParameter reviewParameter,
                               List<LlmClient> llmClients,
                               PersistenceConfiguration persistenceConfiguration,
                               ParallelExecutionParameter parallelExecutionParameter) {

        logger.info("Starting review process with load balancing.");
        Objects.requireNonNull(reviewParameter, "reviewParameter must not be null");
        Objects.requireNonNull(llmClients, "llmClients must not be null");
        Objects.requireNonNull(parallelExecutionParameter, "parallelExecutionParameter must not be null");

        final PersistenceConfiguration resolvedPersistenceConfiguration = resolvePersistenceConfiguration(persistenceConfiguration);

        // Handle single client case - use existing single client method
        if (llmClients.size() == 1) {
            logger.info("Only one LLM client provided, using single client review method");
            return review(reviewParameter, llmClients.get(0), resolvedPersistenceConfiguration); // Use existing method with a single client
        }

        try (AutoCloseable pcAutoCloseable = persistenceConfigurationContext.setPersistenceConfiguration(resolvedPersistenceConfiguration)) {

            try (AutoCloseable dsmAutoCloseable = dataSourceManager) {

                liquibaseMigrationManager.runMigrations();

                final Long[] reviewIdArr = new Long[1];
                final Boolean[] reviewIsNewArr = new Boolean[1];
                final Long[] runIdArr = new Long[1];

                try (Connection ignored = dbConnectionManager.openConnection()) {

                    final Long reviewId = reviewParameter.getReviewId();

                    if (reviewId == null) {
                        transactionRunner.runRunnable(() -> {
                            reviewIdArr[0] = reviewRepository.save();
                            logger.info("reviewId: {}", reviewIdArr[0]);
                            runIdArr[0] = runRepository.save(reviewIdArr[0], reviewParameter).getId();
                            logger.info("runId: {}", runIdArr[0]);
                        });

                        reviewIsNewArr[0] = true;

                    } else {

                        transactionRunner.runRunnable(() -> {

                            if (reviewRepository.existsById(reviewId)) {
                                reviewIdArr[0] = reviewId;
                                reviewIsNewArr[0] = false;
                            } else {
                                throw new NotFoundException(String.format("Review with ID %d not found", reviewId));
                            }

                            logger.info("reviewId: {}", reviewIdArr[0]);
                            runIdArr[0] = runRepository.save(reviewIdArr[0], reviewParameter).getId();
                            logger.info("runId: {}", runIdArr[0]);
                        });

                    }

                } catch (Exception e) {
                    logger.error("Error, error message: {}", e.getMessage(), e);
                    throw new PersistenceRuntimeException(e);
                }

                logger.info("reviewIsNew: {}", reviewIsNewArr[0]);

                reviewRunContext.setReviewRunDetails(new ReviewRunDetails(
                        reviewIsNewArr[0],
                        reviewIdArr[0],
                        runIdArr[0],
                        resolvedPersistenceConfiguration));

                logger.debug("Invoking reviewDetailsService.review(...) with load balancing");
                ReviewedDetailedResult reviewedDetailedResult = reviewDetailsService.review(reviewParameter, llmClients, parallelExecutionParameter);

                logger.debug("Aggregating reviewed result.");
                AggregatedResult aggregatedResult = reviewResultAggregator.aggregate(reviewedDetailedResult);

                logger.debug("Mapping aggregated files to ReviewResultItem list.");
                List<ReviewResultItem> reviewResultItems = aggregatedResult.getFiles()
                        .stream()
                        .sorted(Comparator.comparing(aggregatedFile -> aggregatedFile.getSourceFile().getFilePath()))
                        .map(ReviewServiceImpl::mapAggregatedFileToReviewResultItem)
                        .collect(Collectors.toList());

                logger.info("Review with load balancing completed: {} files processed.", reviewResultItems.size());

                ReviewResult finalReviewResult = new ReviewResult()
                        .setItems(reviewResultItems)
                        .setCompletionUsage(new ReviewCompletionUsage()
                                .setCompletionTokens(aggregatedResult.getCompletionUsage().getCompletionTokens())
                                .setPromptTokens(aggregatedResult.getCompletionUsage().getPromptTokens())
                                .setTotalTokens(aggregatedResult.getCompletionUsage().getTotalTokens())
                        );

                // Save the review result to the database
                try {
                    logger.debug("Saving review result to database for review ID: {}, run ID: {}", reviewIdArr[0], runIdArr[0]);

                    ReviewResultDB reviewResultDB = new ReviewResultDB()
                            .setReviewId(reviewIdArr[0])
                            .setRunId(runIdArr[0])
                            .setReviewParameter(reviewParameter)
                            .setReviewResult(finalReviewResult);

                    Long reviewResultId = reviewResultRepository.save(reviewResultDB);
                    logger.info("Successfully saved review result with ID: {} for review ID: {}, run ID: {}",
                            reviewResultId, reviewIdArr[0], runIdArr[0]);
                } catch (Exception e) {
                    logger.error("Failed to save review result for review ID: {}, run ID: {}. Error: {}",
                            reviewIdArr[0], runIdArr[0], e.getMessage(), e);
                    // Don't fail the entire review process if saving fails
                }

                return finalReviewResult;
            }

        } catch (Exception e) {
            throw new LLMCodeReviewRuntimeException(e);
        }
    }

    private static ReviewResultItem mapAggregatedFileToReviewResultItem(AggregatedFile aggregatedFile) {
        logger.debug("Mapping AggregatedFile '{}' to ReviewResultItem.", aggregatedFile.getSourceFile().getFilePath());
        ReviewFile reviewFile = mapSourceFileToReviewFile(aggregatedFile.getSourceFile());

        List<ReviewedComment> reviewedComments = aggregatedFile.getComments() != null ? aggregatedFile.getComments() : Collections.emptyList();

        List<ReviewComment> comments = reviewedComments
                .stream()
                .map(ReviewServiceImpl::mapAggregatedCommentToReviewComment)
                .collect(Collectors.toList());

        List<ReviewedThinkStep> reviewedThinkSteps = aggregatedFile.getReviewedThinkSteps() != null ? aggregatedFile.getReviewedThinkSteps() : Collections.emptyList();

        List<ReviewThinkStep> thinkSteps = reviewedThinkSteps
                .stream()
                .map(ss -> new ReviewThinkStep()
                        .setFileId(ss.getFileId())
                        .setFileName(ss.getFileName())
                        .setRuleId(ss.getRuleId())
                        .setRuleCode(ss.getRuleCode())
                        .setThinkText(ss.getThinkText()))
                .collect(Collectors.toList());

        logger.debug("Mapped {} comments for file '{}'.", comments.size(), aggregatedFile.getSourceFile().getFilePath());
        return new ReviewResultItem()
                .setFile(reviewFile)
                .setComments(comments)
                .setThinkSteps(thinkSteps);
    }

    private static ReviewFile mapSourceFileToReviewFile(SourceFile sourceFile) {
        logger.trace("Mapping SourceFile '{}' to ReviewFile.", sourceFile.getFilePath());
        return new ReviewFile()
                .setFileName(sourceFile.getFileName())
                .setFilePath(sourceFile.getFilePath())
                .setSize(sourceFile.getSize())
                .setCreatedAt(sourceFile.getCreatedAt())
                .setModifiedAt(sourceFile.getModifiedAt())
                .setAccessedAt(sourceFile.getAccessedAt());
    }

    private static ReviewComment mapAggregatedCommentToReviewComment(ReviewedComment comment) {
        if (comment == null) {
            logger.warn("ReviewedComment is null; returning empty ReviewComment.");
            return new ReviewComment();
        }
        Optional<ReviewedComment> reviewedCommentOptional = Optional.of(comment);
        Optional<PromptRule> promptRuleOptional = reviewedCommentOptional.map(ReviewedComment::getRule);

        Rule rule = new Rule()
                .setCode(promptRuleOptional.map(PromptRule::getRuleKey).map(RuleKey::getCode).orElse(null))
                .setDescription(promptRuleOptional.map(PromptRule::getDescription).orElse(null))
                .setSeverity(promptRuleOptional.map(PromptRule::getSeverity).orElse(null));

        logger.trace("Mapping ReviewedComment at line {} col {} to ReviewComment.",
                reviewedCommentOptional.map(ReviewedComment::getLine).orElse(null),
                reviewedCommentOptional.map(ReviewedComment::getColumn).orElse(null));

        return new ReviewComment()
                .setRule(rule)
                .setRuleId(reviewedCommentOptional.map(ReviewedComment::getRuleId).orElse(null))
                .setRuleCode(reviewedCommentOptional.map(ReviewedComment::getRuleCode).orElse(null))
                .setLine(reviewedCommentOptional.map(ReviewedComment::getLine).orElse(null))
                .setColumn(reviewedCommentOptional.map(ReviewedComment::getColumn).orElse(null))
                .setMessage(reviewedCommentOptional.map(ReviewedComment::getMessage).orElse(null))
                .setSuggestion(reviewedCommentOptional.map(ReviewedComment::getSuggestion).orElse(null));
    }

    private static PersistenceConfiguration resolvePersistenceConfiguration(PersistenceConfiguration persistenceConfiguration) {
        final PersistenceConfiguration resolvedPersistenceConfiguration;

        if (persistenceConfiguration == null || persistenceConfiguration.getDataSourceConfiguration() == null) {
            resolvedPersistenceConfiguration = new PersistenceConfiguration()
                    .setDataSourceConfiguration(new DataSourceConfiguration()
                            .setDriverClassName("org.sqlite.JDBC")
                            .setJdbcUrl("jdbc:sqlite::memory:"))
                    .setPersistFileContent(false);

        } else {
            validatePersistenceConfiguration(persistenceConfiguration);
            resolvedPersistenceConfiguration = persistenceConfiguration;
        }

        return resolvedPersistenceConfiguration;
    }

    private static void validatePersistenceConfiguration(PersistenceConfiguration persistenceConfiguration) {
        Objects.requireNonNull(persistenceConfiguration, "persistenceConfiguration cannot be null");
        Objects.requireNonNull(persistenceConfiguration.getDataSourceConfiguration(), "dataSourceConfiguration cannot be null");

        String driverClassName = persistenceConfiguration.getDataSourceConfiguration().getDriverClassName();
        String jdbcUrl = persistenceConfiguration.getDataSourceConfiguration().getJdbcUrl();

        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            throw new ValidationException("DriverClassName cannot be null or blank");
        }

        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            throw new ValidationException("JdbcUrl cannot be null or blank");
        }
    }

}
