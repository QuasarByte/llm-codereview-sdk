package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFileGroup;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFileGroupPath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedFilePath;
import com.quasarbyte.llm.codereview.sdk.model.resolved.ResolvedReviewTarget;
import com.quasarbyte.llm.codereview.sdk.service.ResolvedFilePathSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResolvedFilePathSplitterImpl implements ResolvedFilePathSplitter {

    private static final Logger logger = LoggerFactory.getLogger(ResolvedFilePathSplitterImpl.class);

    @Override
    public List<List<ResolvedFilePath>> split(List<ResolvedReviewTarget> targets) {
        logger.info("Entering split() with {} targets", targets == null ? 0 : targets.size());
        if (targets == null) {
            logger.warn("Received null targets list");
            return Collections.emptyList();
        }
        List<ResolvedFileGroup> resolvedFileGroups = targets
                .stream()
                .map(ResolvedReviewTarget::getResolvedFileGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        logger.debug("Collected {} resolved file groups from targets", resolvedFileGroups.size());

        List<List<ResolvedFilePath>> result = createPrompts(resolvedFileGroups);
        logger.info("Exiting split(), returning {} prompt batches", result.size());
        return result;
    }

    private static List<List<ResolvedFilePath>> createPrompts(List<ResolvedFileGroup> resolvedFileGroups) {
        logger.debug("Creating prompts for {} file groups", resolvedFileGroups.size());
        List<List<ResolvedFilePath>> prompts = resolvedFileGroups.stream()
                .flatMap(ResolvedFilePathSplitterImpl::createPromptsForFileGroup)
                .collect(Collectors.toList());
        logger.debug("Created {} prompt batches", prompts.size());
        return prompts;
    }

    private static Stream<List<ResolvedFilePath>> createPromptsForFileGroup(ResolvedFileGroup resolvedFileGroup) {
        Integer batchSizeObj = resolvedFileGroup.getFileGroup().getFilesBatchSize();
        int filesBatchSize = (batchSizeObj == null) ? 0 : batchSizeObj;
        logger.debug("File group batch size: {}", filesBatchSize);

        // Collect all resolved file paths sorted
        List<ResolvedFilePath> allResolvedPaths = resolvedFileGroup.getResolvedFileGroupPaths()
                .stream()
                .map(ResolvedFileGroupPath::getResolvedPaths)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ResolvedFilePath::getResolvedPath))
                .collect(Collectors.toList());

        logger.debug("Resolved file group contains {} files", allResolvedPaths.size());

        if (filesBatchSize <= 0 || filesBatchSize >= allResolvedPaths.size()) {
            if (filesBatchSize <= 0) {
                logger.info("Batch size is zero or negative ({}); returning all paths as a single batch", filesBatchSize);
            } else {
                logger.info("Batch size ({}) >= total files ({}); returning all paths as a single batch", filesBatchSize, allResolvedPaths.size());
            }
            // One prompt for all resolved paths
            return Stream.of(allResolvedPaths);
        } else {
            // Split resolved paths into batches of filesBatchSize
            logger.info("Splitting {} files into batches of {}", allResolvedPaths.size(), filesBatchSize);
            List<List<ResolvedFilePath>> batches = new ArrayList<>();
            for (int i = 0; i < allResolvedPaths.size(); i += filesBatchSize) {
                List<ResolvedFilePath> batch = allResolvedPaths.subList(i, Math.min(i + filesBatchSize, allResolvedPaths.size()));
                logger.debug("Created batch with files {} to {} (total {})", i, Math.min(i + filesBatchSize, allResolvedPaths.size()) - 1, batch.size());
                batches.add(batch);
            }
            return batches.stream();
        }
    }
}
