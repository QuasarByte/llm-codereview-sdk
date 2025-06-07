package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPromptExecutionDetails;
import com.quasarbyte.llm.codereview.sdk.model.resolved.*;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathToPromptMapper;
import com.quasarbyte.llm.codereview.sdk.service.SourceFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ResolvedFilePathToPromptMapperImpl implements ResolvedFilePathToPromptMapper {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFilePathToPromptMapperImpl.class);

    private final SourceFileReader sourceFileReader;

    public ResolvedFilePathToPromptMapperImpl(SourceFileReader sourceFileReader) {
        this.sourceFileReader = sourceFileReader;
        logger.debug("Initialized ResolvedFilePathToPromptMapperImpl with SourceFileReader: {}", sourceFileReader);
    }

    @Override
    public ReviewPrompt map(ResolvedFilesRules filesRules,
                            AtomicLong fileId,
                            AtomicLong ruleId,
                            Map<String, SourceFile> sourceFileCache) {
        logger.info("Mapping ResolvedFilesRules to ReviewPrompt...");
        List<ResolvedFilePath> paths = filesRules.getResolvedFilePaths();
        List<Rule> rules = filesRules.getRules();

        logger.debug("Received {} file paths and {} rules to map",
                paths == null ? 0 : paths.size(),
                rules == null ? 0 : rules.size());

        ReviewPrompt reviewPrompt = new ReviewPrompt()
                .setExecutionDetails(new ReviewPromptExecutionDetails()
                        .setResolvedFilePaths(paths));

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
                        .map(rule -> mapRule(rule, ruleId))
                        .collect(Collectors.toList());
            } else {
                promptRules = Collections.emptyList();
            }

            reviewPrompt.setRules(promptRules);

            List<PromptFile> files = paths.stream()
                    .map(rfp -> mapFile(rfp, fileId, sourceFileCache))
                    .collect(Collectors.toList());
            reviewPrompt.setFiles(files);

            logger.info("Mapped ReviewPrompt with {} files and {} rules.", files.size(), promptRules.size());
        } else {
            logger.warn("No file paths provided. Setting empty file list on ReviewPrompt.");
            reviewPrompt.setFiles(Collections.emptyList());
        }

        logger.debug("ReviewPrompt mapping complete: {}", reviewPrompt);
        return reviewPrompt;
    }

    private PromptFile mapFile(ResolvedFilePath resolvedFilePath, AtomicLong fileId, Map<String, SourceFile> sourceFileCache) {
        logger.debug("Mapping PromptFile for path: {}", resolvedFilePath.getResolvedPath());
        final SourceFile sourceFile = getSourceFile(resolvedFilePath, sourceFileCache);
        long id = fileId.get();
        logger.trace("Assigning fileId {} to PromptFile", id);
        PromptFile pf = new PromptFile()
                .setId(fileId.getAndIncrement())
                .setResolvedFilePath(resolvedFilePath)
                .setSourceFile(sourceFile);
        logger.debug("Created PromptFile with id: {}", id);
        return pf;
    }

    private SourceFile getSourceFile(ResolvedFilePath resolvedFilePath, Map<String, SourceFile> sourceFileCache) {
        String path = resolvedFilePath.getResolvedPath();
        String codePage = getCodePage(resolvedFilePath);
        logger.debug("Getting SourceFile for path: {} (codePage: {})", path, codePage);

        final SourceFile sourceFile;
        if (!sourceFileCache.containsKey(path)) {
            logger.info("Cache miss for SourceFile path: {}. Reading from SourceFileReader.", path);
            sourceFile = sourceFileReader.readFile(path, codePage);
            if (sourceFile != null) {
                logger.debug("Read SourceFile for path: {} successfully.", path);
                sourceFileCache.put(path, sourceFile);
            } else {
                logger.warn("Failed to read SourceFile for path: {}", path);
            }
        } else {
            logger.debug("Cache hit for SourceFile path: {}", path);
            sourceFile = sourceFileCache.get(path);
        }
        return sourceFile;
    }

    private PromptRule mapRule(Rule rule, AtomicLong ruleId) {
        long id = ruleId.get();
        logger.trace("Mapping PromptRule with id {} and code {}", id, rule.getCode());
        PromptRule pr = new PromptRule()
                .setRuleKey(new RuleKey(ruleId.getAndIncrement(), rule.getCode()))
                .setDescription(rule.getDescription())
                .setSeverity(rule.getSeverity());
        logger.debug("Mapped PromptRule: {}", pr);
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
