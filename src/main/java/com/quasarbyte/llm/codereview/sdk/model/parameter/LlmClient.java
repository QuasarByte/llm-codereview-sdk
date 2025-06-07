package com.quasarbyte.llm.codereview.sdk.model.parameter;

import com.openai.client.OpenAIClient;

public class LlmClient {

    private OpenAIClient openAIClient;

    public OpenAIClient getOpenAIClient() {
        return openAIClient;
    }

    public LlmClient setOpenAIClient(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
        return this;
    }
}
