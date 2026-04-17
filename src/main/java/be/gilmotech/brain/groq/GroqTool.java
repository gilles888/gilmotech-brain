package be.gilmotech.brain.groq;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqTool(
        String type,
        Function function
) {
    public record Function(
            String name,
            String description,
            Object parameters
    ) {}

    public GroqTool(String name, String description, Object parameters) {
        this("function", new Function(name, description, parameters));
    }
}
