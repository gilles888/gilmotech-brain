package be.gilmotech.brain.users.dto;

public record LoginResponse(
        String token,
        String userId,
        String displayName,
        String avatarUrl,
        String avatarColor,
        String role,
        String preferredMode
) {}
