package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.sdk.exception.db.NotFoundException;
import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.context.ReviewRunDetails;
import com.quasarbyte.llm.codereview.sdk.model.db.FileDB;
import com.quasarbyte.llm.codereview.sdk.model.db.PromptDB;
import com.quasarbyte.llm.codereview.sdk.model.db.ResolvedFileDB;
import com.quasarbyte.llm.codereview.sdk.model.parameter.*;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptJson;
import com.quasarbyte.llm.codereview.sdk.model.resolved.*;
import com.quasarbyte.llm.codereview.sdk.repository.FileRepository;
import com.quasarbyte.llm.codereview.sdk.repository.PromptRepository;
import com.quasarbyte.llm.codereview.sdk.repository.ResolvedFileRepository;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathToPromptMapper;
import com.quasarbyte.llm.codereview.sdk.service.ReviewRunContext;
import com.quasarbyte.llm.codereview.sdk.service.RuleService;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileService;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResolvedFilePathToPromptMapperImpl implements ResolvedFilePathToPromptMapper {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFilePathToPromptMapperImpl.class);

    private final FileRepository fileRepository;
    private final ObjectMapper objectMapper;
    private final PromptRepository promptRepository;
    private final ResolvedFileRepository resolvedFileRepository;
    private final ReviewPromptJsonMapper reviewPromptJsonMapper;
    private final ReviewRunContext reviewRunContext;
    private final RuleService ruleService;
    private final SourceFileService sourceFileService;

    public ResolvedFilePathToPromptMapperImpl(FileRepository fileRepository,
                                              ObjectMapper objectMapper,
                                              PromptRepository promptRepository,
                                              ResolvedFileRepository resolvedFileRepository,
                                              ReviewPromptJsonMapper reviewPromptJsonMapper,
                                              ReviewRunContext reviewRunContext,
                                              RuleService ruleService,
                                              SourceFileService sourceFileService) {
        this.fileRepository = fileRepository;
        this.objectMapper = objectMapper;
        this.promptRepository = promptRepository;
        this.resolvedFileRepository = resolvedFileRepository;
        this.reviewPromptJsonMapper = reviewPromptJsonMapper;
        this.reviewRunContext = reviewRunContext;
        this.ruleService = ruleService;
        this.sourceFileService = sourceFileService;
    }

    @Override
    public ReviewPrompt map(ResolvedFilesRules filesRules,
                            Boolean useReasoning) {
        logger.info("Mapping ResolvedFilesRules to ReviewPrompt...");
        List<ResolvedFilePath> paths = filesRules.getResolvedFilePaths();
        List<Rule> rules = filesRules.getRules();

        logger.debug("Received {} file paths and {} rules to map",
                paths == null ? 0 : paths.size(),
                rules == null ? 0 : rules.size());

        ReviewPrompt reviewPrompt = new ReviewPrompt()
                .setUseReasoning(useReasoning);

        if (paths != null && !paths.isEmpty()) {
            logger.debug("Processing non-empty paths for prompt construction.");

            ResolvedFilePath resolvedFilePath = paths.get(0);
            ResolvedFileGroupPath resolvedFileGroupPath = resolvedFilePath.getResolvedFileGroupPath();
            ResolvedFileGroup resolvedFileGroup = resolvedFileGroupPath.getResolvedFileGroup();
            ResolvedReviewTarget resolvedReviewTarget = resolvedFileGroup.getResolvedReviewTarget();
            ReviewTarget reviewTarget = resolvedReviewTarget.getReviewTarget();
            ResolvedReviewConfiguration resolvedReviewConfiguration = resolvedReviewTarget.getResolvedReviewConfiguration();
            ReviewParameter reviewParameter = resolvedReviewConfiguration.getReviewConfiguration();
            FileGroup fileGroup = resolvedFileGroup.getFileGroup();

            logger.debug("Loaded ReviewParameter: {}, FileGroup: {}, ReviewTarget: {}",
                    reviewParameter, fileGroup, reviewTarget);

            reviewPrompt.setSystemPromptTexts(reviewParameter.getSystemPrompts());
            reviewPrompt.setFileGroupPromptTexts(fileGroup.getFileGroupPrompts());
            reviewPrompt.setReviewTargetPromptTexts(reviewTarget.getReviewTargetPrompts());
            reviewPrompt.setReviewPromptTexts(reviewParameter.getReviewPrompts());

            final List<PromptRule> promptRules;

            if (rules != null && !rules.isEmpty()) {
                promptRules = rules
                        .stream()
                        .map(this::mapRule)
                        .collect(Collectors.toList());
            } else {
                promptRules = Collections.emptyList();
            }

            reviewPrompt.setRules(promptRules);

            List<PromptFile> files = paths.stream()
                    .map(this::mapFile)
                    .collect(Collectors.toList());
            reviewPrompt.setFiles(files);

            logger.info("Mapped ReviewPrompt with {} files and {} rules.", files.size(), promptRules.size());
        } else {
            logger.warn("No file paths provided. Setting empty file list on ReviewPrompt.");
            reviewPrompt.setFiles(Collections.emptyList());
        }

        ReviewPromptJson reviewPromptJson = reviewPromptJsonMapper.toJson(reviewPrompt);

        ReviewRunDetails reviewRunDetails = Objects.requireNonNull(reviewRunContext.getRunDetails(), "ReviewRunDetails cannot be null.");
        Long reviewId = Objects.requireNonNull(reviewRunDetails.getReviewId(), "Review ID cannot be null.");

        PromptDB promptDB = new PromptDB()
                .setReviewId(reviewId)
                .setReviewPrompt(reviewPromptJson);

        Long promptId = promptRepository.save(promptDB);

        reviewPrompt.setId(promptId);

        logger.debug("ReviewPrompt mapping complete.");
        return reviewPrompt;
    }

    private PromptFile mapFile(ResolvedFilePath resolvedFilePath) {
        logger.debug("Mapping PromptFile for path: {}", resolvedFilePath.getResolvedPath());
        final SourceFile sourceFile = getSourceFile(resolvedFilePath);

        ReviewRunDetails reviewRunDetails = reviewRunContext.getRunDetails();
        Objects.requireNonNull(reviewRunDetails, "reviewRunDetails cannot be null.");
        PersistenceConfiguration persistenceConfiguration = reviewRunDetails.getPersistenceConfiguration();
        Objects.requireNonNull(persistenceConfiguration, "persistenceConfiguration cannot be null.");

        final boolean persistFileContent;

        if (persistenceConfiguration.getPersistFileContent() != null) {
            persistFileContent = persistenceConfiguration.getPersistFileContent();
            logger.debug("Persist file content condition (from parameter): {}", persistFileContent);
        } else {
            persistFileContent = false;
            logger.debug("Persist file content condition (by default): {}", false);
        }

        final Long fileId;
        if (!fileRepository.existsByFilePath(resolvedFilePath.getResolvedPath())) {
            FileDB fileDB = new FileDB()
                    .setReviewId(resolvedFilePath.getResolvedFileGroupPath().getResolvedFileGroup().getResolvedReviewTarget().getReviewId())
                    .setFileName(sourceFile.getFileName())
                    .setFileNameExtension(sourceFile.getFileNameExtension())
                    .setFilePath(sourceFile.getFilePath())
                    .setContent(persistFileContent ? sourceFile.getContent() : null)
                    .setSize(sourceFile.getSize())
                    .setCreatedAt(sourceFile.getCreatedAt())
                    .setModifiedAt(sourceFile.getModifiedAt())
                    .setAccessedAt(sourceFile.getAccessedAt());

            fileId = fileRepository.save(fileDB);
            logger.debug("Created new FileDB with ID: {} for path: {}", fileId, resolvedFilePath.getResolvedPath());
        } else {
            // File already exists, find its ID
            Optional<FileDB> existingFile = fileRepository.findByFilePath(resolvedFilePath.getResolvedPath());
            if (existingFile.isPresent()) {
                fileId = existingFile.get().getId();
                logger.debug("Found existing FileDB with ID: {} for path: {}", fileId, resolvedFilePath.getResolvedPath());
            } else {
                logger.error("File exists check passed but could not retrieve FileDB for path: {}", resolvedFilePath.getResolvedPath());
                throw new NotFoundException("FileDB with path: " + resolvedFilePath.getResolvedPath() + " not found.");
            }
        }

        ResolvedFileDB resolvedFileDB = new ResolvedFileDB()
                .setFileId(fileId)
                .setGroupId(resolvedFilePath.getResolvedFileGroupPath().getResolvedFileGroup().getId())
                .setTargetId(resolvedFilePath.getResolvedFileGroupPath().getResolvedFileGroup().getResolvedReviewTarget().getId())
                .setReviewId(resolvedFilePath.getResolvedFileGroupPath().getResolvedFileGroup().getResolvedReviewTarget().getReviewId())
                .setFileName(sourceFile.getFileName())
                .setFileNameExtension(sourceFile.getFileNameExtension())
                .setFilePath(sourceFile.getFilePath())
                .setCodePage(sourceFile.getCodePage());

        long id = resolvedFileRepository.save(resolvedFileDB);

        logger.trace("Assigning resolvedFileId {} to PromptFile (linked to fileId {})", id, fileId);
        PromptFile pf = new PromptFile()
                .setId(id)
                .setSourceFile(sourceFile);
        logger.debug("Created PromptFile with id: {} (linked to fileId: {})", id, fileId);
        return pf;
    }

    private SourceFile getSourceFile(ResolvedFilePath resolvedFilePath) {
        String path = resolvedFilePath.getResolvedPath();
        String codePage = getCodePage(resolvedFilePath);
        logger.debug("Getting SourceFile for path: {} (codePage: {})", path, codePage);
        final SourceFile sourceFile = sourceFileService.findByPathAndCodePage(path, codePage);
        logger.debug("Got SourceFile for path: {} (codePage: {})", path, codePage);
        return sourceFile;
    }

    private PromptRule mapRule(Rule rule) {
        ReviewRunDetails reviewRunDetails = Objects.requireNonNull(reviewRunContext.getRunDetails(), "ReviewRunDetails cannot be null.");
        Long reviewId = Objects.requireNonNull(reviewRunDetails.getReviewId(), "Review ID cannot be null.");

        final Long id = ruleService.findOrInsertRule(reviewId, rule);

        logger.trace("Mapping PromptRule with id {} and code {}", id, rule.getCode());
        PromptRule pr = new PromptRule()
                .setRuleKey(new RuleKey(id, rule.getCode()))
                .setDescription(rule.getDescription())
                .setSeverity(rule.getSeverity());

        if (logger.isDebugEnabled()) {
            try {
                String promptRuleAsString = objectMapper.writeValueAsString(pr);
                logger.debug("Mapped PromptRule: {}", promptRuleAsString);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to serialize PromptRule for logging: {}", e.getMessage());
            }
        }

        return pr;
    }

    private static String getCodePage(ResolvedFilePath resolvedFilePath) {
        String codePage = Optional.of(resolvedFilePath)
                .map(ResolvedFilePath::getResolvedFileGroupPath)
                .map(ResolvedFileGroupPath::getResolvedFileGroup)
                .map(ResolvedFileGroup::getFileGroup)
                .map(FileGroup::getCodePage)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);
        if (codePage == null) {
            logger.debug("No codePage found for path: {}", resolvedFilePath.getResolvedPath());
        } else {
            logger.trace("Found codePage {} for path: {}", codePage, resolvedFilePath.getResolvedPath());
        }
        return codePage;
    }
}
