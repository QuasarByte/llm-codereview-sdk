package com.quasarbyte.llm.codereview.sdk.service.impl;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessage;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessageRoleEnum;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmMessages;
import com.quasarbyte.llm.codereview.sdk.model.llm.LlmReviewPrompt;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RhinoLlmMessagesMapperImplTest {

    @Test
    void testMap_withSimpleJsFunction_returnsMappedMessages() {
        // --- Arrange ---
        String jsScript =
                "function mapPromptToMessages(prompt) {" +
                        "  var messages = [];" +
                        "  (prompt.getSystemPromptTexts() || []).forEach(function(systemText) {" +
                        "    messages.push({role: 'system', content: 'System: ' + systemText});" +
                        "  });" +
                        "  (prompt.getReviewTargetPromptTexts() || []).forEach(function(userText) {" +
                        "    messages.push({role: 'user', content: 'User: ' + userText});" +
                        "  });" +
                        "  return messages;" +
                        "}";

        LlmMessagesMapperConfigurationRhino rhinoConfig = new LlmMessagesMapperConfigurationRhino()
                .setScriptBody(jsScript)
                .setFunctionName("mapPromptToMessages");

        // Prepare a minimal prompt object with MULTIPLE texts to test loops
        LlmReviewPrompt prompt = new LlmReviewPrompt();
        prompt.setSystemPromptTexts(Arrays.asList("SystemText1", "SystemText2"));
        prompt.setReviewTargetPromptTexts(Arrays.asList("UserText1", "UserText2"));

        // Service under test
        RhinoLlmMessagesMapperImpl service = new RhinoLlmMessagesMapperImpl();

        // --- Act ---
        LlmMessages llmMessages = service.map(prompt, rhinoConfig);

        // --- Assert ---
        assertNotNull(llmMessages);
        List<LlmMessage> messages = llmMessages.getMessages();
        assertNotNull(messages);
        assertEquals(4, messages.size());

        assertEquals(LlmMessageRoleEnum.SYSTEM, messages.get(0).getRole());
        assertEquals("System: SystemText1", messages.get(0).getContent());

        assertEquals(LlmMessageRoleEnum.SYSTEM, messages.get(1).getRole());
        assertEquals("System: SystemText2", messages.get(1).getContent());

        assertEquals(LlmMessageRoleEnum.USER, messages.get(2).getRole());
        assertEquals("User: UserText1", messages.get(2).getContent());

        assertEquals(LlmMessageRoleEnum.USER, messages.get(3).getRole());
        assertEquals("User: UserText2", messages.get(3).getContent());
    }
}
