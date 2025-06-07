package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletion;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.openai.models.completions.CompletionUsage;
import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewException;
import com.quasarbyte.llm.codereview.sdk.model.FileKey;
import com.quasarbyte.llm.codereview.sdk.model.RuleKey;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.llm.*;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptFile;
import com.quasarbyte.llm.codereview.sdk.model.prompt.PromptRule;
import com.quasarbyte.llm.codereview.sdk.model.prompt.ReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.model.reviewed.*;
import com.quasarbyte.llm.codereview.sdk.service.ChatCompletionCreateParamsFactory;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessagesMapper;
import com.quasarbyte.llm.codereview.sdk.service.LlmReviewProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LlmReviewProcessorImpl implements LlmReviewProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LlmReviewProcessorImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final LlmMessagesMapper llmMessagesMapper;
    private final ChatCompletionCreateParamsFactory chatCompletionCreateParamsFactory;

    public LlmReviewProcessorImpl(LlmMessagesMapper llmMessagesMapper,
                                  ChatCompletionCreateParamsFactory chatCompletionCreateParamsFactory) {
        this.llmMessagesMapper = llmMessagesMapper;
        this.chatCompletionCreateParamsFactory = chatCompletionCreateParamsFactory;
    }

    @Override
    public ReviewedResultItem process(ReviewPrompt prompt,
                                      LlmChatCompletionConfiguration llmChatCompletionConfiguration,
                                      LlmMessagesMapperConfiguration messagesMapperConfiguration,
                                      LlmClient llmClient) {

        logger.info("Processing review prompt. Files: {}, Rules: {}",
                prompt.getFiles() != null ? prompt.getFiles().size() : 0,
                prompt.getRules() != null ? prompt.getRules().size() : 0);

        List<PromptFile> promptFiles = prompt.getFiles() != null ? prompt.getFiles() : Collections.emptyList();

        // Build LlmReviewPrompt (log details for debug)
        LlmReviewPrompt llmReviewPrompt = new LlmReviewPrompt()
                .setSystemPromptTexts(prompt.getSystemPromptTexts())
                .setReviewPromptTexts(prompt.getReviewPromptTexts())
                .setRules(prompt.getRules())
                .setReviewTargetPromptTexts(prompt.getReviewTargetPromptTexts())
                .setFileGroupPromptTexts(prompt.getFileGroupPromptTexts())
                .setFiles(promptFiles
                        .stream()
                        .map(file -> new LlmFile()
                                .setId(file.getId())
                                .setMetadata(new LlmFileMetadata()
                                        .setFileName(file.getSourceFile().getFileName())
                                        .setFileNameExtension(file.getSourceFile().getFileNameExtension())
                                        .setFilePath(file.getSourceFile().getFilePath())
                                        .setFileSize(file.getSourceFile().getSize())
                                        .setAccessedAt(file.getSourceFile().getAccessedAt())
                                        .setCreatedAt(file.getSourceFile().getCreatedAt())
                                        .setModifiedAt(file.getSourceFile().getModifiedAt())
                                )
                                .setContent(new String(file.getSourceFile().getContent(), getCharset(file))))
                        .collect(toList()));

        logger.debug("Mapped LlmReviewPrompt: files = {}, rules = {}",
                llmReviewPrompt.getFiles() != null ? llmReviewPrompt.getFiles().size() : 0,
                llmReviewPrompt.getRules() != null ? llmReviewPrompt.getRules().size() : 0);

        LlmMessages llmMessages = llmMessagesMapper.map(llmReviewPrompt, messagesMapperConfiguration);

        logger.debug("Mapped {} LLM messages for chat completion.", llmMessages.getMessages().size());

        if (llmMessages.getMessages().isEmpty()) {
            logger.info("No LLM messages to process. Returning empty result.");
            return new ReviewedResultItem()
                    .setExecutionDetailsItem(new ReviewedExecutionDetailsItem()
                            .setPrompt(prompt)
                            .setLlmMessages(llmMessages))
                    .setFiles(Collections.emptyList());
        }

        final ChatCompletionCreateParams.Builder chatCompletionCreateParamsBuilder = chatCompletionCreateParamsFactory.create(llmChatCompletionConfiguration);

        llmMessages.getMessages()
                .forEach(llmMessage -> {
                    if (LlmMessageRoleEnum.SYSTEM.equals(llmMessage.getRole())) {
                        chatCompletionCreateParamsBuilder.addSystemMessage(llmMessage.getContent());
                    } else if (LlmMessageRoleEnum.USER.equals(llmMessage.getRole())) {
                        chatCompletionCreateParamsBuilder.addUserMessage(llmMessage.getContent());
                    } else {
                        logger.error("Unknown LLM message role encountered: {}", llmMessage.getRole());
                        throw new LLMCodeReviewException("Unknown role: " + llmMessage.getRole());
                    }
                });

        StructuredChatCompletionCreateParams<LlmReviewResult> params = chatCompletionCreateParamsBuilder.responseFormat(LlmReviewResult.class)
                .build();

        OpenAIClient openAIClient = llmClient.getOpenAIClient();

        try {
            logger.info("Calling LLM Provider chat completion API.");
            StructuredChatCompletion<LlmReviewResult> chatCompletion = openAIClient
                    .chat()
                    .completions()
                    .create(params);

            logger.info("Calling LLM Provider chat completion API.");

            logger.debug("Received {} chat completion choices(s) from LLM Provider.", chatCompletion.choices().size());

            List<LlmReviewResult> llmReviewResults = chatCompletion
                    .choices()
                    .stream()
                    .map(choice -> choice.message().content())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());

            logger.debug("Received {} review result(s) from LLM Provider.", llmReviewResults.size());

            List<LlmReviewedFile> llmReviewedFiles = llmReviewResults
                    .stream()
                    .map(LlmReviewResult::getFiles)
                    .flatMap(Collection::stream)
                    .collect(toList());

            if (llmReviewedFiles.isEmpty()) {
                logger.info("No reviewed files in LLM Provider response.");
                return new ReviewedResultItem()
                        .setExecutionDetailsItem(new ReviewedExecutionDetailsItem()
                                .setPrompt(prompt)
                                .setLlmMessages(llmMessages))
                        .setFiles(Collections.emptyList());
            }

            List<LlmReviewedFile> mergedFiles = mergeFiles(llmReviewedFiles);

            logger.debug("Merged reviewed files: count = {}", mergedFiles.size());

            Map<RuleKey, PromptRule> ruleMap = prompt.getRules()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            PromptRule::getRuleKey,
                            rule -> rule,
                            (r1, r2) -> r2 // if codes are the same, take the last one
                    ));

            Map<FileKey, PromptFile> fileMap = prompt.getFiles()
                    .stream()
                    .collect(Collectors.toMap(
                            (PromptFile file) -> new FileKey(file.getId(), file.getSourceFile().getFileName()),
                            file -> file,
                            (fileOne, fileTwo) -> fileTwo // if keys are the same, take the last one
                    ));

            List<ReviewedFile> reviewedFiles = mergedFiles.stream()
                    .map(file -> new ReviewedFile()
                            .setPromptFile(fileMap.get(new FileKey(file.getFileId(), file.getFileName())))
                            .setComments(file.getComments().stream().map(comment -> new ReviewedComment()
                                            .setRule(ruleMap.get(new RuleKey(comment.getRuleId(), comment.getRuleCode())))
                                            .setLine(comment.getLine())
                                            .setColumn(comment.getColumn())
                                            .setMessage(comment.getMessage())
                                            .setSuggestion(comment.getSuggestion()))
                                    .collect(toList())
                            )
                    )
                    .collect(toList());

            Optional<CompletionUsage> completionUsageOptional = chatCompletion.usage();

            completionUsageOptional.ifPresent(usage -> logger.info(
                    "LLM Provider token usage - prompt: {}, completion: {}, total: {}",
                    usage.promptTokens(),
                    usage.completionTokens(),
                    usage.totalTokens()
            ));

            logger.info("Processed review prompt successfully. Reviewed files: {}", reviewedFiles.size());

            ReviewedResultItem reviewedResultItem = new ReviewedResultItem()
                    .setExecutionDetailsItem(new ReviewedExecutionDetailsItem()
                            .setPrompt(prompt)
                            .setLlmMessages(llmMessages)
                            .setLlmReviewResults(llmReviewResults))
                    .setFiles(reviewedFiles)
                    .setCompletionUsage(completionUsageOptional
                            .map(completionUsage -> new ReviewedCompletionUsage()
                                    .setCompletionTokens(completionUsage.completionTokens())
                                    .setPromptTokens(completionUsage.promptTokens())
                                    .setTotalTokens(completionUsage.totalTokens()))
                            .orElse(null));
            
            if (logger.isDebugEnabled()) {
                try {
                    String reviewedResultItemJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reviewedResultItem);
                    logger.debug("llmReviewResults received from chat completion:\n{}", reviewedResultItemJson);
                } catch (JsonProcessingException e) {
                    logger.trace("Failed to serialize llmReviewResults to JSON", e);
                }
            }
            
            return reviewedResultItem;
        } catch (Exception e) {
            logger.error("LLM review processing failed: {}", e.getMessage(), e);
            throw new LLMCodeReviewException("Failed to process review prompt: " + e.getMessage(), e);
        }
    }

    private static List<LlmReviewedFile> mergeFiles(List<LlmReviewedFile> files) {
        Objects.requireNonNull(files);

        if (logger.isDebugEnabled()) {
            logger.debug("Merging {} reviewed file(s) into distinct file entries.", files.size());
        }
        Map<FileKey, List<LlmReviewedFile>> grouped = files.stream()
                .collect(Collectors.groupingBy(file -> new FileKey(file.getFileId(), file.getFileName())));

        List<LlmReviewedFile> merged = grouped
                .entrySet()
                .stream()
                .map(entry -> {
                    FileKey key = entry.getKey();
                    List<LlmReviewedFile> group = entry.getValue();
                    List<LlmReviewComment> allComments = group.stream()
                            .flatMap(f -> f.getComments() == null ? Stream.empty() : f.getComments().stream())
                            .collect(toList());

                    List<LlmReviewComment> distinctComments = getDistinctComments(allComments);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Merged fileId: {}, fileName: {}, comments: {} (from {} group items)",
                                key.getId(), key.getName(),
                                distinctComments.size(), group.size());
                    }

                    return new LlmReviewedFile()
                            .setFileId(key.getId())
                            .setFileName(key.getName())
                            .setComments(distinctComments);
                })
                .collect(toList());

        if (logger.isDebugEnabled()) {
            logger.debug("mergeFiles completed. Total merged files: {}", merged.size());
        }
        return merged;
    }

    private static List<LlmReviewComment> getDistinctComments(List<LlmReviewComment> comments) {

        if (logger.isDebugEnabled()) {
            logger.debug("Deduplicating comments. Input count: {}", comments != null ? comments.size() : 0);
        }

        if (comments == null) {
            return Collections.emptyList();
        }

        Set<LlmCommentKey> seen = new HashSet<>();
        List<LlmReviewComment> result = new ArrayList<>();

        for (LlmReviewComment c : comments) {
            if (c != null) {
                LlmCommentKey key = new LlmCommentKey(c);
                if (seen.add(key)) {
                    result.add(c);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Duplicate comment skipped: {}", key);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Null comment encountered and skipped.");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Deduplication complete. Distinct comments count: {}", result.size());
        }

        return result;
    }

    private static Charset getCharset(PromptFile file) {
        String codePage = file.getSourceFile().getCodePage();
        return codePage != null && !codePage.trim().isEmpty() ? Charset.forName(codePage) : StandardCharsets.UTF_8;
    }
}
