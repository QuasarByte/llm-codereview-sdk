package com.quasarbyte.llm.codereview.sdk.model.configuration;

public class LlmMessagesMapperConfigurationRhino extends LlmMessagesMapperConfiguration {
    private String scriptBody;
    private String functionName;

    public String getScriptBody() {
        return scriptBody;
    }

    public LlmMessagesMapperConfigurationRhino setScriptBody(String scriptBody) {
        this.scriptBody = scriptBody;
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public LlmMessagesMapperConfigurationRhino setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }
}
