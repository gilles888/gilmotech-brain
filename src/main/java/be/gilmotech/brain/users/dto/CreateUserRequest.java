package be.gilmotech.brain.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 50) String username,
        @NotBlank @Size(min = 1, max = 100) String displayName,
        @NotBlank @Size(min = 6) String password,
        String avatarColor,
        String preferredMode
) {}
