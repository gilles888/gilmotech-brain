package be.gilmotech.brain.groq;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ToolCall(
        String id,
        String type,
        Function function
) {
    public record Function(
            String name,
            String arguments
    ) {}

    public String name() {
        return function != null ? function.name() : null;
    }

    public String argumentsRaw() {
        return function != null ? function.arguments() : null;
    }
}
