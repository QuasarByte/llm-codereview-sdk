package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedCompletionUsage;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;
import com.quasarbyte.llm.codereview.sdk.model.aggregated.AggregatedResult;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.*;
import com.quasarbyte.llm.codereview.sdk.service.ReviewResultAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ReviewResultAggregatorImpl implements ReviewResultAggregator {

    private static final Logger logger = LoggerFactory.getLogger(ReviewResultAggregatorImpl.class);

    @Override
    public AggregatedResult aggregate(ReviewedDetailedResult result) {
        logger.info("Aggregating ReviewedDetailedResult: {} reviewed files", result.getFiles() != null ? result.getFiles().size() : 0);
        List<ReviewedFile> unhandledFiles = new ArrayList<>();

        Map<String, AggregatedFile> fileMap = new HashMap<>();

        result.getFiles().forEach(reviewedFile -> {
            if (reviewedFile != null) {
                PromptFile promptFile = reviewedFile.getPromptFile();

                if (promptFile != null) {

                    SourceFile sourceFile = promptFile.getSourceFile();

                    if (sourceFile != null) {

                        String filePath = sourceFile.getFilePath();

                        if (!fileMap.containsKey(filePath)) {
                            AggregatedFile newAggregatedFile = new AggregatedFile()
                                    .setSourceFile(sourceFile)
                                    .setComments(getComments(reviewedFile));
                            fileMap.put(filePath, newAggregatedFile);
                            logger.debug("Added new AggregatedFile for path: {}", filePath);
                        } else {
                            if (reviewedFile.getComments() != null && !reviewedFile.getComments().isEmpty()) {
                                fileMap.get(filePath).getComments().addAll(reviewedFile.getComments());
                                logger.debug("Added {} additional comments to existing AggregatedFile for path: {}",
                                        reviewedFile.getComments().size(), filePath);
                            }
                        }
                    } else {
                        unhandledFiles.add(reviewedFile);
                        logger.warn("SourceFile is null in PromptFile, adding to unhandledFiles.");
                    }

                } else {
                    logger.warn("PromptFile is null in ReviewedFile, skipping file.");
                }

            } else {
                logger.warn("ReviewedFile is null, skipping entry.");
            }
        });

        ReviewedCompletionUsage completionUsage = getCompletionUsage(result);
        logger.debug("Aggregated completion usage: completionTokens={}, promptTokens={}, totalTokens={}",
                completionUsage.getCompletionTokens(), completionUsage.getPromptTokens(), completionUsage.getTotalTokens());

        AggregatedCompletionUsage usage = new AggregatedCompletionUsage()
                .setCompletionTokens(completionUsage.getCompletionTokens())
                .setPromptTokens(completionUsage.getPromptTokens())
                .setTotalTokens(completionUsage.getTotalTokens());

        logger.info("Aggregation complete: {} aggregated files, {} unhandled files.",
                fileMap.size(), unhandledFiles.size());

        return new AggregatedResult()
                .setFiles(new ArrayList<>(fileMap.values()))
                .setUnhandledFiles(unhandledFiles)
                .setCompletionUsage(usage);
    }

    private static ReviewedCompletionUsage getCompletionUsage(ReviewedDetailedResult result) {
        ReviewedCompletionUsage usage = Optional.of(result)
                .map(ReviewedDetailedResult::getExecutionDetails)
                .map(ReviewedExecutionDetails::getReviewResultItems)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ReviewedResultItem::getCompletionUsage)
                .filter(Objects::nonNull)
                .reduce(
                        new ReviewedCompletionUsage()
                                .setCompletionTokens(0L)
                                .setPromptTokens(0L)
                                .setTotalTokens(0L),
                        (agg, u) -> {
                            agg.setCompletionTokens(
                                    agg.getCompletionTokens() + (u.getCompletionTokens() != null ? u.getCompletionTokens() : 0L)
                            );
                            agg.setPromptTokens(
                                    agg.getPromptTokens() + (u.getPromptTokens() != null ? u.getPromptTokens() : 0L)
                            );
                            agg.setTotalTokens(
                                    agg.getTotalTokens() + (u.getTotalTokens() != null ? u.getTotalTokens() : 0L)
                            );
                            return agg;
                        }
                );
        logger.trace("Reduced completion usage to: completionTokens={}, promptTokens={}, totalTokens={}",
                usage.getCompletionTokens(), usage.getPromptTokens(), usage.getTotalTokens());
        return usage;
    }

    private static List<ReviewedComment> getComments(ReviewedFile reviewedFile) {
        if (reviewedFile == null || reviewedFile.getComments() == null || reviewedFile.getComments().isEmpty()) {
            logger.debug("No comments found for ReviewedFile.");
            return Collections.emptyList();
        } else {
            List<ReviewedComment> comments = reviewedFile
                    .getComments()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            logger.debug("Retrieved {} comments from ReviewedFile.", comments.size());
            return comments;
        }
    }
}
