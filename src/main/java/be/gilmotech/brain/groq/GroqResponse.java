package be.gilmotech.brain.groq;

import java.util.List;

public record GroqResponse(
        String content,
        List<ToolCall> toolCalls,
        String finishReason
) {}
