package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;
import com.quasarbyte.llm.codereview.sdk.model.resolved.*;
import com.quasarbyte.llm.codereview.sdk.service.ReviewConfigurationResolver;
import com.quasarbyte.llm.codereview.sdk.service.TargetResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReviewConfigurationResolverImpl implements ReviewConfigurationResolver {

    private static final Logger logger = LoggerFactory.getLogger(ReviewConfigurationResolverImpl.class);

    private final TargetResolverService targetResolverService;

    public ReviewConfigurationResolverImpl(TargetResolverService targetResolverService) {
        this.targetResolverService = targetResolverService;
        logger.debug("ReviewConfigurationResolverImpl initialized with TargetResolverService: {}", targetResolverService);
    }

    @Override
    public ResolvedReviewConfiguration resolve(ReviewParameter reviewParameter) {
        logger.info("Resolving review configuration for ReviewParameter: {}", reviewParameter);

        Objects.requireNonNull(reviewParameter, "ReviewParameter must not be null");
        Objects.requireNonNull(reviewParameter.getTargets(), "Targets must not be null in ReviewParameter");

        ResolvedReviewConfiguration resolvedReviewConfiguration = new ResolvedReviewConfiguration()
                .setReviewConfiguration(reviewParameter);

        List<ReviewTarget> targets = reviewParameter.getTargets();
        logger.debug("Found {} targets to resolve.", targets.size());

        List<ResolvedReviewTarget> resolvedReviewTargets = targets
                .stream()
                .map(target -> resolveReviewTarget(target, resolvedReviewConfiguration))
                .collect(Collectors.toList());

        resolvedReviewConfiguration.setResolvedReviewTargets(resolvedReviewTargets);

        logger.info("Resolved {} review targets.", resolvedReviewTargets.size());
        return resolvedReviewConfiguration;
    }

    private ResolvedReviewTarget resolveReviewTarget(
            ReviewTarget reviewTarget,
            ResolvedReviewConfiguration resolvedReviewConfiguration) {

        logger.debug("Resolving ReviewTarget: {}", reviewTarget);
        Objects.requireNonNull(reviewTarget, "ReviewTarget must not be null");

        ResolvedReviewTarget resolvedReviewTarget = new ResolvedReviewTarget();

        List<FileGroup> fileGroups = reviewTarget.getFileGroups();
        logger.debug("ReviewTarget has {} file groups.", fileGroups != null ? fileGroups.size() : 0);

        List<ResolvedFileGroup> resolvedFileGroups = fileGroups == null
                ? Collections.emptyList()
                : fileGroups.stream()
                .map(fileGroup -> resolveFileGroup(fileGroup, resolvedReviewTarget))
                .collect(Collectors.toList());

        resolvedReviewTarget
                .setResolvedReviewConfiguration(resolvedReviewConfiguration)
                .setReviewTarget(reviewTarget)
                .setResolvedFileGroups(resolvedFileGroups);

        logger.debug("Resolved ReviewTarget with {} file groups.", resolvedFileGroups.size());
        return resolvedReviewTarget;
    }

    private ResolvedFileGroup resolveFileGroup(
            FileGroup fileGroup,
            ResolvedReviewTarget resolvedReviewTarget) {
        logger.debug("Resolving FileGroup: {}", fileGroup);

        ResolvedFileGroup resolvedFileGroup = new ResolvedFileGroup();

        List<String> paths = fileGroup.getPaths();
        logger.debug("FileGroup has {} paths.", paths != null ? paths.size() : 0);

        List<ResolvedFileGroupPath> resolvedFileGroupPaths = paths == null
                ? Collections.emptyList()
                : paths.stream()
                .map(path -> resolveFileGroupPath(path, resolvedFileGroup))
                .collect(Collectors.toList());

        resolvedFileGroup
                .setFileGroup(fileGroup)
                .setResolvedReviewTarget(resolvedReviewTarget)
                .setResolvedFileGroupPaths(resolvedFileGroupPaths);

        logger.debug("Resolved FileGroup with {} paths.", resolvedFileGroupPaths.size());
        return resolvedFileGroup;
    }

    private ResolvedFileGroupPath resolveFileGroupPath(
            String path,
            ResolvedFileGroup resolvedFileGroup) {
        logger.debug("Resolving FileGroupPath for path: {}", path);

        ResolvedFileGroupPath resolvedFileGroupPath = new ResolvedFileGroupPath();

        List<String> resolvedPathStrings = targetResolverService.resolve(path);
        logger.debug("TargetResolverService resolved {} file(s) for path '{}'.", resolvedPathStrings.size(), path);

        List<ResolvedFilePath> resolvedPaths = resolvedPathStrings
                .stream()
                .map(resolvedPathStr -> new ResolvedFilePath()
                        .setResolvedFileGroupPath(resolvedFileGroupPath)
                        .setResolvedPath(resolvedPathStr))
                .collect(Collectors.toList());

        resolvedFileGroupPath
                .setResolvedFileGroup(resolvedFileGroup)
                .setPath(path)
                .setResolvedPaths(resolvedPaths);

        logger.debug("ResolvedFileGroupPath for '{}' contains {} file(s).", path, resolvedPaths.size());
        return resolvedFileGroupPath;
    }
}
