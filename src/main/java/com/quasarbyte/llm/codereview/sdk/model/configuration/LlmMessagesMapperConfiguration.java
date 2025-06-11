package com.quasarbyte.llm.codereview.sdk.model.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LlmMessagesMapperConfigurationRhino.class, name = "Rhino")
})
public abstract class LlmMessagesMapperConfiguration {

}
