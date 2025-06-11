package com.quasarbyte.llm.codereview.sdk.service.mapper.impl;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.SourceFileKey;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFileJson;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedFile;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedFileJson;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItem;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.ReviewedResultItemJson;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewedResultItemJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ReviewedResultItemJsonMapper for converting between ReviewedResultItem and ReviewedResultItemJson objects
 */
public class ReviewedResultItemJsonMapperImpl implements ReviewedResultItemJsonMapper {

    private static final Logger logger = LoggerFactory.getLogger(ReviewedResultItemJsonMapperImpl.class);

    @Override
    public ReviewedResultItemJson toJson(ReviewedResultItem reviewedResultItem) {
        if (reviewedResultItem == null) {
            return null;
        }
        
        logger.debug("Converting ReviewedResultItem to ReviewedResultItemJson");
        
        ReviewedResultItemJson json = new ReviewedResultItemJson();
        
        json.setFiles(mapReviewedFilesToJson(reviewedResultItem.getFiles()));
        json.setThinkSteps(copyList(reviewedResultItem.getThinkSteps()));
        json.setCompletionUsage(reviewedResultItem.getCompletionUsage());
        
        return json;
    }

    @Override
    public ReviewedResultItem fromJson(ReviewedResultItemJson reviewedResultItemJson) {
        if (reviewedResultItemJson == null) {
            return null;
        }
        
        logger.debug("Converting ReviewedResultItemJson to ReviewedResultItem");
        
        ReviewedResultItem reviewedResultItem = new ReviewedResultItem();
        
        reviewedResultItem.setFiles(mapReviewedFilesFromJson(reviewedResultItemJson.getFiles()));
        reviewedResultItem.setThinkSteps(copyList(reviewedResultItemJson.getThinkSteps()));
        reviewedResultItem.setCompletionUsage(reviewedResultItemJson.getCompletionUsage());
        
        return reviewedResultItem;
    }
    
    /**
     * Helper method to create a defensive copy of a list
     */
    private <T> List<T> copyList(List<T> source) {
        if (source == null) {
            return null;
        }
        return new ArrayList<>(source);
    }
    
    /**
     * Maps List<ReviewedFile> to List<ReviewedFileJson>
     */
    private List<ReviewedFileJson> mapReviewedFilesToJson(List<ReviewedFile> reviewedFiles) {
        if (reviewedFiles == null) {
            return null;
        }
        
        List<ReviewedFileJson> jsonFiles = new ArrayList<>();
        for (ReviewedFile reviewedFile : reviewedFiles) {
            if (reviewedFile != null) {
                ReviewedFileJson jsonFile = mapReviewedFileToJson(reviewedFile);
                jsonFiles.add(jsonFile);
            }
        }
        return jsonFiles;
    }
    
    /**
     * Maps List<ReviewedFileJson> to List<ReviewedFile>
     */
    private List<ReviewedFile> mapReviewedFilesFromJson(List<ReviewedFileJson> reviewedFileJsons) {
        if (reviewedFileJsons == null) {
            return null;
        }
        
        List<ReviewedFile> reviewedFiles = new ArrayList<>();
        for (ReviewedFileJson reviewedFileJson : reviewedFileJsons) {
            if (reviewedFileJson != null) {
                ReviewedFile reviewedFile = mapReviewedFileFromJson(reviewedFileJson);
                reviewedFiles.add(reviewedFile);
            }
        }
        return reviewedFiles;
    }
    
    /**
     * Maps individual ReviewedFile to ReviewedFileJson
     */
    private ReviewedFileJson mapReviewedFileToJson(ReviewedFile reviewedFile) {
        if (reviewedFile == null) {
            return null;
        }
        
        ReviewedFileJson reviewedFileJson = new ReviewedFileJson();
        reviewedFileJson.setPromptFile(mapPromptFileToJson(reviewedFile.getPromptFile()));
        reviewedFileJson.setComments(copyList(reviewedFile.getComments()));
        reviewedFileJson.setReviewedThinkSteps(copyList(reviewedFile.getReviewedThinkSteps()));
        
        return reviewedFileJson;
    }
    
    /**
     * Maps individual ReviewedFileJson to ReviewedFile
     */
    private ReviewedFile mapReviewedFileFromJson(ReviewedFileJson reviewedFileJson) {
        if (reviewedFileJson == null) {
            return null;
        }
        
        ReviewedFile reviewedFile = new ReviewedFile();
        reviewedFile.setPromptFile(mapPromptFileFromJson(reviewedFileJson.getPromptFile()));
        reviewedFile.setComments(copyList(reviewedFileJson.getComments()));
        reviewedFile.setReviewedThinkSteps(copyList(reviewedFileJson.getReviewedThinkSteps()));
        
        return reviewedFile;
    }
    
    /**
     * Maps individual PromptFile to PromptFileJson
     */
    private PromptFileJson mapPromptFileToJson(PromptFile promptFile) {
        if (promptFile == null) {
            return null;
        }
        
        PromptFileJson promptFileJson = new PromptFileJson();
        promptFileJson.setId(promptFile.getId());
        
        if (promptFile.getSourceFile() != null) {
            SourceFileKey sourceFileKey = new SourceFileKey();
            sourceFileKey.setFilePath(promptFile.getSourceFile().getFilePath());
            sourceFileKey.setCodePage(promptFile.getSourceFile().getCodePage());
            promptFileJson.setSourceFileKey(sourceFileKey);
        }
        
        return promptFileJson;
    }
    
    /**
     * Maps individual PromptFileJson to PromptFile
     */
    private PromptFile mapPromptFileFromJson(PromptFileJson promptFileJson) {
        if (promptFileJson == null) {
            return null;
        }
        
        PromptFile promptFile = new PromptFile();
        promptFile.setId(promptFileJson.getId());
        
        if (promptFileJson.getSourceFileKey() != null) {
            SourceFile sourceFile = new SourceFile();
            sourceFile.setFilePath(promptFileJson.getSourceFileKey().getFilePath());
            sourceFile.setCodePage(promptFileJson.getSourceFileKey().getCodePage());
            promptFile.setSourceFile(sourceFile);
        }
        
        return promptFile;
    }
}
