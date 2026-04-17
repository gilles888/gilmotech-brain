package be.gilmotech.brain.agent;

import jakarta.validation.constraints.NotBlank;

public record AgentRequest(
        @NotBlank String task,
        @NotBlank String sessionId,
        String mode
) {
    public String resolvedMode() {
        return mode != null && !mode.isBlank() ? mode : "conversation";
    }
}
