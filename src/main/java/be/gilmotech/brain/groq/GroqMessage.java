package be.gilmotech.brain.groq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroqMessage(
        String role,
        String content,
        @JsonProperty("tool_call_id") String toolCallId,
        @JsonProperty("tool_calls") List<ToolCall> toolCalls,
        String name
) {
    public GroqMessage(String role, String content, String toolCallId) {
        this(role, content, toolCallId, null, null);
    }

    public GroqMessage(String role, String content) {
        this(role, content, null, null, null);
    }
}
