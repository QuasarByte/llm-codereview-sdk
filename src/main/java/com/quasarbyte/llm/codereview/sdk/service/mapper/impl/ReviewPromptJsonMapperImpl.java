package com.quasarbyte.llm.codereview.sdk.service.mapper.impl;

import com.quasarbyte.llm.codereview.sdk.model.SourceFile;
import com.quasarbyte.llm.codereview.sdk.model.SourceFileKey;
import com.quasarbyte.llm.codereview.sdk.model.prompt.*;
import com.quasarbyte.llm.codereview.sdk.service.mapper.ReviewPromptJsonMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ReviewPromptJsonMapper for converting between ReviewPrompt and ReviewPromptJson objects
 */
public class ReviewPromptJsonMapperImpl implements ReviewPromptJsonMapper {
    
    @Override
    public ReviewPromptJson toJson(ReviewPrompt reviewPrompt) {
        if (reviewPrompt == null) {
            return null;
        }
        
        ReviewPromptJson json = new ReviewPromptJson();
        
        json.setFileGroupPromptTexts(copyList(reviewPrompt.getFileGroupPromptTexts()));
        json.setReviewTargetPromptTexts(copyList(reviewPrompt.getReviewTargetPromptTexts()));
        json.setReviewPromptTexts(copyList(reviewPrompt.getReviewPromptTexts()));
        json.setSystemPromptTexts(copyList(reviewPrompt.getSystemPromptTexts()));
        json.setRules(copyList(reviewPrompt.getRules()));
        json.setFiles(mapPromptFilesToJson(reviewPrompt.getFiles()));
        json.setUseReasoning(reviewPrompt.getUseReasoning());
        
        return json;
    }
    
    @Override
    public ReviewPrompt fromJson(ReviewPromptJson reviewPromptJson) {
        if (reviewPromptJson == null) {
            return null;
        }
        
        ReviewPrompt reviewPrompt = new ReviewPrompt();
        
        reviewPrompt.setFileGroupPromptTexts(copyList(reviewPromptJson.getFileGroupPromptTexts()));
        reviewPrompt.setReviewTargetPromptTexts(copyList(reviewPromptJson.getReviewTargetPromptTexts()));
        reviewPrompt.setReviewPromptTexts(copyList(reviewPromptJson.getReviewPromptTexts()));
        reviewPrompt.setSystemPromptTexts(copyList(reviewPromptJson.getSystemPromptTexts()));
        reviewPrompt.setRules(copyList(reviewPromptJson.getRules()));
        reviewPrompt.setFiles(mapPromptFilesFromJson(reviewPromptJson.getFiles()));
        reviewPrompt.setUseReasoning(reviewPromptJson.getUseReasoning());
        
        return reviewPrompt;
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
     * Maps List<PromptFile> to List<PromptFileJson>
     */
    private List<PromptFileJson> mapPromptFilesToJson(List<PromptFile> promptFiles) {
        if (promptFiles == null) {
            return null;
        }
        
        List<PromptFileJson> jsonFiles = new ArrayList<>();
        for (PromptFile promptFile : promptFiles) {
            if (promptFile != null) {
                PromptFileJson jsonFile = mapPromptFileToJson(promptFile);
                jsonFiles.add(jsonFile);
            }
        }
        return jsonFiles;
    }
    
    /**
     * Maps List<PromptFileJson> to List<PromptFile>
     */
    private List<PromptFile> mapPromptFilesFromJson(List<PromptFileJson> promptFileJsons) {
        if (promptFileJsons == null) {
            return null;
        }
        
        List<PromptFile> promptFiles = new ArrayList<>();
        for (PromptFileJson promptFileJson : promptFileJsons) {
            if (promptFileJson != null) {
                PromptFile promptFile = mapPromptFileFromJson(promptFileJson);
                promptFiles.add(promptFile);
            }
        }
        return promptFiles;
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
