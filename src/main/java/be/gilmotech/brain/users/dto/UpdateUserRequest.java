package be.gilmotech.brain.users.dto;

public record UpdateUserRequest(
        String displayName,
        String avatarColor,
        String preferredMode
) {}
