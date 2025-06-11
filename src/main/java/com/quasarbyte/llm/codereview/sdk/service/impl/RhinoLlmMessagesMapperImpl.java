package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.sdk.exception.LLMCodeReviewRhinoException;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessage;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessageRoleEnum;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessages;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmReviewPrompt;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessagesMapper;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps LlmReviewPrompt to LlmMessages using a custom Rhino JavaScript mapping function.
 * Throws LLMCodeReviewException for script, type, or mapping errors.
 */
public class RhinoLlmMessagesMapperImpl implements LlmMessagesMapper {

    private static final String FIELD_NAME_ROLE = "role";
    private static final String FIELD_NAME_CONTENT = "content";

    private static final Logger logger = LoggerFactory.getLogger(RhinoLlmMessagesMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LlmMessages map(LlmReviewPrompt prompt, LlmMessagesMapperConfiguration configuration) {
        Objects.requireNonNull(prompt);
        Objects.requireNonNull(configuration);

        logger.debug("Mapping LlmReviewPrompt to LlmMessages using Rhino JS. Prompt: {}", prompt);
        logger.debug("LlmMessagesMapperConfiguration type: {}", configuration.getClass().getSimpleName());

        final LlmMessagesMapperConfigurationRhino llmMessagesMapperConfigurationRhino;

        if ((configuration instanceof LlmMessagesMapperConfigurationRhino)) {
            llmMessagesMapperConfigurationRhino = (LlmMessagesMapperConfigurationRhino) configuration;
        } else {
            logger.error("Configuration parameter should be of type LlmMessagesMapperConfigurationRhino, but got: {}", configuration.getClass().getName());
            throw new LLMCodeReviewRhinoException(String.format(
                    "configuration parameter should be of type %s",
                    LlmMessagesMapperConfigurationRhino.class.getName()
            ));
        }

        Objects.requireNonNull(llmMessagesMapperConfigurationRhino);
        String jsScript = llmMessagesMapperConfigurationRhino.getScriptBody();

        String functionName = llmMessagesMapperConfigurationRhino.getFunctionName();

        if (jsScript == null || jsScript.isEmpty()) {
            logger.error("Rhino script body must not be null or empty");
            throw new LLMCodeReviewRhinoException("Rhino script body must not be null or empty");
        }

        if (functionName == null || functionName.isEmpty()) {
            logger.error("Rhino function name must not be null or empty");
            throw new LLMCodeReviewRhinoException("Rhino function name must not be null or empty");
        }

        logger.debug("Rhino script body size: {} characters", jsScript.length());
        logger.trace("Script body:\n {}", jsScript);

        logger.debug("Rhino JS function to call: {}", functionName);

        try (Context ctx = Context.enter()) {

            ctx.setLanguageVersion(Context.VERSION_ES6);

            Scriptable scope = ctx.initStandardObjects();
            logger.debug("Initialized Rhino standard objects.");

            ctx.evaluateString(scope, jsScript, "rhino-llm-messages-mapper.js", 1, null);
            logger.debug("Rhino JS script evaluated successfully.");

            // Get the JS function by name
            Object jsFunctionObj = scope.get(functionName, scope);
            if (!(jsFunctionObj instanceof Function)) {
                logger.error("No function '{}' found in JS script", functionName);
                throw new LLMCodeReviewRhinoException(String.format("No function '%s' found in JS script", functionName));
            }
            Function jsFunction = (Function) jsFunctionObj;
            logger.debug("Located JS function: {}", functionName);

            // Pass the prompt to JS and call the function
            Object jsPrompt = Context.javaToJS(prompt, scope);
            logger.debug("Converted LlmReviewPrompt to JS object.");

            Object jsResult = jsFunction.call(ctx, scope, scope, new Object[]{jsPrompt});
            logger.debug("Called JS function '{}' successfully.", functionName);

            if (!(jsResult instanceof NativeArray)) {
                logger.error("JS function did not return an array of messages, got: {}", jsResult != null ? jsResult.getClass().getSimpleName() : "null");
                throw new LLMCodeReviewRhinoException("JS function did not return an array of messages");
            }
            NativeArray jsMessages = (NativeArray) jsResult;

            // Convert JS array to Java objects
            List<LlmMessage> result = new ArrayList<>();

            long lengthLong = jsMessages.getLength();
            logger.debug("JS function returned array of length: {}", lengthLong);

            if (lengthLong > Integer.MAX_VALUE) {
                logger.error("JS function returned too many messages: {} (max allowed: {})", lengthLong, Integer.MAX_VALUE);
                throw new LLMCodeReviewRhinoException(String.format("JS function returned too many messages: %d (max allowed: %d)", lengthLong, Integer.MAX_VALUE));
            }

            int length = (int) lengthLong;

            for (int i = 0; i < length; i++) {
                try {
                    Scriptable jsMsg = (Scriptable) jsMessages.get(i, jsMessages);
                    String role = getRequiredStringField(jsMsg, FIELD_NAME_ROLE);
                    String content = getRequiredStringField(jsMsg, FIELD_NAME_CONTENT);
                    result.add(new LlmMessage().setRole(LlmMessageRoleEnum.fromRole(role)).setContent(content));
                    logger.trace("Parsed message [{}]: role={}, content size={}", i, role, content != null ? content.length() : 0);
                } catch (Exception e) {
                    logger.error("Cannot handle message with index {}. Error: {}", i, e.getMessage(), e);
                    throw new LLMCodeReviewRhinoException(String.format("Can not handle message with index %d. Error message: %s", i, e.getMessage()), e);
                }
            }

            List<LlmMessage> filteredMessages = result.stream()
                    .filter(llmMessage -> llmMessage.getContent() != null)
                    .filter(llmMessage -> !llmMessage.getContent().trim().isEmpty())
                    .collect(Collectors.toList());

            logger.debug("Filtered messages. Total: {}, After filter: {}", result.size(), filteredMessages.size());

            LlmMessages llmMessages = new LlmMessages().setMessages(filteredMessages);

            logger.debug("Returning LlmMessages with {} message(s)", llmMessages.getMessages() != null ? llmMessages.getMessages().size() : 0);

            if (logger.isTraceEnabled()) {
                try {
                    String llmMessagesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(llmMessages);
                    logger.trace("llmMessages mapped for chat completion: \n{}", llmMessagesJson);
                } catch (JsonProcessingException e) {
                    logger.trace("Failed to serialize llmMessages to JSON", e);
                }
            }

            return llmMessages;

        } catch (Exception e) {
            if (e instanceof LLMCodeReviewRhinoException) {
                logger.error("LLMCodeReviewRhinoException: {}", e.getMessage(), e);
                throw e;
            } else {
                logger.error("Error during mapping LlmReviewPrompt via Rhino Javascript engine: {}", e.getMessage(), e);
                throw new LLMCodeReviewRhinoException("Error during mapping LlmReviewPrompt via Rhino Javascript engine", e);
            }
        }
    }

    private static String getRequiredStringField(Scriptable obj, String fieldName) {
        Object value = obj.get(fieldName, obj);

        if (Scriptable.NOT_FOUND.equals(value)) {
            logger.error("Field '{}' not found in Messages JS execution result", fieldName);
            throw new LLMCodeReviewRhinoException(String.format("Field '%s' not found in Messages JS execution result", fieldName));
        }

        if (value instanceof CharSequence) {
            String stringValue = ((CharSequence) value).toString();
            logger.trace("Returning CharSequence: '{}'", stringValue);
            return stringValue;
        } else if (value instanceof NativeJavaObject) {
            String stringValue = ((NativeJavaObject) value).getDefaultValue(ScriptRuntime.StringClass).toString();
            logger.trace("Returning NativeJavaObject: '{}'", stringValue);
            return stringValue;
        } else if (value == null) {
            logger.trace("Field '{}' is null in Messages JS execution result", fieldName);
            return null;
        } else {
            logger.error("Unknown field type for '{}'. Expected string value. Field class: '{}', Field value: '{}'.",
                    fieldName, value.getClass().getSimpleName(), value);
            throw new LLMCodeReviewRhinoException(String.format(
                    "Unknown field type. Expected string value. Field name: '%s', Field class: '%s', Field value: '%s'.",
                    fieldName, value.getClass().getSimpleName(), value));
        }
    }
}
