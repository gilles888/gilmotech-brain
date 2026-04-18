package be.gilmotech.brain.users.dto;

import java.time.LocalDateTime;

public record UserProfileDto(
        String id,
        String username,
        String displayName,
        String avatarUrl,
        String avatarColor,
        String role,
        String preferredMode,
        LocalDateTime createdAt
) {}
