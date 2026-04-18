package be.gilmotech.brain.users;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 7)
    @Builder.Default
    private String avatarColor = "#7C3AED";

    @Column(length = 20)
    @Builder.Default
    private String role = "member";

    @Column(length = 20)
    @Builder.Default
    private String preferredMode = "conversation";

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
